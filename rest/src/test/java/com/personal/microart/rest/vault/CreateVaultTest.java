package com.personal.microart.rest.vault;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.microart.api.operations.vault.create.CreateVaultInput;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class CreateVaultTest {
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

    private final String VAULT_NAME = "testVault";

    private final String URI_TEMPLATE = "/vault";

    private final MicroartUser EXISTING_USER_1 = MicroartUser
            .builder()
            .email(EXISTING_EMAIL_1)
            .username(EXISTING_USERNAME_1)
            .password(this.passwordEncoder.encode(EXISTING_PASSWORD_1))
            .build();

    @BeforeEach
    public void setup() {
        this.userRepository.saveAll(List.of(this.EXISTING_USER_1));
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
    public void returns201whenVaultCreated() {
        CreateVaultInput input = CreateVaultInput
                .builder()
                .vaultName(this.VAULT_NAME)
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        mockMvc.perform(MockMvcRequestBuilders.post(URI_TEMPLATE)
                        .header("Authorization", getAuthHeaderValue(EXISTING_USER_1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isCreated());

        Vault savedVault = vaultRepository.findAll().stream().findFirst().orElseThrow();
        assertEquals(1, vaultRepository.count());

        assertEquals(this.VAULT_NAME, savedVault.getName());
        assertEquals(0, savedVault.getArtefacts().size());
        assertEquals(1, savedVault.getAuthorizedUsers().size());
        assertTrue(savedVault.getAuthorizedUsers().contains(this.EXISTING_USER_1));
        assertTrue(savedVault.isPublic());
        assertEquals(this.EXISTING_USER_1, savedVault.getOwner());

    }

    @SneakyThrows
    @ParameterizedTest
    @ValueSource(strings = {
            "Hello World",
            "First#Tag",
            "100%off",
            "John&Doe",
            "Read/Write",
            "Query?String",
            "Login:User",
            "String#With#Many#Hashes",
            "@Email",
            "localhost:8080",
            "C++ tutorial",
            "Date=2022.07.13",
            "summer sale!",
            "$19.99",
            "example with space",
            "Path/To/File.txt",
            "<HTML>",
            "code=1234567",
            "Hello|World",
            "{JSON}"
    })
    public void returns400whenVaultNameNotUrlSafe(String vaultName) {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.URI_TEMPLATE);

        CreateVaultInput input = CreateVaultInput
                .builder()
                .vaultName(vaultName)
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        mockMvc.perform(MockMvcRequestBuilders.post(URI_TEMPLATE)
                        .header("Authorization", getAuthHeaderValue(EXISTING_USER_1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @SneakyThrows
    @Test
    public void returns400whenVaultNameMissing() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.URI_TEMPLATE);

        CreateVaultInput input = CreateVaultInput
                .builder()
                .vaultName("")
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        mockMvc.perform(MockMvcRequestBuilders.post(URI_TEMPLATE)
                        .header("Authorization", getAuthHeaderValue(EXISTING_USER_1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @SneakyThrows
    @Test
    public void returns400whenVaultNameLong() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.URI_TEMPLATE);

        CreateVaultInput input = CreateVaultInput
                .builder()
                .vaultName("a".repeat(105))
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        mockMvc.perform(MockMvcRequestBuilders.post(URI_TEMPLATE)
                        .header("Authorization", getAuthHeaderValue(EXISTING_USER_1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @SneakyThrows
    @Test
    public void returns400whenVaultNameNull() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.URI_TEMPLATE);

        CreateVaultInput input = CreateVaultInput
                .builder()
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        mockMvc.perform(MockMvcRequestBuilders.post(URI_TEMPLATE)
                        .header("Authorization", getAuthHeaderValue(EXISTING_USER_1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @SneakyThrows
    @Test
    public void returns409whenVaultNameDuplicate() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.URI_TEMPLATE);

        Vault vault = Vault
                .builder()
                .name(this.VAULT_NAME)
                .owner(this.EXISTING_USER_1)
                .build();
        this.vaultRepository.save(vault);

        CreateVaultInput input = CreateVaultInput
                .builder()
                .vaultName(this.VAULT_NAME)
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        mockMvc.perform(MockMvcRequestBuilders.post(URI_TEMPLATE)
                        .header("Authorization", getAuthHeaderValue(EXISTING_USER_1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isConflict());
    }
}
