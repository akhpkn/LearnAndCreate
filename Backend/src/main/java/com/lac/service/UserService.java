package com.lac.service;

import com.lac.dto.UserDto;
import com.lac.dto.mapper.EntityToDtoMapper;
import com.lac.model.User;
import com.lac.payload.ApiResponse;
import com.lac.repository.UserRepository;
import com.lac.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final EntityToDtoMapper entityToDtoMapper = new EntityToDtoMapper();

    public boolean editUsername(UserPrincipal currentUser, String username){
        if (username.length() > 15 || username.length() < 4)
            return false;
        User user = userRepository.findByUserId(currentUser.getUserId());
        user.setUsername(username.toLowerCase());
        userRepository.save(user);
        return true;
    }

    public boolean editName(UserPrincipal currentUser, String name) {
        if (name.length() > 20 || name.length() < 2)
            return false;
        User user = userRepository.findByUserId(currentUser.getUserId());
        user.setName(name);
        userRepository.save(user);
        return true;
    }

    public ApiResponse editPassword(UserPrincipal currentUser, String oldPassword,
                                    String newPassword, String repeatedPassword){
        User user = userRepository.findByUserId(currentUser.getUserId());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            user.getUsername(),
                            oldPassword
                    )
            );
        }
        catch (BadCredentialsException ex) {
            return new ApiResponse(false, "Old password is incorrect");
        }

        if (oldPassword.equals(newPassword))
            return new ApiResponse(false, "New password must be different from old one");
        if (!newPassword.equals(repeatedPassword))
            return new ApiResponse(false, "You repeated the password incorrectly");

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return new ApiResponse(true, "The password was edited");
    }

    public UserDto getUserDto(User user) {
        return entityToDtoMapper.userToDto(user);
    }
}
