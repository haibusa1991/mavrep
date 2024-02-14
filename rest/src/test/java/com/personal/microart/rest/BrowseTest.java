package com.personal.microart.rest;

import com.jayway.jsonpath.JsonPath;
import com.personal.microart.persistence.entities.Artefact;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.filehandler.FileWriter;
import com.personal.microart.persistence.repositories.ArtefactRepository;
import com.personal.microart.persistence.repositories.UserRepository;
import com.personal.microart.persistence.repositories.VaultRepository;
import com.personal.microart.rest.controllers.ExchangeAccessor;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Random;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureWebTestClient
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class BrowseTest {

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

    private final String EXISTING_EMAIL_1 = "test@test";
    private final String EXISTING_USERNAME_1 = "test-user1";
    private final String EXISTING_PASSWORD_1 = "testpass";

    private final String EXISTING_EMAIL_2 = "test2@test";
    private final String EXISTING_USERNAME_2 = "test-user2";
    private final String EXISTING_PASSWORD_2 = "testpass2";

    private final String EXISTING_VAULT_1 = "test-vault-1";
    private final String EXISTING_VAULT_2 = "test-vault-2";
    private final String EXISTING_VAULT_3 = "test-vault-3";

    private final String INVALID_CREDENTIALS = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJlbWFpbCI6IjJAMiIsInVzZXJuYW1lIjoicGFjZWsyIiwiaWF0IjoxNzA3NDgxODA1LCJleHAiOjE3MTAwNzM4MDV9.CKY1ZxOfrgO7ftpDR8d6PW9EmInOmnWgoYRZaoqlB8k";

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

    @BeforeAll
    public void init() {
        new Random().nextBytes(this.FILE_CONTENTS);
    }

    @BeforeEach
    public void setup() {
        this.userRepository.save(this.EXISTING_USER_1);
        this.userRepository.save(this.EXISTING_USER_2);

        Vault vault1 = Vault
                .builder()
                .name(this.EXISTING_VAULT_1)
                .user(this.EXISTING_USER_1)
                .build();
        Artefact artefact1 = this.artefactRepository.save(Artefact
                .builder()
                .uri(String.format("/mvn/%s/%s/com/test/download/0.0.1-SNAPSHOT/test-12345678.123456-1.jar", this.EXISTING_USERNAME_1, this.EXISTING_VAULT_1))
                .filename("file1")
                .build());
        vault1.addArtefact(artefact1);

        Vault vault2 = Vault
                .builder()
                .name(this.EXISTING_VAULT_2)
                .user(this.EXISTING_USER_1)
                .build();

        vault2.isPublic(false);
        vault2.addUser(this.EXISTING_USER_2);

        Artefact artefact2 = this.artefactRepository.save(Artefact
                .builder()
                .uri(String.format("/mvn/%s/%s/com/test/download/0.0.1-SNAPSHOT/test-12345678.123456-1.jar", this.EXISTING_USERNAME_1, this.EXISTING_VAULT_2))
                .filename("file2")
                .build());
        vault2.addArtefact(artefact2);


        Vault vault3 = Vault
                .builder()
                .name(this.EXISTING_VAULT_3)
                .user(this.EXISTING_USER_2)
                .build();

        vault3.isPublic(false);

        Artefact artefact3 = this.artefactRepository.save(Artefact
                .builder()
                .uri(String.format("/mvn/%s/%s/com/test/download/0.0.1-SNAPSHOT/test-12345678.123456-1.jar", this.EXISTING_USERNAME_1, this.EXISTING_VAULT_3))
                .filename("file3")
                .build());

        vault3.addArtefact(artefact3);

        this.vaultRepository.saveAll(List.of(vault1, vault2, vault3));
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

    @SneakyThrows
    @Test
    public void returnsListOfAllPublicVaultsWithCorrectUrisWhenUserIsAnonymous() {

        MvcResult result;
        String uri = "/browse";

        while (!uri.contains("/mvn")) {
            result = this.mockMvc.perform(get(uri))
                    .andExpect(status().isOk())
                    .andReturn();

            String content = result.getResponse().getContentAsString();

            uri = JsonPath.parse(content).read("$.content[0].uri");
            String name = JsonPath.parse(content).read("$.content[0].name");
            String[] uriElements = uri.split("/");

            Assertions.assertEquals(uriElements[uriElements.length - 1], name);
            Assertions.assertEquals((Integer) 1, JsonPath.read(result.getResponse().getContentAsString(), "$.content.length()"));
            Assertions.assertEquals(result.getRequest().getRequestURI() + "/" + name, uri);
        }

        System.out.println();

    }

    @Test
    public void returnsListOfAllPublicVaultsWithCorrectUrisWhenUserIsNonExistent() { //non-existing = invalid credentials
//TODO: Implement me
    }

    @Test
    public void exploresPublicVaultAndDownloadsFileWhenUserIsAnonymous() {
//TODO: Implement me
    }

    @Test
    public void exploresPublicVaultAndDownloadsFileWhenUserIsNonExisting() {
//TODO: Implement me
    }

    @Test
    public void returnsListOfAllPublicVaultsWithCorrectUrisWhenUserIsAuthorized() {
//TODO: Implement me
    }

    @Test
    public void exploresPublicVaultAndDownloadsFileWhenUserIsAuthorized() {
//TODO: Implement me
    }

    @Test
    public void returnsListOfAllPublicAndAuthorizedVaultsWithCorrectUrisWhenUserIsAuthorized() {
//TODO: Implement me
    }

    @Test
    public void exploresPublicAndAuthorizedVaultAndDownloadsFileWhenUserIsAuthorized() {
//TODO: Implement me
    }
}