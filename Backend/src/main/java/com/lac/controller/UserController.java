package com.lac.controller;

import com.lac.model.EmailConfirmation;
import com.lac.model.Image;
import com.lac.model.User;
import com.lac.payload.ApiResponse;
import com.lac.payload.PasswordRequest;
import com.lac.payload.UploadFileResponse;
import com.lac.dto.UserDto;
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

    @GetMapping("/user/me/dto")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserDto> getCurrentUserInfo(@CurrentUser UserPrincipal currentUser) {
        User user = userRepository.findByUserId(currentUser.getUserId());
        UserDto dto = userService.getUserDto(user);
        return new ResponseEntity<>(dto, HttpStatus.OK);
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
            return new ResponseEntity<>(new ApiResponse(true, "Имя успешно изменено"), HttpStatus.OK);
        return new ResponseEntity<>(new ApiResponse(false, "Имя не было изменено. Неверное имя"), HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/user/me/edit/username")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> editUsername(@CurrentUser UserPrincipal currentUser,
                                          @RequestParam(name = "username") String username) {
        if (userRepository.existsByUsername(username))
            return new ResponseEntity<>(new ApiResponse(false, "Имя пользователя не было изменено. Имя пользоваьеля уже занято"), HttpStatus.BAD_REQUEST);
        if(userService.editUsername(currentUser, username))
            return new ResponseEntity<>(new ApiResponse(true, "Имя пользователя было успешно изменено"), HttpStatus.OK);
        return new  ResponseEntity<>(new ApiResponse(false, "Имя пользователя не было изменено. Неверное имя"), HttpStatus.BAD_REQUEST);
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
            return new ResponseEntity<>(new ApiResponse(true, "Код подтверждения был выслан на Ваш email"), HttpStatus.OK);
        return new ResponseEntity<>(new ApiResponse(false, "Email уже используется"), HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/user/me/edit/email/confirm")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> confirmEmail(@CurrentUser UserPrincipal currentUser,
                                          @RequestParam("code") String code) {
        User user = userRepository.findByUserId(currentUser.getUserId());
        EmailConfirmation confirmation = emailConfirmationRepository.findByUser(user);

        if (confirmation == null)
            return new ResponseEntity<>(new ApiResponse(false, "Вам не нужно подтверждать email"), HttpStatus.BAD_REQUEST);
        if (!confirmation.getCode().equals(code))
            return new ResponseEntity<>(new ApiResponse(false, "Неверный код подтверждения"), HttpStatus.BAD_REQUEST);

        user.setEmail(confirmation.getNewEmail());
        emailConfirmationRepository.delete(confirmation);
        userRepository.save(user);

        return new ResponseEntity<>(new ApiResponse(true, "Email был изменен"), HttpStatus.OK);
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

            return new ResponseEntity<>(new ApiResponse(true, "Аватар был удален"), HttpStatus.OK);
        }
        return new ResponseEntity<>(new ApiResponse(false, "Аватар не был удален, его не существует"), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("user/me/imagetype")
    public MediaType getImageContentType(@CurrentUser UserPrincipal currentUser) {
        User user = userRepository.findByUserId(currentUser.getUserId());
        Image image = user.getImage();
        MediaType mediaType = MediaType.valueOf(image.getType());
        return mediaType;
    }
}
