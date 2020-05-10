package com.lac.controller;

import com.lac.model.Image;
import com.lac.model.Role;
import com.lac.model.RoleName;
import com.lac.model.User;
import com.lac.payload.ApiResponse;
import com.lac.payload.JwtAuthenticationResponse;
import com.lac.payload.LoginRequest;
import com.lac.payload.SignUpRequest;
import com.lac.repository.FileRepository;
import com.lac.repository.RoleRepository;
import com.lac.repository.UserRepository;
import com.lac.security.JwtTokenProvider;
import com.lac.service.EmailService;
import lombok.AllArgsConstructor;
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

        User user = new User(request.getName(), request.getUsername(), request.getEmail(), request.getPassword());

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
