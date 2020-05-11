package com.lac.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.Email;

@Entity
@Table(name = "confirmations")
@Getter
@Setter
@NoArgsConstructor
public class EmailConfirmation {

    @Id
    @Column(name = "confirmation_id")
    private Long confirmationId;

    private String code;

    @Email
    private String newEmail;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @MapsId
    private User user;

    public EmailConfirmation(String code, String newEmail) {
        this.code = code;
        this.newEmail = newEmail;
    }
}
