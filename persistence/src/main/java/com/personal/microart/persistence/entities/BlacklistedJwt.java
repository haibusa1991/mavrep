package com.personal.microart.persistence.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
@Getter
@Entity
@Table(name = "blacklisted_jwt")

public class BlacklistedJwt {

    @Builder
    public BlacklistedJwt(String token, LocalDateTime validity) {
        this.token = token;
        this.validity = validity;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String token;

    private LocalDateTime validity;

}
