package com.lac.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lac.model.Image;
import com.lac.model.Role;
import com.lac.model.RoleName;
import com.lac.model.User;
import com.lac.payload.*;
import com.lac.repository.FileRepository;
import com.lac.repository.RoleRepository;
import com.lac.repository.UserRepository;
import com.lac.security.JwtTokenProvider;
import com.lac.service.EmailService;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import lombok.AllArgsConstructor;
import org.apache.http.protocol.HTTP;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final FileRepository fileRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtTokenProvider tokenProvider;

    private final EmailService emailService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        HttpHeaders headers = new HttpHeaders();
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/vk/signin")
    public ResponseEntity<?> authenticateVkUser(@Valid @RequestBody ClientVkRequest VkRequest) throws IOException {
        String jwt = "";
        OkHttpClient vk = new OkHttpClient();

        ObjectMapper objectMapper = new ObjectMapper();

        MediaType mediaType = MediaType.parse("text/plain");
        com.squareup.okhttp.RequestBody body = com.squareup.okhttp.RequestBody.create(mediaType, "");


        Request request = new Request.Builder()
                .url("https://oauth.vk.com/access_token?client_id="+ VkRequest.getClient_id() + "&client_secret=" + VkRequest.getClient_secret()
                        + "&redirect_uri=" + VkRequest.getRedirect_uri()+ "&code=" + VkRequest.getCode())
                .method("POST", body)
                .addHeader("Cookie", "remixlang=0")
                .build();
        Response response2 = vk.newCall(request).execute();
        String b = response2.body().string();

         AccessTokenVkResponse accessTokenVkResponse = objectMapper.readValue(b, AccessTokenVkResponse.class);



        OkHttpClient client = new OkHttpClient();
        Request vkRequest = new Request.Builder()
                .url("https://api.vk.com/method/users.get?user_ids=" + accessTokenVkResponse.getUser_id()
                        + "&fields=photo_50,domain&access_token=" + accessTokenVkResponse.getAccess_token() + "&v=5.103")
                .method("GET", null)
                .addHeader("Authorization", accessTokenVkResponse.getAccess_token() )
                .addHeader("Content-Type", "jsonp")
                .build();
        Response response = client.newCall(vkRequest).execute();


        VkResponse vkResponse = objectMapper.readValue(response.body().string(), VkResponse.class);

        VkUser user = vkResponse.response[0];
        if(userRepository.existsByUsername(user.getDomain())) {

            User noVkUser = userRepository.findByUsername(user.getDomain());
            String estimated = noVkUser.getPassword();
            String real = passwordEncoder.encode(user.getDomain());
            if(noVkUser.getRegistrationType() != RegistrationType.VK){
                return new ResponseEntity<>(new ApiResponse(false, "Пользователь с таким именем уже сущесвует!"),
                        HttpStatus.BAD_REQUEST);
            }
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getDomain(),
                            user.getDomain()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            jwt = tokenProvider.generateToken(authentication);

        }
        else{
            if (userRepository.existsByEmail(accessTokenVkResponse.getEmail())) {
                return new ResponseEntity<>(new ApiResponse(false, "Такой email уже привзяан к другому аккаунту!"),
                        HttpStatus.BAD_REQUEST);
            }

            User newUser = new User(user.getFirst_name(), user.getLast_name(),
                    user.getDomain(), (accessTokenVkResponse.getEmail() == "" || accessTokenVkResponse.getEmail() == null )? "default@mail.ru" : accessTokenVkResponse.getEmail(),
                    user.getDomain(),RegistrationType.VK);

            Role userRole = roleRepository.findByName(RoleName.ROLE_USER);

            if (userRole == null)
                throw new RuntimeException("User Role not set");
            newUser.setRoles(Collections.singleton(userRole));

            Image image = new Image(user.getPhoto_50(), "image/jpeg");

            fileRepository.save(image);

            newUser.setImage(image);

            newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));

            User result = userRepository.save(newUser);

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getDomain(),
                            user.getDomain()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            jwt = tokenProvider.generateToken(authentication);
        }
        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignUpRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            return new ResponseEntity<>(new ApiResponse(false, "Такое имя пользователя уже занято!"),
                    HttpStatus.BAD_REQUEST);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return new ResponseEntity<>(new ApiResponse(false, "Такой email уже привзяан к другому аккаунту!"),
                    HttpStatus.BAD_REQUEST);
        }

        User user = new User(request.getName(), request.getSurname(), request.getUsername(), request.getEmail(), request.getPassword(), RegistrationType.DEFAULT);

        Role userRole = roleRepository.findByName(RoleName.ROLE_USER);
        if (userRole == null)
            throw new RuntimeException("User Role not set");
        user.setRoles(Collections.singleton(userRole));

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User result = userRepository.save(user);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(result.getUsername()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "Вы успешно зарегистрированы!"));
    }

    @PostMapping("/forgotpassword")
    public ResponseEntity<?> forgotPassword(@RequestParam("usernameOrEmail") String usernameOrEmail) {
        boolean success = emailService.sendPassword(usernameOrEmail);

        if (success)
            return new ResponseEntity<>(new ApiResponse(true, "Временный пароль был выслан на вашу почту."), HttpStatus.OK);
        else return new ResponseEntity<>(new ApiResponse(false, "Пользователь с таким именем или email не найден."), HttpStatus.BAD_REQUEST);
    }
}
