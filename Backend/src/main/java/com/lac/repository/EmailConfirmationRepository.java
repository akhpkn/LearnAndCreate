package com.lac.repository;

import com.lac.model.EmailConfirmation;
import com.lac.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailConfirmationRepository extends JpaRepository<EmailConfirmation, Long> {
    EmailConfirmation findByUser(User user);
}
