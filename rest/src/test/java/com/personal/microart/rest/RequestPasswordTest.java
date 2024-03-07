package com.personal.microart.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.microart.api.errors.ServiceUnavailableError;
import com.personal.microart.api.operations.user.requestpassword.RequestPasswordInput;
import com.personal.microart.core.email.mailgun.MailgunEmailSender;
import com.personal.microart.core.email.mailgun.MailgunResponse;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.PasswordRecoveryToken;
import com.personal.microart.persistence.repositories.PasswordRecoveryTokenRepository;
import com.personal.microart.persistence.repositories.UserRepository;
import com.personal.microart.rest.controllers.ExchangeAccessor;
import io.undertow.server.HttpServerExchange;
import io.vavr.control.Either;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class RequestPasswordTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MailgunEmailSender emailSender;

    @MockBean
    private ExchangeAccessor exchangeAccessor;

    @Autowired
    private PasswordRecoveryTokenRepository passwordRecoveryTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${PASSWORD_RECOVERY_TOKEN_VALIDITY}")
    private Long TOKEN_VALIDITY;

    private final String URI = "/user/request-password";
    private final String USER_EMAIL = "test@test.bg";
    private final String USER_PASSWORD = "password";
    private final String USER_USERNAME = "mytestuser";

    @BeforeAll
    public void init() {

    }

    @BeforeEach
    public void setUp() {
        doReturn(Either.right(new MailgunResponse())).when(this.emailSender).sendEmail(any());

        MicroartUser user = MicroartUser.builder()
                .username(this.USER_USERNAME)
                .password(this.passwordEncoder.encode(this.USER_PASSWORD))
                .email(this.USER_EMAIL)
                .build();

        this.userRepository.save(user);
    }

    @AfterEach
    public void tearDown() {
        this.passwordRecoveryTokenRepository.deleteAll();
        this.userRepository.deleteAll();
    }

    @SneakyThrows
    @ParameterizedTest
    @ValueSource(strings = {
            "emailwithoutat.com",
            "email@withoutdot",
            "@missinglocalpart.com",
            "missingdomain@.com",
            "email@domain..com",
            "email@domain.c",
            "email@.com",
            "email@domain",
            "email.domain.com",
            "email@domain@com"
    })
    public void returns400whenInputEmailNotEmail(String invalidEmail) {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(URI);

        RequestPasswordInput input = RequestPasswordInput
                .builder()
                .email(invalidEmail)
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(this.URI)
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[?(@.errors anyof ['email must be a well-formed email address'])]").exists())
                .andExpect(jsonPath("$.errors.length()").value(1));
    }

    @SneakyThrows
    @Test
    public void returns400whenInputEmailNull() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(URI);

        RequestPasswordInput input = RequestPasswordInput
                .builder()
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(this.URI)
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[?(@.errors anyof ['email must not be null'])]").exists())
                .andExpect(jsonPath("$.errors.length()").value(1));
    }


    @SneakyThrows
    @Test
    public void returns400whenInputEmailEmpty() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(URI);

        RequestPasswordInput input = RequestPasswordInput
                .builder()
                .email("")
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(this.URI)
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[?(@.errors anyof ['email must be a well-formed email address'])]").exists())
                .andExpect(jsonPath("$.errors.length()").value(1));
    }

    @SneakyThrows
    @Test
    public void returns400whenInputEmailLong() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(URI);

        RequestPasswordInput input = RequestPasswordInput
                .builder()
                .email("a".repeat(50) + "@abv.bg")
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(this.URI)
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[?(@.errors anyof ['email must be less than 40 characters'])]").exists())
                .andExpect(jsonPath("$.errors.length()").value(1));
    }

    @SneakyThrows
    @Test
    public void returns200WhenResetIsSuccessful() {
        RequestPasswordInput input = RequestPasswordInput
                .builder()
                .email(this.USER_EMAIL)
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(this.URI)
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isOk());

        Mockito.verify(this.emailSender, times(1)).sendEmail(any());

        assertEquals(1, this.passwordRecoveryTokenRepository.count());

        PasswordRecoveryToken passwordRecoveryToken = this.passwordRecoveryTokenRepository.findAll().get(0);
        MicroartUser user = userRepository.findByEmail(this.USER_EMAIL).get();

        assertEquals(passwordRecoveryToken.getUser(), user);
        assertEquals(passwordRecoveryToken.getIsValid(), true);
        assertEquals(this.TOKEN_VALIDITY,
                ChronoUnit.MINUTES.between(
                        passwordRecoveryToken.getValidUntil().minus(Duration.of(this.TOKEN_VALIDITY, ChronoUnit.MINUTES)),
                        passwordRecoveryToken.getValidUntil()
                ));
    }

    @SneakyThrows
    @Test
    public void returns204WhenUserDoesntExist() {
        // Exchange is required, since the processor returns SilentFailError when the user does not exist.
        // Despite status being 204, the return value technically is an error, so it requires the ExchangeAccessor.

        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(URI);

        RequestPasswordInput input = RequestPasswordInput
                .builder()
                .email("non-existent@abv.bg")
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(this.URI)
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isNoContent());

        Mockito.verify(this.emailSender, times(0)).sendEmail(any());

        assertEquals(0, this.passwordRecoveryTokenRepository.count());
    }

    @SneakyThrows
    @Test
    public void returns200AndInvalidatesExistingToken() {
        PasswordRecoveryToken passwordRecoveryToken = PasswordRecoveryToken
                .builder()
                .user(userRepository.findByEmail(this.USER_EMAIL).get())
                .tokenValue("test")
                .tokenValidity(10)
                .build();

        this.passwordRecoveryTokenRepository.save(passwordRecoveryToken);

        RequestPasswordInput input = RequestPasswordInput
                .builder()
                .email(this.USER_EMAIL)
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(this.URI)
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isOk());

        List<PasswordRecoveryToken> all = this.passwordRecoveryTokenRepository.findAll();

        all.stream()
                .filter(token -> token.getTokenValue().equalsIgnoreCase("test"))
                .forEach(token -> {
                    assertEquals(token.getIsValid(), false);
                });

        all.stream()
                .filter(token -> !token.getTokenValue().equalsIgnoreCase("test"))
                .forEach(token -> {
                    assertEquals(token.getIsValid(), true);
                });

    }

    @SneakyThrows
    @Test
    public void returns503WhenEmailSendingUnsuccessful() {
        doReturn(Either.left(new ServiceUnavailableError())).when(this.emailSender).sendEmail(any());

        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(URI);

        RequestPasswordInput input = RequestPasswordInput
                .builder()
                .email(this.USER_EMAIL)
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(this.URI)
                        .contentType("application/json")
                        .content(content))
                .andExpect(status().isServiceUnavailable());

    }


}
