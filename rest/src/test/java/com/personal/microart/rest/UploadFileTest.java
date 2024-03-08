package com.personal.microart.rest;

import com.personal.microart.api.operations.file.upload.UploadFileInput;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.filehandler.FileReader;
import com.personal.microart.persistence.filehandler.FileWriter;
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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;
import java.util.stream.Stream;

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
class UploadFileTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VaultRepository vaultRepository;

    @Autowired
    private ArtefactRepository artefactRepository;

    @MockBean
    private FileWriter fileWriter;

    @MockBean
    private ExchangeAccessor exchangeAccessor;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final byte[] FILE_CONTENTS = new byte[1024];

    private final String EXISTING_EMAIL = "test@test";
    private final String EXISTING_USERNAME = "test";
    private final String EXISTING_PASSWORD = "testpass";

    private final String EXISTING_VAULT = "test-vault";

    private final MicroartUser EXISTING_USER = MicroartUser
            .builder()
            .email(EXISTING_EMAIL)
            .username(EXISTING_USERNAME)
            .password(this.passwordEncoder.encode(EXISTING_PASSWORD))
            .build();
    private final UploadFileInput VALID_INPUT = UploadFileInput
            .builder()
            .uri(String.format("/mvn/%s/%s/com/test/test/0.0.1-SNAPSHOT/mrt-0.0.4-20240125.124348-1.jar", this.EXISTING_USERNAME, this.EXISTING_VAULT))
            .content(this.FILE_CONTENTS)
            .authentication(getAuthHeaderValue(this.EXISTING_USERNAME, this.EXISTING_PASSWORD))
            .build();

    @BeforeAll
    public void init() {
        new Random().nextBytes(this.FILE_CONTENTS);
    }

    @BeforeEach
    public void setup() {
        this.userRepository.save(this.EXISTING_USER);
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
    public void returns403onAnonymousUser() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.VALID_INPUT.getUri());

        mockMvc.perform(MockMvcRequestBuilders.put(this.VALID_INPUT.getUri())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .content(this.VALID_INPUT.getContent()))
                .andExpect(status().isForbidden());
    }

    @SneakyThrows
    @Test
    public void returns403onInvalidCredentials() {
        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(this.VALID_INPUT.getUri());

        mockMvc.perform(MockMvcRequestBuilders.put(this.VALID_INPUT.getUri())
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .content(this.VALID_INPUT.getContent())
                        .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue("invalid", "invalidPass")))
                .andExpect(status().isForbidden());
    }

    @SneakyThrows
    @Test
    public void returns403onUploadInExistingUnauthorizedVault() {
        String EMAIL = "new@new";
        String USER_NAME = "newUser";
        String PASSWORD = "newPassword";
        String URI = String.format("/mvn/%s/%s/com/test/test/0.0.1-SNAPSHOT/new-20240125.124348-1.jar", this.EXISTING_USERNAME, this.EXISTING_VAULT);

        when(this.fileWriter.saveFileToDisk(any())).thenReturn(Either.right("dummy written file"));

        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(URI);

        Vault vault = Vault
                .builder()
                .user(this.userRepository.findByUsername(this.EXISTING_USERNAME).get())
                .name(this.EXISTING_VAULT)
                .build();

        vault.isPublic(false);
        this.vaultRepository.save(vault);

        MicroartUser newUser = MicroartUser
                .builder()
                .email(EMAIL)
                .username(USER_NAME)
                .password(this.passwordEncoder.encode(PASSWORD))
                .build();

        this.userRepository.save(newUser);


        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .put(URI)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(this.VALID_INPUT.getContent())
                .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(USER_NAME, PASSWORD))
        ).andReturn();

        Assertions.assertEquals(403, mvcResult.getResponse().getStatus());
        Assertions.assertEquals(1, this.vaultRepository.count());
        Assertions.assertEquals(2, this.userRepository.count());
        Assertions.assertEquals(0, this.artefactRepository.count());
    }

    @SneakyThrows
    @Test
    public void returns200onUploadInExistingAuthorizedVault() {
        String EMAIL = "new@new";
        String USER_NAME = "newUser";
        String PASSWORD = "newPassword";
        String URI = String.format("/mvn/%s/%s/com/test/test/0.0.1-SNAPSHOT/new-20240125.124348-1.jar", this.EXISTING_USERNAME, this.EXISTING_VAULT);

        when(this.fileWriter.saveFileToDisk(any())).thenReturn(Either.right("dummy written file"));

        Vault vault = Vault
                .builder()
                .user(this.userRepository.findByUsername(this.EXISTING_USERNAME).get())
                .name(this.EXISTING_VAULT)
                .build();

        MicroartUser newUser = MicroartUser
                .builder()
                .email(EMAIL)
                .username(USER_NAME)
                .password(this.passwordEncoder.encode(PASSWORD))
                .build();

        MicroartUser persistedNewUser = this.userRepository.save(newUser);
        vault.addUser(persistedNewUser);
        this.vaultRepository.save(vault);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .put(URI)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(this.VALID_INPUT.getContent())
                .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(USER_NAME, PASSWORD))
        ).andReturn();

        Assertions.assertEquals(200, mvcResult.getResponse().getStatus());
        Assertions.assertEquals(1, this.vaultRepository.count());
        Assertions.assertEquals(2, this.userRepository.count());
        Assertions.assertEquals(1, this.artefactRepository.count());
    }

    @SneakyThrows
    @Test
    public void returns200onUploadInExistingOwnVault() {
        String URI = String.format("/mvn/%s/%s/com/test/test/0.0.1-SNAPSHOT/new-20240125.124348-1.jar", this.EXISTING_USERNAME, this.EXISTING_VAULT);

        when(this.fileWriter.saveFileToDisk(any())).thenReturn(Either.right("dummy written file"));

        Vault vault = Vault
                .builder()
                .user(this.userRepository.findByUsername(this.EXISTING_USERNAME).get())
                .name(this.EXISTING_VAULT)
                .build();

        this.vaultRepository.save(vault);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .put(URI)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(this.VALID_INPUT.getContent())
                .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USERNAME, this.EXISTING_PASSWORD))
        ).andReturn();

        Assertions.assertEquals(200, mvcResult.getResponse().getStatus());
        Assertions.assertEquals(1, this.vaultRepository.count());
        Assertions.assertEquals(1, this.userRepository.count());
        Assertions.assertEquals(1, this.artefactRepository.count());
    }

    @SneakyThrows
    @Test
    public void returns200onUploadInNonExistingOwnVault() {
        String URI = String.format("/mvn/%s/%s/com/test/test/0.0.1-SNAPSHOT/new-20240125.124348-1.jar", this.EXISTING_USERNAME, this.EXISTING_VAULT);

        when(this.fileWriter.saveFileToDisk(any())).thenReturn(Either.right("dummy written file"));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .put(URI)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(this.VALID_INPUT.getContent())
                .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USERNAME, this.EXISTING_PASSWORD))
        ).andReturn();

        Assertions.assertEquals(200, mvcResult.getResponse().getStatus());
        Assertions.assertEquals(1, this.vaultRepository.count());
        Assertions.assertEquals(1, this.userRepository.count());
        Assertions.assertEquals(1, this.artefactRepository.count());
    }

    @SneakyThrows
    @Test
    public void returns403onUploadInNonExistingNotOwnVault() {
        String EMAIL = "new@new";
        String USER_NAME = "newUser";
        String PASSWORD = "newPassword";
        String URI = String.format("/mvn/%s/%s/com/test/test/0.0.1-SNAPSHOT/new-20240125.124348-1.jar", this.EXISTING_USERNAME, this.EXISTING_VAULT);

        when(this.fileWriter.saveFileToDisk(any())).thenReturn(Either.right("dummy written file"));

        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(URI);

        MicroartUser newUser = MicroartUser
                .builder()
                .email(EMAIL)
                .username(USER_NAME)
                .password(this.passwordEncoder.encode(PASSWORD))
                .build();

        this.userRepository.save(newUser);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .put(URI)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(this.VALID_INPUT.getContent())
                .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(USER_NAME, PASSWORD))
        ).andReturn();

        Assertions.assertEquals(403, mvcResult.getResponse().getStatus());
        Assertions.assertEquals(0, this.vaultRepository.count());
        Assertions.assertEquals(2, this.userRepository.count());
        Assertions.assertEquals(0, this.artefactRepository.count());
    }

    @SneakyThrows
    @Test
    public void returns400onDuplicateFilenameWhenUploadingInExistingOwnVault() {
        String URI = String.format("/mvn/%s/%s/com/test/test/0.0.1-SNAPSHOT/new-20240125.124348-1.jar", this.EXISTING_USERNAME, this.EXISTING_VAULT);
        String DUPLICATE_URI = String.format("/mvn/%s/%s/com/test/test/0.0.1-SNAPSHOT/new-20240125.124348-2.jar", this.EXISTING_USERNAME, this.EXISTING_VAULT);

        when(this.fileWriter.saveFileToDisk(any())).thenReturn(Either.right("dummy written file"));

        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(URI);

        Vault vault = Vault
                .builder()
                .user(this.userRepository.findByUsername(this.EXISTING_USERNAME).get())
                .name(this.EXISTING_VAULT)
                .build();

        this.vaultRepository.save(vault);
        mockMvc.perform(MockMvcRequestBuilders
                .put(URI)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(this.VALID_INPUT.getContent())
                .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USERNAME, this.EXISTING_PASSWORD)));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .put(DUPLICATE_URI)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(this.VALID_INPUT.getContent())
                .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USERNAME, this.EXISTING_PASSWORD))
        ).andReturn();

        Assertions.assertEquals(400, mvcResult.getResponse().getStatus());
        Assertions.assertEquals(1, this.vaultRepository.count());
        Assertions.assertEquals(1, this.userRepository.count());
        Assertions.assertEquals(1, this.artefactRepository.count());
    }

    @SneakyThrows
    @Test
    public void returns200onReplacingFileWhenUploadingInExistingOwnVault() {
        String URI = String.format("/mvn/%s/%s/com/test/test/0.0.1-SNAPSHOT/new-20240125.124348-1.jar", this.EXISTING_USERNAME, this.EXISTING_VAULT);

        when(this.fileWriter.saveFileToDisk(any())).thenReturn(Either.right("dummy written file"));

        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(URI);

        Vault vault = Vault
                .builder()
                .user(this.userRepository.findByUsername(this.EXISTING_USERNAME).get())
                .name(this.EXISTING_VAULT)
                .build();

        this.vaultRepository.save(vault);

        mockMvc.perform(MockMvcRequestBuilders
                .put(URI)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(this.VALID_INPUT.getContent())
                .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USERNAME, this.EXISTING_PASSWORD)));

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .put(URI)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(this.VALID_INPUT.getContent())
                .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USERNAME, this.EXISTING_PASSWORD))
        ).andReturn();

        Assertions.assertEquals(200, mvcResult.getResponse().getStatus());
        Assertions.assertEquals(1, this.vaultRepository.count());
        Assertions.assertEquals(1, this.userRepository.count());
        Assertions.assertEquals(1, this.artefactRepository.count());
    }


    private static Stream<String> returns400onInvalidFilenameWhenUploadingInExistingOwnVault() {
        return Stream.of(
                "ABCDEFGH.123456-1.xyz",
                "ABCDEFG.123456-1.xyz",
                "ABCDEFGHI.123456-1.xyz",
                "12345678.ABCDEF-1.xyz",
                "12345678.ABCDEFG-1.xyz",
                "12345678.ABCDE-1.xyz",
                "ABCDEFGH.ABCDEF-1.xyz",
                "ABCDEFG.ABCDE-1.xyz",
                "ABCDEFGHI.ABCDEFG-1.xyz",
                "ABCDEFGH.ABCDEFG-1.xyz",
                "ABCDEFGG.ABCDEF-1.xyz",
                "ABCDEFGH.ABCDE-1.xyz",
                "!@#$%^&*.098765-1.xyz",

                "ABCDEFGH.123456-1-javadoc.xyz",
                "ABCDEFG.123456-1-JAVADOC.xyz",
                "ABCDEFGHI.123456-1-javadoc.xyz",
                "12345678.ABCDEF-1-javadoc.xyz",
                "12345678.ABCDEFG-1-javadoc.xyz",
                "12345678.ABCDE-1-javadoc.xyz",
                "ABCDEFGH.ABCDEF-1-javadoc.xyz",
                "ABCDEFG.ABCDE-1-javadoc.xyz",
                "ABCDEFGHI.ABCDEFG-1-javadoc.xyz",
                "ABCDEFGH.ABCDEFG-1-javadoc.xyz",
                "ABCDEFGG.ABCDEF-1-javadoc.xyz",
                "ABCDEFGH.ABCDE-1-javadoc.xyz",
                "!@#$%^&*.098765-1-javadoc.xyz",

                "ABCDEFGH.123456-1-sources.xyz",
                "ABCDEFG.123456-1-sources.xyz",
                "ABCDEFGHI.123456-1-SOURCES.xyz",
                "12345678.ABCDEF-1-sources.xyz",
                "12345678.ABCDEFG-1-sources.xyz",
                "12345678.ABCDE-1-sources.xyz",
                "ABCDEFGH.ABCDEF-1-sources.xyz",
                "ABCDEFG.ABCDE-1-sources.xyz",
                "ABCDEFGHI.ABCDEFG-1-sources.xyz",
                "ABCDEFGH.ABCDEFG-1-sources.xyz",
                "ABCDEFGG.ABCDEF-1-sources.xyz",
                "ABCDEFGH.ABCDE-1-sources.xyz",
                "!@#$%^&*.098765-1-sources.xyz",

                "README.md",
                "LICENSE.txt",
                "pom.xml",
                "index.html",
                "data.csv"
        );
    }

    @SneakyThrows
    @ParameterizedTest()
    @MethodSource
    public void returns400onInvalidFilenameWhenUploadingInExistingOwnVault(String value) {
        String URI = String.format("/mvn/%s/%s/com/test/test/0.0.1-SNAPSHOT/%s", this.EXISTING_USERNAME, this.EXISTING_VAULT, value);

        when(this.fileWriter.saveFileToDisk(any())).thenReturn(Either.right("dummy written file"));

        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(URI);

        Vault vault = Vault
                .builder()
                .user(this.userRepository.findByUsername(this.EXISTING_USERNAME).get())
                .name(this.EXISTING_VAULT)
                .build();

        this.vaultRepository.save(vault);

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .put(URI)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .content(this.VALID_INPUT.getContent())
                .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USERNAME, this.EXISTING_PASSWORD))
        ).andReturn();

        Assertions.assertEquals(400, mvcResult.getResponse().getStatus());
        Assertions.assertEquals(1, this.vaultRepository.count());
        Assertions.assertEquals(1, this.userRepository.count());
        Assertions.assertEquals(0, this.artefactRepository.count());
    }
}