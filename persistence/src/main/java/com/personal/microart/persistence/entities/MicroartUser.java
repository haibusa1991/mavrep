package com.personal.microart.persistence.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class MicroartUser {

    @Builder
    public MicroartUser(String email, String username, String password) {
        this.email = email;
        this.username = username;
        this.password = password;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    @Length(max = 40)
    @Email
    private String email;

    @Column(unique = true)
    @Length(min=1, max = 40)
    @Pattern(regexp = "^[^:]*$",message = "cannot contain ':'")
    private String username;

    @NotEmpty
    private String password;
}
