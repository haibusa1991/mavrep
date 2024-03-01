package com.personal.microart.persistence.entities;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;


@Entity
@Table(name = "password_recovery_tokens")
@Getter
@NoArgsConstructor
public class PasswordRecoveryToken {

    @Builder
    public PasswordRecoveryToken(MicroartUser user, String tokenValue, Integer tokenValidity) {
        this.user = user;
        this.tokenValue = tokenValue;
        this.isValid = true;
        this.validUntil = LocalDateTime.now(ZoneOffset.UTC).plusMinutes(tokenValidity);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDateTime validUntil;

    @ManyToOne
    private MicroartUser user;

    private String tokenValue;

    private Boolean isValid;

    public PasswordRecoveryToken invalidate() {
        isValid = false;
        return this;
    }
}
