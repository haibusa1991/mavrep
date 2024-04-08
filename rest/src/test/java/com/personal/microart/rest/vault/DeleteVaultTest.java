package com.personal.microart.rest.vault;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personal.microart.api.operations.vault.delete.DeleteVaultInput;
import com.personal.microart.core.auth.jwt.JwtProvider;
import com.personal.microart.persistence.entities.Artefact;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.repositories.ArtefactRepository;
import com.personal.microart.persistence.repositories.UserRepository;
import com.personal.microart.persistence.repositories.VaultRepository;
import com.personal.microart.rest.controllers.ExchangeAccessor;
import io.undertow.server.HttpServerExchange;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;

import static com.personal.microart.rest.Endpoints.VAULT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class DeleteVaultTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VaultRepository vaultRepository;

    @Autowired
    private ArtefactRepository artefactRepository;

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


    private final String VAULT_NAME = "testVault";

    private final String URI_TEMPLATE = VAULT;

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

    private final Artefact Artefact1 = Artefact
            .builder()
            .uri("/artefact1")
            .filename("artefact1")
            .build();

    private final Artefact Artefact2 = Artefact
            .builder()
            .uri("/artefact2")
            .filename("artefact2")
            .build();

    @BeforeEach
    public void setup() {
        this.userRepository.saveAll(List.of(EXISTING_USER_1, EXISTING_USER_2));
        this.artefactRepository.saveAll(List.of(this.Artefact1, this.Artefact2));

        Vault vault = Vault
                .builder()
                .name(this.VAULT_NAME)
                .owner(EXISTING_USER_1)
                .build();

        vault.addArtefact(Artefact1);
        vault.addArtefact(Artefact2);
        this.vaultRepository.save(vault);
    }

    @AfterEach
    public void teardown() {
        this.vaultRepository.deleteAll();
        this.userRepository.deleteAll();
        this.artefactRepository.deleteAll();
    }

    private String getAuthHeaderValue(MicroartUser user) {
        return "Bearer " + this.conversionService.convert(this.jwtProvider.getJwt(user), String.class);
    }

    @SneakyThrows
    @Test
    public void returns204whenVaultDeleted() {
        DeleteVaultInput input = DeleteVaultInput
                .builder()
                .vaultName(this.VAULT_NAME)
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete(URI_TEMPLATE)
                        .header("Authorization", getAuthHeaderValue(EXISTING_USER_1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        assertEquals(0, this.vaultRepository.count());

        long nullArtefacts = this.artefactRepository
                .findAll()
                .stream()
                .filter(artefact -> artefact.getFilename() == null)
                .count();
        assertEquals(2, nullArtefacts);
    }

    @SneakyThrows
    @Test
    public void returns404whenVaultMissing() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.URI_TEMPLATE);

        DeleteVaultInput input = DeleteVaultInput
                .builder()
                .vaultName("non-existent")
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete(URI_TEMPLATE)
                        .header("Authorization", getAuthHeaderValue(EXISTING_USER_1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @SneakyThrows
    @Test
    public void returns400whenVaultNameEmpty() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.URI_TEMPLATE);

        DeleteVaultInput input = DeleteVaultInput
                .builder()
                .vaultName("")
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete(URI_TEMPLATE)
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

        DeleteVaultInput input = DeleteVaultInput
                .builder()
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete(URI_TEMPLATE)
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

        DeleteVaultInput input = DeleteVaultInput
                .builder()
                .vaultName("A".repeat(101))
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete(URI_TEMPLATE)
                        .header("Authorization", getAuthHeaderValue(EXISTING_USER_1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @SneakyThrows
    @Test
    public void returns404whenVaultNotExisting() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.URI_TEMPLATE);

        DeleteVaultInput input = DeleteVaultInput
                .builder()
                .vaultName("non-existent")
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete(URI_TEMPLATE)
                        .header("Authorization", getAuthHeaderValue(EXISTING_USER_1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @SneakyThrows
    @Test
    public void returns404whenTryingToDeleteNotOwnedVault() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.URI_TEMPLATE);

        DeleteVaultInput input = DeleteVaultInput
                .builder()
                .vaultName(this.VAULT_NAME)
                .build();

        String content = this.objectMapper
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writeValueAsString(input);

        mockMvc.perform(MockMvcRequestBuilders
                        .delete(URI_TEMPLATE)
                        .header("Authorization", getAuthHeaderValue(EXISTING_USER_2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }


}
