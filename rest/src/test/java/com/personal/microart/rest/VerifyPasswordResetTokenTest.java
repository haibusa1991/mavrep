package com.personal.microart.rest;

import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.PasswordRecoveryToken;
import com.personal.microart.persistence.repositories.PasswordRecoveryTokenRepository;
import com.personal.microart.persistence.repositories.UserRepository;
import com.personal.microart.rest.controllers.ExchangeAccessor;
import io.undertow.server.HttpServerExchange;
import lombok.SneakyThrows;
import org.hibernate.exception.JDBCConnectionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class VerifyPasswordResetTokenTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private ExchangeAccessor exchangeAccessor;

    @SpyBean
    private PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;

    private final String URI = "/user/password-reset?passwordResetToken=";
    private final String USER_EMAIL = "test@test.bg";
    private final String USER_PASSWORD = "password";
    private final String USER_USERNAME = "mytestuser";
    private final String RECOVERY_TOKEN = "A".repeat(50);


    @BeforeEach
    public void setUp() {
        MicroartUser user = MicroartUser.builder()
                .username(this.USER_USERNAME)
                .password(this.passwordEncoder.encode(this.USER_PASSWORD))
                .email(this.USER_EMAIL)
                .build();

        this.userRepository.save(user);

        PasswordRecoveryToken token = PasswordRecoveryToken
                .builder()
                .tokenValue(this.RECOVERY_TOKEN)
                .user(user)
                .tokenValidity(10)
                .build();

        this.passwordRecoveryTokenRepository.save(token);
    }

    @AfterEach
    public void tearDown() {
        this.passwordRecoveryTokenRepository.deleteAll();
        this.userRepository.deleteAll();
    }

    @SneakyThrows
    @Test
    public void returns200whenTokenIsValidAndExisting() {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(this.URI + this.RECOVERY_TOKEN)
                        .contentType("application/json"))
                .andExpect(status().isNoContent());
    }

    @SneakyThrows
    @Test
    public void returns400whenTokenLong() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.URI);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(this.URI + "A".repeat(101))
                        .contentType("application/json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[?(@.errors anyof ['resetToken must be less than 100 characters'])]").exists())
                .andExpect(jsonPath("$.errors.length()").value(1));
    }

    @SneakyThrows
    @Test
    public void returns401whenTokenNull() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.URI);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(this.URI)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.[?(@.errors anyof ['Token is expired or already used.'])]").exists())
                .andExpect(jsonPath("$.errors.length()").value(1));
    }

    @SneakyThrows
    @Test
    public void returns400whenTokenMissing() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.URI);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get("/user/password-reset")
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    public void returns401whenTokenUsed() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.URI);

        this.passwordRecoveryTokenRepository.findByTokenValue(this.RECOVERY_TOKEN)
                .ifPresent(token -> {
                    token.invalidate();
                    this.passwordRecoveryTokenRepository.save(token);
                });

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(this.URI + this.RECOVERY_TOKEN)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.[?(@.errors anyof ['Token is expired or already used.'])]").exists())
                .andExpect(jsonPath("$.errors.length()").value(1));
    }

    @SneakyThrows
    @Test
    public void returns401whenTokenExpired() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.URI);

        PasswordRecoveryToken token = this.passwordRecoveryTokenRepository.findByTokenValue(this.RECOVERY_TOKEN).get();
        Field field = token.getClass().getDeclaredField("validUntil");
        field.setAccessible(true);
        field.set(token, LocalDateTime.of(1970, 1, 1, 0, 0));
        this.passwordRecoveryTokenRepository.save(token);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(this.URI + this.RECOVERY_TOKEN)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.[?(@.errors anyof ['Token is expired or already used.'])]").exists())
                .andExpect(jsonPath("$.errors.length()").value(1));
    }

    @SneakyThrows
    @Test
    public void returns401whenTokenUsedAndExpired() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.URI);

        PasswordRecoveryToken token = this.passwordRecoveryTokenRepository.findByTokenValue(this.RECOVERY_TOKEN).get();
        token.invalidate();

        Field field = token.getClass().getDeclaredField("validUntil");
        field.setAccessible(true);
        field.set(token, LocalDateTime.of(1970, 1, 1, 0, 0));
        this.passwordRecoveryTokenRepository.save(token);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(this.URI + this.RECOVERY_TOKEN)
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.[?(@.errors anyof ['Token is expired or already used.'])]").exists())
                .andExpect(jsonPath("$.errors.length()").value(1));
    }

    @SneakyThrows
    @Test
    public void returns401whenTokenNonExistent() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.URI);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(this.URI + "non-existing-token")
                        .contentType("application/json"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.[?(@.errors anyof ['Token is expired or already used.'])]").exists())
                .andExpect(jsonPath("$.errors.length()").value(1));
    }

    @SneakyThrows
    @Test
    public void return503whenTokenRepositoryNotAccessible() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.URI);

        when(this.passwordRecoveryTokenRepository.findByTokenValue(any()))
                .thenThrow(new JDBCConnectionException("Test exception", new SQLException("Test SQL exception")));

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(this.URI + this.RECOVERY_TOKEN)
                        .contentType("application/json"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.[?(@.errors anyof ['Service unavailable.'])]").exists())
                .andExpect(jsonPath("$.errors.length()").value(1));
    }
}
