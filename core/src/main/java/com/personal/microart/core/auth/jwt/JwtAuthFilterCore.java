package com.personal.microart.core.auth.jwt;

import com.personal.microart.core.auth.base.BaseFilterCore;
import com.personal.microart.core.Extractor;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.repositories.UserRepository;
import com.personal.microart.persistence.repositories.VaultRepository;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;

/**
 * The JwtAuthFilterCore class is a component in the application that handles the authorization and authentication of
 * HTTP requests using JWT (JSON Web Token). It extends the BaseFilterCore class and provides implementations for the
 * isAuthorized and getAuthentication methods.<br>
 * The isAuthorized method checks whether an incoming HTTP request is authorized. It allows all requests to the "/browse"
 * endpoint and unrestricted download access to all public vaults. For private vaults, it checks if the JWT token
 * in the request header corresponds to a user who is authorized to access the vault.<br>
 * The getAuthentication method authenticates a user based on the JWT token present in the request header.
 * If the token is valid and corresponds to an existing user, it creates a new authentication token for the user.
 * If the token is not present or invalid, it returns the current authentication token from the security context.
 * The current token should be an AnonymousAuthenticationToken if the user hasn't been authenticated in some other way.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilterCore extends BaseFilterCore {
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final ApplicationContext context;
    private final VaultRepository vaultRepository;
    private final Extractor uriProcessor;

    @PostConstruct
    public void init() {
        super.setContext(this.context);
    }

    //TODO: Add to docs that blacklisted tokens are not allowed access
    /**
     * Checks if the request is authorized.
     * This method performs the following steps:<br>
     * 1. If the request URI is "/browse", the request is automatically authorized.<br>
     * 2. Extracts the vault name from the request URI.<br>
     * 3. Checks if the vault associated with the vault name is public. If it is, the request is authorized.<br>
     * 4. If the vault is not public, it extracts the JWT from the request header.<br>
     * 5. Checks if the user associated with the JWT is authorized to access the vault. If the user is authorized, the request is authorized.
     *
     * @param request The HttpServletRequest to be checked.
     * @return Boolean value indicating whether the request is authorized.
     */
    @Override
    public Boolean isAuthorized(HttpServletRequest request) {
        System.out.println("JwtAuthFilter was called");

        if (request.getRequestURI().equalsIgnoreCase("/browse")) {
            return true;
        }

        //TODO: If token is blacklisted, return false

        String vaultName = this.uriProcessor.getVaultName(request.getRequestURI());

        Vault vault = this.vaultRepository.findVaultByName(vaultName)
                .orElseGet(Vault::empty);

        if (vault.isPublic()) {
            return true;
        }

        Token token = Optional
                .ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .map(this.jwtProvider::getJwt)
                .orElseGet(Token::empty);

        return this.userRepository
                .findByUsername(token.getUsername())
                .filter(microartUser -> vault.getAuthorizedUsers().contains(microartUser))
                .isPresent();
    }

    /**
     * This method is used to authenticate a user based on the JWT token present in the request header.
     * The method performs the following steps:<br>
     * 1. Extracts the JWT token from the request header.<br>
     * 2. If the token is present, it uses the JWT provider to get the token.<br>
     * 3. It then finds the user associated with the username in the token.<br>
     * 4. If the user is found, it gets the user details and creates an authentication token for the user.<br>
     * 5. If the user is not found or the JWT token is not present in the request header, it returns the current authentication token from the security context.
     *
     * @param request The HttpServletRequest from which the JWT token is extracted.
     * @return An Authentication object which can be a new authentication token for the user or the current authentication token from the security context.
     */
    @Override
    public Authentication getAuthentication(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .map(this.jwtProvider::getJwt)
                .flatMap(token -> this.userRepository.findByUsername(token.getUsername())
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
     * This method is used to create an authentication token for a user. It performs the following steps:<br>
     * 1. Extracts the UserDetails and MicroartUser from the provided tuple.<br>
     * 2. Creates a new UsernamePasswordAuthenticationToken using the UserDetails, null as credentials,
     * and the authorities from the UserDetails.<br>
     * 3. Sets the details of the token to be the MicroartUser.<br>
     * 4. Returns the created token.<br>
     *
     * @param userDetailsAndUserTuple A tuple containing the UserDetails and MicroartUser for which the token is to be created.
     * @return A UsernamePasswordAuthenticationToken which represents the authentication token for the user.
     */
    private Authentication getAuthToken(Tuple2<UserDetails, MicroartUser> userDetailsAndUserTuple) {
        UserDetails userDetails = userDetailsAndUserTuple._1;
        MicroartUser user = userDetailsAndUserTuple._2;

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        token.setDetails(user);
        return token;
    }

    /**
     * This method is used to create a tuple containing UserDetails and MicroartUser.
     * UserDetails is created using the username, password and authorities of the MicroartUser.
     * The method performs the following steps:<br>
     * 1. Creates a new User object using the username, password and authorities of the MicroartUser.<br>
     * 2. Returns a tuple containing the created User object and the original MicroartUser.<br>
     *
     * @param user The MicroartUser from which the UserDetails is to be created.
     * @return A Tuple2 object containing the UserDetails and MicroartUser.
     */
    private Tuple2<UserDetails, MicroartUser> getUserDetails(MicroartUser user) {
        return Tuple.of(new User(
                        user.getUsername(),
                        user.getPassword(),
                        Set.of(new SimpleGrantedAuthority("ROLE_USER"))),
                user);
    }
}
