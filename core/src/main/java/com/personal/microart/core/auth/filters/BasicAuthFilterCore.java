package com.personal.microart.core.auth.filters;

import com.personal.microart.core.auth.basic.BasicAuth;
import com.personal.microart.core.processor.UriProcessor;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.repositories.UserRepository;
import com.personal.microart.persistence.repositories.VaultRepository;
import io.vavr.control.Either;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class BasicAuthFilterCore extends BaseFilterCore {
    private final PasswordEncoder passwordEncoder;
    private final ConversionService conversionService;
    private final ApplicationContext context;
    private final UserRepository userRepository;
    private final UriProcessor uriProcessor;
    private final VaultRepository vaultRepository;

    @PostConstruct
    public void init() {
        super.setContext(this.context);
    }

    @Override
    public Authentication getAuthentication(HttpServletRequest request) {

        return Optional.ofNullable(this.conversionService.convert(Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)), BasicAuth.class))
                .flatMap(auth -> this.userRepository.findByUsername(auth.getUsername())
                        .map(this::getUserDetails)
                        .map(this::getAuthToken))
                .orElseGet(this::getCurrentToken);
    }

    private Authentication getCurrentToken() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private Authentication getAuthToken(UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    private UserDetails getUserDetails(MicroartUser user) {
        return new User(
                user.getUsername(),
                user.getPassword(),
                Set.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Override
    public Boolean isAuthorized(HttpServletRequest request) {
        String vaultName = this.uriProcessor.getVaultName(request.getRequestURI());

        // If the vault does not exist, then the request is authorized - the processor will handle the 404
        if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name()) && !this.vaultRepository.existsByName(vaultName)) {
            return true;
        }


        // get vault
        Optional<Vault> vaultOptional = this.vaultRepository.findVaultByName(vaultName);

        Vault vault = vaultOptional.orElseGet(Vault::empty);

        // check if vault is public - if so, return true
        if (request.getMethod().equalsIgnoreCase(HttpMethod.GET.name()) && vault.isPublic()) {
            return true;
        }

        // does request have auth header? if not, return false
        Boolean hasAuthHeader = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .map(header -> header.startsWith("Basic"))
                .orElse(false);

        if (!hasAuthHeader) {
            return false;
        }

        // does request have valid credentials in header? if not, return false
        BasicAuth auth = this.conversionService.convert(Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)), BasicAuth.class);
        Optional<MicroartUser> userOptional = this.userRepository
                .findByUsername(auth.getUsername())
                .flatMap(microartUser -> this.passwordEncoder.matches(auth.getPassword(), microartUser.getPassword()) ? Optional.of(microartUser) : Optional.empty());

        // does the vault belong to the user? if not, check if user is authorized to access the vault - if not, return false
        String targetUsername = this.uriProcessor.getUsername(request.getRequestURI());
        String currentUsername = userOptional.map(MicroartUser::getUsername).orElse("/");
        Boolean isOwnVault = targetUsername.equals(currentUsername);

        if(userOptional.isPresent() && vault.getAuthorizedUsers().isEmpty() && isOwnVault){
            vault.addUser(userOptional.get());
        }

        return userOptional.filter(microartUser -> vault.getAuthorizedUsers().contains(microartUser))
                .isPresent();

    }
}
