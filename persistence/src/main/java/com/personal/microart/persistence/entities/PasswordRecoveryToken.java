package com.personal.microart.persistence.entities;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "password_recovery_tokens")
@Getter
@NoArgsConstructor
public class PasswordRecoveryToken {

    @Builder
    public PasswordRecoveryToken(MicroartUser user, String value, Integer tokenValidity) {
        this.user = user;
        this.value = value;
        this.isValid = true;
        this.validUntil = LocalDateTime.now().plusMinutes(tokenValidity);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDateTime validUntil;

    @ManyToOne
    private MicroartUser user;

    @Column(name = "token_value")
    private String value;

    private Boolean isValid;

    public PasswordRecoveryToken invalidate() {
        isValid = false;
        return this;
    }
}
