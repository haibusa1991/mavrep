package com.personal.microart.rest;

import com.personal.microart.persistence.entities.Artefact;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.errors.Error;
import com.personal.microart.persistence.errors.ReadError;
import com.personal.microart.persistence.directorymanager.FileReader;
import com.personal.microart.persistence.repositories.ArtefactRepository;
import com.personal.microart.persistence.repositories.UserRepository;
import com.personal.microart.persistence.repositories.VaultRepository;
import com.personal.microart.rest.controllers.ExchangeAccessor;
import io.undertow.server.HttpServerExchange;
import io.vavr.control.Either;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
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
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class DownloadFileTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VaultRepository vaultRepository;

    @Autowired
    private ArtefactRepository artefactRepository;

    @MockBean
    private FileReader fileReader;

    @MockBean
    private ExchangeAccessor exchangeAccessor;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final byte[] FILE_CONTENTS = new byte[1024];

    private final String EXISTING_EMAIL = "test@test";
    private final String EXISTING_USERNAME = "testusername";
    private final String EXISTING_PASSWORD = "testpass";
    private final String EXISTING_VAULT = "test-vault";
    private final String ARTEFACT_URI = String.format("/mvn/%s/%s/com/test/download/0.0.1-SNAPSHOT/test-12345678.123456-1.jar", this.EXISTING_USERNAME, this.EXISTING_VAULT);
    private final String NON_EXISTENT_ARTEFACT_URI = String.format("/mvn/%s/%s/com/test/download/0.0.1-SNAPSHOT/readme.md", this.EXISTING_USERNAME, this.EXISTING_VAULT);

    @BeforeAll
    public void init() {

        new Random().nextBytes(this.FILE_CONTENTS);
    }

    @BeforeEach
    public void setup() {
        when(this.fileReader.readFile(any())).thenReturn(Either.right(this.FILE_CONTENTS));

        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.ARTEFACT_URI);

        MicroartUser EXISTING_USER = MicroartUser
                .builder()
                .email(EXISTING_EMAIL)
                .username(EXISTING_USERNAME)
                .password(this.passwordEncoder.encode(EXISTING_PASSWORD))
                .build();

        MicroartUser persistedUser = this.userRepository.save(EXISTING_USER);

        Vault vault = Vault
                .builder()
                .name(this.EXISTING_VAULT)
                .owner(persistedUser)
                .build();

        Artefact artefact = Artefact
                .builder()
                .uri(this.ARTEFACT_URI)
                .filename("test-12345678.123456-1.jar")
                .build();

        Artefact persistedArtefact = this.artefactRepository.save(artefact);

        vault.addUser(persistedUser);
        vault.addArtefact(persistedArtefact);

        this.vaultRepository.save(vault);
    }

    @AfterEach
    public void teardown() {
        this.vaultRepository.deleteAll();
        this.artefactRepository.deleteAll();
        this.userRepository.deleteAll();
    }

    private String encodeCredentials(String username, String rawPassword) {
        return Base64
                .getEncoder()
                .encodeToString((String.format("%s:%s", username, rawPassword).getBytes(StandardCharsets.UTF_8)))
                .replace("=", "");
    }

    private String getAuthHeaderValue(String username, String rawPassword) {
        return "Basic " + this.encodeCredentials(username, rawPassword);
    }

    @Test
    @SneakyThrows
    public void downloadsOnAnonymousUserWhenVaultIsPublic() {
        mockMvc.perform(MockMvcRequestBuilders.get(this.ARTEFACT_URI))
                .andExpect(status().isOk())
                .andExpect(content().bytes(this.FILE_CONTENTS));
    }

    @ParameterizedTest
    @SneakyThrows
    @ValueSource(strings = {
            "",
            "Basic",
            "Basic ",
            "Basic d",
            "Basic asdffsdgf",

    })
    public void downloadsWhenVaultIsPublicAndAuthHeaderInvalid(String headerValue) {
        mockMvc.perform(MockMvcRequestBuilders
                        .get(this.ARTEFACT_URI)
                        .header(HttpHeaders.AUTHORIZATION, headerValue))
                .andExpect(status().isOk())
                .andExpect(content().bytes(this.FILE_CONTENTS));
    }

    @SneakyThrows
    @Test
    public void downloadsOnAuthenticatedUserWhenVaultIsPublic() {
        mockMvc.perform(MockMvcRequestBuilders
                        .get(this.ARTEFACT_URI)
                        .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USERNAME, this.EXISTING_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(content().bytes(this.FILE_CONTENTS));
    }

    @SneakyThrows
    @Test
    public void downloadsOnNonExistentUserWhenVaultIsPublic() {
        mockMvc.perform(MockMvcRequestBuilders
                        .get(this.ARTEFACT_URI)
                        .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue("non-existent", "non-existent")))
                .andExpect(status().isOk())
                .andExpect(content().bytes(this.FILE_CONTENTS));
    }

    @SneakyThrows
    @Test
    public void downloadsOnAuthorizedUserWhenVaultIsPrivate() {
        String newEmail = "new@test";
        String newUsername = "newusername";
        String newPassword = "newpass";

        MicroartUser newUser = MicroartUser
                .builder()
                .email(newEmail)
                .username(newUsername)
                .password(this.passwordEncoder.encode(newPassword))
                .build();

        MicroartUser persistedNewUser = this.userRepository.save(newUser);

        this.vaultRepository.findVaultByName(this.EXISTING_VAULT)
                .ifPresent(vault -> {
                    vault.isPublic(false);
                    vault.addUser(persistedNewUser);
                    this.vaultRepository.save(vault);
                });

        mockMvc.perform(MockMvcRequestBuilders
                        .get(this.ARTEFACT_URI)
                        .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USERNAME, this.EXISTING_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(content().bytes(this.FILE_CONTENTS));

        mockMvc.perform(MockMvcRequestBuilders
                        .get(this.ARTEFACT_URI)
                        .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(newUsername, newPassword)))
                .andExpect(status().isOk())
                .andExpect(content().bytes(this.FILE_CONTENTS));
    }

    @SneakyThrows
    @Test
    public void returns404WhenAnonymousUserDownloadsNonExistentFile() {
        when(this.fileReader.readFile(any())).thenReturn(Either.left(ReadError.builder().error(Error.FILE_NOT_FOUND_ERROR).build()));

        mockMvc.perform(MockMvcRequestBuilders.get(this.NON_EXISTENT_ARTEFACT_URI))
                .andExpect(status().isNotFound());
    }


    @SneakyThrows
    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "Basic",
            "Basic ",
            "Basic d",
            "Basic asdffsdgf"
    })
    public void returns404WhenDownloadsNonExistentFileAndAuthHeaderIsInvalid(String headerValue) {
        when(this.fileReader.readFile(any())).thenReturn(Either.left(ReadError.builder().error(Error.FILE_NOT_FOUND_ERROR).build()));

        mockMvc.perform(MockMvcRequestBuilders
                        .get(this.NON_EXISTENT_ARTEFACT_URI)
                        .header(HttpHeaders.AUTHORIZATION, headerValue))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    public void returns404WhenAuthenticatedUserDownloadsNonExistentFile() {
        when(this.fileReader.readFile(any())).thenReturn(Either.left(ReadError.builder().error(Error.FILE_NOT_FOUND_ERROR).build()));

        mockMvc.perform(MockMvcRequestBuilders
                        .get(this.NON_EXISTENT_ARTEFACT_URI)
                        .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USERNAME, this.EXISTING_PASSWORD)))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    public void returns404WhenNonExistentUserDownloadsNonExistentFile() {
        when(this.fileReader.readFile(any())).thenReturn(Either.left(ReadError.builder().error(Error.FILE_NOT_FOUND_ERROR).build()));

        mockMvc.perform(MockMvcRequestBuilders
                        .get(this.NON_EXISTENT_ARTEFACT_URI)
                        .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue("non-existent", "non-existent")))
                .andExpect(status().isNotFound());
    }

    @SneakyThrows
    @Test
    public void returns403WhenAnonymousUserDownloadsFromPrivateVault() {
        this.vaultRepository.findVaultByName(this.EXISTING_VAULT)
                .ifPresent(vault -> {
                    vault.isPublic(false);
                    this.vaultRepository.save(vault);
                });

        mockMvc.perform(MockMvcRequestBuilders.get(this.ARTEFACT_URI))
                .andExpect(status().isForbidden());
    }

    @SneakyThrows
    @Test
    public void returns403WhenAuthenticatedUnauthorizedUserDownloadsFromPrivateVault() {
        String newEmail = "new@test";
        String newUsername = "newusername";
        String newPassword = "newpass";

        MicroartUser newUser = MicroartUser
                .builder()
                .email(newEmail)
                .username(newUsername)
                .password(this.passwordEncoder.encode(newPassword))
                .build();

        this.userRepository.save(newUser);

        this.vaultRepository.findVaultByName(this.EXISTING_VAULT)
                .ifPresent(vault -> {
                    vault.isPublic(false);
                    this.vaultRepository.save(vault);
                });

        mockMvc.perform(MockMvcRequestBuilders
                        .get(this.ARTEFACT_URI)
                        .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(newUsername, newPassword)))
                .andExpect(status().isForbidden());
    }

    @SneakyThrows
    @Test
    public void returns403WhenNonExistentUserDownloadsFromPrivateVault() {
        this.vaultRepository.findVaultByName(this.EXISTING_VAULT)
                .ifPresent(vault -> {
                    vault.isPublic(false);
                    this.vaultRepository.save(vault);
                });

        mockMvc.perform(MockMvcRequestBuilders
                        .get(this.ARTEFACT_URI)
                        .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue("non-existent", "non-existent")))
                .andExpect(status().isForbidden());
    }


}