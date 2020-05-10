package com.lac.controller;

import com.lac.model.EmailConfirmation;
import com.lac.model.Image;
import com.lac.model.User;
import com.lac.payload.ApiResponse;
import com.lac.payload.PasswordRequest;
import com.lac.payload.UploadFileResponse;
import com.lac.payload.UserInfo;
import com.lac.repository.EmailConfirmationRepository;
import com.lac.repository.UserRepository;
import com.lac.security.CurrentUser;
import com.lac.security.UserPrincipal;
import com.lac.service.EmailService;
import com.lac.service.ImageService;
import com.lac.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    private final ImageService imageService;

    private final PasswordEncoder passwordEncoder;

    private final UserService userService;

    private final EmailService emailService;

    private final EmailConfirmationRepository emailConfirmationRepository;

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<User> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        User user = userRepository.findByUserId(currentUser.getUserId());
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/user/me/info")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserInfo> getCurrentUserInfo(@CurrentUser UserPrincipal currentUser) {
        User user = userRepository.findByUserId(currentUser.getUserId());
        UserInfo info = user.userInfo();
        return new ResponseEntity<>(info, HttpStatus.OK);
    }

    @GetMapping("/user/checkUsernameAvailability")
    public ApiResponse checkUsernameAvailability(@RequestParam(value = "username") String username) {
        boolean isAvailable = !userRepository.existsByUsername(username);
        return new ApiResponse(isAvailable, "");
    }

    @GetMapping("/user/checkEmailAvailability")
    public ApiResponse checkEmailAvailability(@RequestParam(value = "email") String email) {
        boolean isAvailable = !userRepository.existsByEmail(email);
        return new ApiResponse(isAvailable, "");
    }

    @PutMapping("/user/me/edit/name")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> editName(@CurrentUser UserPrincipal currentUser,
                                         @RequestParam(name = "name") String name) {
        if(userService.editName(currentUser, name))
            return new ResponseEntity<>(new ApiResponse(true, "Name was edited successfully"), HttpStatus.OK);
        return new ResponseEntity<>(new ApiResponse(false, "Name was not edited. Name is incorrect"), HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/user/me/edit/username")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> editUsername(@CurrentUser UserPrincipal currentUser,
                                          @RequestParam(name = "username") String username) {
        if (userRepository.existsByUsername(username))
            return new ResponseEntity<>(new ApiResponse(false, "Username was not edited. The username is already taken"), HttpStatus.BAD_REQUEST);
        if(userService.editUsername(currentUser, username))
            return new ResponseEntity<>(new ApiResponse(true, "Username was edited successfully"), HttpStatus.OK);
        return new  ResponseEntity<>(new ApiResponse(false, "Username was not edited. The username is incorrect"), HttpStatus.BAD_REQUEST);
    }

    @PutMapping("user/me/edit/password")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> editPassword(@CurrentUser UserPrincipal currentUser,
                                            @RequestBody PasswordRequest request) {
        ApiResponse response = userService.editPassword(currentUser, request.getOldPassword(),
                request.getNewPassword(), request.getRepeatedPassword());
        if (response.getSuccess())
            return new ResponseEntity<>(response, HttpStatus.OK);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/user/me/edit/email")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> editEmail(@CurrentUser UserPrincipal currentUser,
                                       @RequestParam("email") String email) {
        User user = userRepository.findByUserId(currentUser.getUserId());

        boolean flag = emailService.sendCodeToConfirmEmail(user.getEmail(), email.toLowerCase());

        if (flag)
            return new ResponseEntity<>(new ApiResponse(true, "Confirmation code was sent to your email"), HttpStatus.OK);
        return new ResponseEntity<>(new ApiResponse(false, "This email is already in use"), HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/user/me/edit/email/confirm")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> confirmEmail(@CurrentUser UserPrincipal currentUser,
                                          @RequestParam("code") String code) {
        User user = userRepository.findByUserId(currentUser.getUserId());
        EmailConfirmation confirmation = emailConfirmationRepository.findByUser(user);

        if (confirmation == null)
            return new ResponseEntity<>(new ApiResponse(false, "You don't need to confirm email"), HttpStatus.BAD_REQUEST);
        if (!confirmation.getCode().equals(code))
            return new ResponseEntity<>(new ApiResponse(false, "Incorrect confirmation code"), HttpStatus.BAD_REQUEST);

        user.setEmail(confirmation.getNewEmail());
        emailConfirmationRepository.delete(confirmation);
        userRepository.save(user);

        return new ResponseEntity<>(new ApiResponse(true, "Email was edited"), HttpStatus.OK);
    }

    @PostMapping("/user/me/image")
    @PreAuthorize("hasRole('USER')")
    public UploadFileResponse setUserImage(@CurrentUser UserPrincipal currentUser,
                                           @RequestParam("file") MultipartFile file) throws IOException {
        User user = userRepository.findByUserId(currentUser.getUserId());
        if (user.getImage() != null)
            imageService.deleteImage(user.getImage());
        Image image = imageService.store(file);

        user.setImage(image);

        userRepository.save(user);

        return new UploadFileResponse(image.getUrl(), image.getType(), file.getSize());
    }

    @DeleteMapping("/user/me/image")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> removeUserImage(@CurrentUser UserPrincipal currentUser) {
        User user = userRepository.findByUserId(currentUser.getUserId());
        if (user.getImage() != null) {
            imageService.deleteImage(user.getImage());
            user.setImage(null);
            userRepository.save(user);

            return new ResponseEntity<>(new ApiResponse(true, "Image was removed"), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse(false, "Nothing to remove"), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("user/me/imagetype")
    public MediaType getImageContentType(@CurrentUser UserPrincipal currentUser) {
        User user = userRepository.findByUserId(currentUser.getUserId());
        Image image = user.getImage();
        MediaType mediaType = MediaType.valueOf(image.getType());
        return mediaType;
    }
}
