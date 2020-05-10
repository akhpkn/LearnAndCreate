package com.lac.service;

import com.lac.model.EmailConfirmation;
import com.lac.model.User;
import com.lac.repository.EmailConfirmationRepository;
import com.lac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String PASSWORD_TEXT = "Ваш временный пароль: ";
    private static final String CONFIRMATION_CODE = "Ваш код подвтерждения: ";

    private final JavaMailSender javaMailSender;

    private final UserRepository userRepository;

    private final EmailConfirmationRepository emailConfirmationRepository;

    private final PasswordEncoder passwordEncoder;

    public boolean sendPassword(String usernameOrEmail) {
        User receiver = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail);
        if (receiver == null)
            return false;

        SimpleMailMessage message = new SimpleMailMessage();

        String receiverEmail = receiver.getEmail();
        message.setTo(receiverEmail);
        message.setSubject("Смена пароля");

        String newPassword = generateString();
        String text = PASSWORD_TEXT + newPassword;
        message.setText(text);

        receiver.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(receiver);

        javaMailSender.send(message);
        return true;
    }

    public boolean sendCodeToConfirmEmail(String oldEmail, String newEmail) {
        SimpleMailMessage message = new SimpleMailMessage();
        String code = generateString();

        if (userRepository.existsByEmail(newEmail))
            return false;

        User user = userRepository.findByEmail(oldEmail);
        EmailConfirmation confirmation = new EmailConfirmation(code, newEmail);
        confirmation.setUser(user);
        emailConfirmationRepository.save(confirmation);
//        userRepository.save(user);

        message.setTo(newEmail);
        message.setSubject("Подтверждение новой почты");
        message.setText(CONFIRMATION_CODE + code);

        javaMailSender.send(message);

        return true;
    }

    private String generateString() {
        int length = 7;
        return RandomStringUtils.random(length, true, true);
    }
}
