package com.personal.microart.persistence.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
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
        this.enabled = true;
    }

    public static MicroartUser empty() {
        return MicroartUser
                .builder()
                .email("")
                .username("")
                .password("")
                .build();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    @Length(max = 40)
    @Email
    private String email;

    @Column(unique = true)
    @Length(min = 1, max = 40)
    @Pattern(regexp = "^[^:]*$", message = "cannot contain ':'")
    private String username;

    @NotEmpty
    @Setter
    private String password;

    @Accessors(fluent = true)
    private Boolean enabled;

    private MicroartUser enableUser() {
        this.enabled = true;
        return this;
    }

    private MicroartUser disableUser() {
        this.enabled = false;
        return this;
    }

    public Boolean isEmpty() {
        return this.username.isEmpty() && this.email.isEmpty() && this.password.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MicroartUser user = (MicroartUser) o;

        return id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
