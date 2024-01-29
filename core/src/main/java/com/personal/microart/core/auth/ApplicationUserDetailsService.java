package com.personal.microart.core.auth;

import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ApplicationUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
     MicroartUser persistedUser = this.userRepository
                .findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Email not found"));

        return new User(
                persistedUser.getEmail(),
                persistedUser.getPassword(),
                Set.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
