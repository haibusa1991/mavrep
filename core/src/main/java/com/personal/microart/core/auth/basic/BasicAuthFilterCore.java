package com.personal.microart.core.auth.basic;

import com.personal.microart.core.auth.base.BaseFilterCore;
import com.personal.microart.core.processor.UriProcessor;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.repositories.UserRepository;
import com.personal.microart.persistence.repositories.VaultRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * BasicAuthFilterCore is a component that handles the core logic of basic authentication.
 */
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
    private void init() {
        super.setContext(this.context);
    }

    /**
     * Returns the authentication token of the current request.
     *
     * @param request The HTTP request.
     * @return UsernamePasswordAuthenticationToken or the AnonymousAuthenticationToken taken from ApplicationContext.
     */
    @Override
    public Authentication getAuthentication(HttpServletRequest request) {
        return Optional.ofNullable(this.conversionService.convert(request.getHeader(HttpHeaders.AUTHORIZATION), BasicAuth.class))
                .flatMap(auth -> this.userRepository.findByUsername(auth.getUsername())
                        .map(this::getUserDetails)
                        .map(this::getAuthToken))
                .orElseGet(this::getCurrentToken);
    }

    /**
     * @return The current authentication token.
     */
    private Authentication getCurrentToken() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * Returns an authentication token for the given user details.
     *
     * @param userDetails The user details.
     * @return An UsernamePasswordAuthenticationToken.
     */
    private Authentication getAuthToken(UserDetails userDetails) {
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    /**
     * Returns the user details for the given MicroartUser.
     *
     * @param user The MicroartUser.
     * @return The user details.
     */
    private UserDetails getUserDetails(MicroartUser user) {
        return new User(
                user.getUsername(),
                user.getPassword(),
                Set.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    /**
     * Checks if the current request is authorized. In case that the authentication is already set, no further
     * checks are made. Authorized requests are those that have a valid 'Bearer' authorization header and are
     * accessing a public vault or a vault that the user is authorized to access. The User is authorized to access a
     * vault that they are the owner of or a vault that has the user added to the authorizedUsers set.
     *
     * @param request The HTTP request.
     * @return True if the request is authorized, false otherwise.
     */
    @Override
    public Boolean isAuthorized(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            return true;
        }

        String vaultName = this.uriProcessor.getVaultName(request.getRequestURI());

        if (this.isNonExistentVault(request, vaultName)) {
            return true;
        }

        Vault vault = this.getVault(vaultName);

        if (this.isPublicVault(request, vault)) {
            return true;
        }

        if (!this.hasAuthHeader(request)) {
            return false;
        }

        Optional<MicroartUser> userOptional = this.getUserOptional(request);

        return this.isAuthorizedUser(request, userOptional, vault);

    }

    /**
     * Checks whether the vault exists. If the request is a GET request and the vault does not exist, the method
     * returns true. The processor will then handle the request and return a 404 response.
     *
     * @param request   The HTTP request.
     * @param vaultName The name of the vault.
     * @return True if the vault does not exist and the method is GET, false otherwise.
     */
    private Boolean isNonExistentVault(HttpServletRequest request, String vaultName) {
        return request.getMethod().equalsIgnoreCase(HttpMethod.GET.name()) && !this.vaultRepository.existsByName(vaultName);
    }

    /**
     * Returns the vault with the given name. If the vault does not exist, an empty vault is returned.
     * Empty vaults are used to handle requests to non-existent vaults.
     *
     * @param vaultName The name of the vault.
     * @return The vault.
     */
    private Vault getVault(String vaultName) {
        return this.vaultRepository
                .findVaultByName(vaultName)
                .orElseGet(Vault::empty);
    }

    /**
     * Checks if the method is GET and the vault is public.
     *
     * @param request The HTTP request.
     * @param vault   The vault.
     * @return True if the vault is public, false otherwise.
     */
    private Boolean isPublicVault(HttpServletRequest request, Vault vault) {
        return request.getMethod().equalsIgnoreCase(HttpMethod.GET.name()) && vault.isPublic();
    }

    /**
     * Checks if the request has an authorization header. The header must be present and start with 'Basic'.
     *
     * @param request The HTTP request.
     * @return True if the request has an authorization header, false otherwise.
     */
    private Boolean hasAuthHeader(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .map(header -> header.startsWith("Basic"))
                .orElse(false);
    }

    /**
     * Returns the MicroartUser for the given request. Requires a valid 'Basic' authorization header.
     *
     * @param request The HTTP request.
     * @return The MicroartUser.
     */
    private Optional<MicroartUser> getUserOptional(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .map(rawHeader -> this.conversionService.convert(rawHeader, BasicAuth.class))
                .flatMap(auth -> this.userRepository
                        .findByUsername(auth.getUsername())
                        .flatMap(microartUser -> this.passwordEncoder.matches(auth.getPassword(), microartUser.getPassword())
                                ? Optional.of(microartUser)
                                : Optional.of(MicroartUser.empty()
                        )));
    }

    /**
     * Checks if the user is authorized.
     *
     * @param request      The HTTP request.
     * @param userOptional The optional MicroartUser.
     * @param vault        The vault.
     * @return True if the user is authorized, false otherwise.
     */
    private Boolean isAuthorizedUser(HttpServletRequest request, Optional<MicroartUser> userOptional, Vault vault) {
        String targetUsername = this.uriProcessor.getUsername(request.getRequestURI());
        String currentUsername = userOptional.map(MicroartUser::getUsername).orElse("/");
        Boolean isOwnVault = targetUsername.equals(currentUsername);

        if (userOptional.isPresent() && vault.getAuthorizedUsers().isEmpty() && isOwnVault) {
            vault.addUser(userOptional.get());
        }

        return userOptional.filter(microartUser -> vault.getAuthorizedUsers().contains(microartUser))
                .isPresent();
    }
}