package com.personal.microart.rest;

import com.personal.microart.core.auth.jwt.JwtProvider;
import com.personal.microart.core.auth.jwt.Token;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.repositories.BlacklistedJwtRepository;
import com.personal.microart.persistence.repositories.UserRepository;
import com.personal.microart.rest.controllers.ExchangeAccessor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class LogoutTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private BlacklistedJwtRepository blacklistedJwtRepository;

    @MockBean
    private ExchangeAccessor exchangeAccessor;

    private final String TEST_URI = "/test/jwt-auth";
    private final String LOGOUT_URI = "/user/logout";
    private final String USER_EMAIL = "test@test.bg";
    private final String USER_PASSWORD = "password";
    private final String USER_USERNAME = "mytestuser";

    private final String EXISTING_USER_EMAIL = "test_existing@test.bg";
    private final String EXISTING_USER_PASSWORD = "password2";
    private final String EXISTING_USER_USERNAME = "existing_testuser";

    private HttpHeaders headers;

    @BeforeEach
    public void setUp() {
        MicroartUser user = MicroartUser.builder()
                .username(this.USER_USERNAME)
                .password(this.passwordEncoder.encode(this.USER_PASSWORD))
                .email(this.USER_EMAIL)
                .build();

        MicroartUser user2 = MicroartUser.builder()
                .username(this.EXISTING_USER_USERNAME)
                .password(this.passwordEncoder.encode(this.EXISTING_USER_PASSWORD))
                .email(this.EXISTING_USER_EMAIL)
                .build();

        this.userRepository.saveAll(List.of(user, user2));

        headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + this.conversionService.convert(this.jwtProvider.getJwt(user), String.class));
    }

    @AfterEach
    public void tearDown() {
        this.blacklistedJwtRepository.deleteAll();
        this.userRepository.deleteAll();
    }

    @SneakyThrows
    @Test
    public void returns200whenJwtValid() {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(this.TEST_URI)
                        .headers(this.headers)
                        .contentType("application/json"))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    public void returns403whenJwtMissing() {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(this.TEST_URI)
                        .contentType("application/json"))
                .andExpect(status().isForbidden());
    }

    @SneakyThrows
    @Test
    public void returns204AndBlacklistsTokenOnLogOut() {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(this.LOGOUT_URI)
                        .headers(this.headers)
                        .contentType("application/json"))
                .andExpect(status().isOk());

        assertEquals(1, this.blacklistedJwtRepository.count());
        assertEquals(this.headers.get(HttpHeaders.AUTHORIZATION).get(0).substring(7),
                this.blacklistedJwtRepository.findAll().get(0).getToken());

    }

    @SneakyThrows
    @Test
    public void returns403whenJwtBlacklisted() {
        this.mockMvc.perform(MockMvcRequestBuilders
                        .post(this.LOGOUT_URI)
                        .headers(this.headers)
                        .contentType("application/json"))
                .andExpect(status().isOk());

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(this.TEST_URI)
                        .headers(this.headers)
                        .contentType("application/json"))
                .andExpect(status().isForbidden());
    }

    @SneakyThrows
    @Test
    public void returns403whenJwtExpired() {
        headers = new HttpHeaders();

        Token token = this.jwtProvider.getJwt(this.userRepository.findAll().get(0));

        Field field = token.getClass().getDeclaredField("exp");
        field.setAccessible(true);
        field.set(token, Instant.now().minus(1, ChronoUnit.DAYS));

        String jwt = this.conversionService.convert(token, String.class);

        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .get(this.TEST_URI)
                        .headers(headers)
                        .contentType("application/json"))
                .andExpect(status().isForbidden());
    }
//    TODO: write test for invalid jwt
//    @ParameterizedTest
//    @ArgumentsSource(InvalidJwtProvider.class)
//    @SneakyThrows
//    public void returns403whenJwtInvalid(String jwt) {
//        HttpHeaders headers = new HttpHeaders();
//        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);
//
//        this.mockMvc.perform(MockMvcRequestBuilders
//                        .get(this.TEST_URI)
//                        .headers(headers)
//                        .contentType("application/json"))
//                .andExpect(status().isForbidden());
//    }

//    static class InvalidJwtProvider implements ArgumentsProvider {
//        public InvalidJwtProvider() {
//        }
//
//        @Override
//        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
//            return Stream.of(
//                    Arguments.of(getUnsignedJwt(headers.get(HttpHeaders.AUTHORIZATION).get(0))),
//                    Arguments.of(getTamperedSignature(headers.get(HttpHeaders.AUTHORIZATION).get(0))),
//                    Arguments.of(getTamperedHeader(headers.get(HttpHeaders.AUTHORIZATION).get(0))),
//                    Arguments.of(getTamperedBody(headers.get(HttpHeaders.AUTHORIZATION).get(0)))
//            );
//        }
//    }
//
//    protected String getUnsignedJwt(String jwt) {
//        return jwt.substring(0, jwt.lastIndexOf('.') + 1);
//    }
//
//    private String getTamperedSignature(String jwt) {
//        return jwt.substring(0, jwt.lastIndexOf('.') + 1) + "tampered";
//    }
//
//    private String getTamperedHeader(String rawHeader) {
//        String jwt = rawHeader.substring(7);
//        String[] jwtElements = jwt.split("\\.");
//        byte[] header = Base64.getDecoder().decode(jwtElements[0]);
//        String tamperedHeader = new String(header).replace("HS256", "None");
//
//        return Base64.getEncoder().encodeToString(tamperedHeader.getBytes()) + "." + jwtElements[1] + "." + jwtElements[2];
//    }
//
//    private String getTamperedBody(String rawHeader) {
//        String jwt = rawHeader.substring(7);
//        String[] jwtElements = jwt.split("\\.");
//        byte[] body = Base64.getDecoder().decode(jwtElements[1]);
//        String tamperedBody = new String(body).replace(this.USER_USERNAME, this.EXISTING_USER_EMAIL);
//
//        return jwtElements[0] + "." + Base64.getEncoder().encodeToString(tamperedBody.getBytes()) + "." + jwtElements[2];
//    }


}
