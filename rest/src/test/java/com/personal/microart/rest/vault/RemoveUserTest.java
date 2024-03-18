package com.personal.microart.rest.vault;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.microart.api.operations.vault.adduser.AddUserInput;
import com.personal.microart.api.operations.vault.removeuser.RemoveUserInput;
import com.personal.microart.core.auth.jwt.JwtProvider;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.repositories.UserRepository;
import com.personal.microart.persistence.repositories.VaultRepository;
import com.personal.microart.rest.controllers.ExchangeAccessor;
import io.undertow.server.HttpServerExchange;
import lombok.SneakyThrows;
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
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class RemoveUserTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VaultRepository vaultRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExchangeAccessor exchangeAccessor;

    private final String EXISTING_EMAIL_1 = "test@test";
    private final String EXISTING_USERNAME_1 = "test-user1";
    private final String EXISTING_PASSWORD_1 = "testpass";

    private final String EXISTING_EMAIL_2 = "test2@test";
    private final String EXISTING_USERNAME_2 = "test-user2";
    private final String EXISTING_PASSWORD_2 = "testpass2";

    private final String EXISTING_VAULT_1 = "test-vault-1";

    private final String URI_TEMPLATE = "/vault/%s/user";

    private final MicroartUser EXISTING_USER_1 = MicroartUser
            .builder()
            .email(EXISTING_EMAIL_1)
            .username(EXISTING_USERNAME_1)
            .password(this.passwordEncoder.encode(EXISTING_PASSWORD_1))
            .build();

    private final MicroartUser EXISTING_USER_2 = MicroartUser
            .builder()
            .email(EXISTING_EMAIL_2)
            .username(EXISTING_USERNAME_2)
            .password(this.passwordEncoder.encode(EXISTING_PASSWORD_2))
            .build();

    @BeforeEach
    public void setup() {
        this.userRepository.saveAll(List.of(this.EXISTING_USER_1, this.EXISTING_USER_2));

        Vault vault = Vault
                .builder()
                .name(this.EXISTING_VAULT_1)
                .user(this.EXISTING_USER_1)
                .build();

        vault.addUser(this.EXISTING_USER_2);

        this.vaultRepository.save(vault);
    }

    @AfterEach
    public void teardown() {
        this.vaultRepository.deleteAll();
        this.userRepository.deleteAll();
    }

    private String getAuthHeaderValue(MicroartUser user) {
        return "Bearer " + this.conversionService.convert(this.jwtProvider.getJwt(user), String.class);
    }

    @SneakyThrows
    @Test
    public void returns204WhenOwnerRemovesExistingUser() {
        RemoveUserInput input = RemoveUserInput
                .builder()
                .username(this.EXISTING_USERNAME_2)
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete(String.format(this.URI_TEMPLATE, this.EXISTING_VAULT_1))
                        .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USER_1))
                        .content(content)
                        .contentType("application/json"))
                .andExpect(status().isNoContent());

        Vault vault = this.vaultRepository
                .findAll()
                .get(0);

        assertEquals(1, vault.getAuthorizedUsers().size());
        assertEquals(this.EXISTING_USER_1, vault.getAuthorizedUsers().iterator().next());
    }

    @SneakyThrows
    @Test
    public void returns204WhenOwnerRemovesExistingUserWhichAlreadyIsRemoved() {
        Vault vault1 = this.vaultRepository.findAll().get(0);
        vault1.removeUser(this.EXISTING_USER_2);
        this.vaultRepository.save(vault1);

        RemoveUserInput input = RemoveUserInput
                .builder()
                .username(this.EXISTING_USERNAME_2)
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete(String.format(this.URI_TEMPLATE, this.EXISTING_VAULT_1))
                        .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USER_1))
                        .content(content)
                        .contentType("application/json"))
                .andExpect(status().isNoContent());

        Vault vault = this.vaultRepository
                .findAll()
                .get(0);

        assertEquals(1, vault.getAuthorizedUsers().size());
        assertEquals(this.EXISTING_USER_1, vault.getAuthorizedUsers().iterator().next());
    }

    @SneakyThrows
    @Test
    public void returns403WhenNonOwnerRemovesExistingUser() {
        String uri = String.format(this.URI_TEMPLATE, this.EXISTING_VAULT_1);

        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(uri);

        RemoveUserInput input = RemoveUserInput
                .builder()
                .username(this.EXISTING_USERNAME_2)
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete(uri)
                        .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USER_2))
                        .content(content)
                        .contentType("application/json"))
                .andExpect(status().isForbidden());

        Vault vault = this.vaultRepository
                .findAll()
                .get(0);

        assertEquals(2, vault.getAuthorizedUsers().size());
    }

    @SneakyThrows
    @Test
    public void returns403WhenNonOwnerAddsNonExistingUser() {
        String uri = String.format(this.URI_TEMPLATE, this.EXISTING_VAULT_1);

        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(uri);

        RemoveUserInput input = RemoveUserInput
                .builder()
                .username("Non-existent")
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete(uri)
                        .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USER_2))
                        .content(content)
                        .contentType("application/json"))
                .andExpect(status().isForbidden());

        Vault vault = this.vaultRepository
                .findAll()
                .get(0);

        assertEquals(2, vault.getAuthorizedUsers().size());
    }

    @SneakyThrows
    @Test
    public void returns404WhenOwnerRemovesNonExistingUser() {
        String uri = String.format(this.URI_TEMPLATE, this.EXISTING_VAULT_1);

        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(uri);

        AddUserInput input = AddUserInput
                .builder()
                .username("non-existent-username")
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete(uri)
                        .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USER_1))
                        .content(content)
                        .contentType("application/json"))
                .andExpect(status().isNotFound());

        Vault vault = this.vaultRepository
                .findAll()
                .get(0);

        assertEquals(2, vault.getAuthorizedUsers().size());
    }

    @SneakyThrows
    @Test
    public void returns404WhenOwnerRemovesExistingUserToNonExistingVault() {
        String uri = String.format(this.URI_TEMPLATE, "non-existent-vault");

        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(uri);

        AddUserInput input = AddUserInput
                .builder()
                .username(this.EXISTING_USERNAME_2)
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete(uri)
                        .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USER_1))
                        .content(content)
                        .contentType("application/json"))
                .andExpect(status().isForbidden());

        Vault vault = this.vaultRepository
                .findAll()
                .get(0);

        assertEquals(2, vault.getAuthorizedUsers().size());
    }

    @SneakyThrows
    @Test
    public void returns400WhenOwnerRemovesAllUsersFromExistingVault() {
        String uri = String.format(this.URI_TEMPLATE, this.EXISTING_VAULT_1);

        Vault vault1 = this.vaultRepository.findAll().get(0);
        vault1.removeUser(this.EXISTING_USER_2);
        this.vaultRepository.save(vault1);

        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(uri);

        RemoveUserInput input = RemoveUserInput
                .builder()
                .username(this.EXISTING_USERNAME_1)
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        this.mockMvc.perform(MockMvcRequestBuilders
                        .delete(uri)
                        .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USER_1))
                        .content(content)
                        .contentType("application/json"))
                .andExpect(status().isBadRequest());

        Vault vault = this.vaultRepository
                .findAll()
                .get(0);

        assertEquals(1, vault.getAuthorizedUsers().size());
        assertEquals(this.EXISTING_USER_1, vault.getAuthorizedUsers().iterator().next());
    }
}