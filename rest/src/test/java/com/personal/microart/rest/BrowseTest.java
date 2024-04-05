package com.personal.microart.rest;

import com.jayway.jsonpath.JsonPath;
import com.personal.microart.core.auth.jwt.JwtProvider;
import com.personal.microart.core.auth.jwt.Token;
import com.personal.microart.persistence.entities.Artefact;
import com.personal.microart.persistence.entities.BlacklistedJwt;
import com.personal.microart.persistence.entities.MicroartUser;
import com.personal.microart.persistence.entities.Vault;
import com.personal.microart.persistence.directorymanager.FileReader;
import com.personal.microart.persistence.repositories.ArtefactRepository;
import com.personal.microart.persistence.repositories.BlacklistedJwtRepository;
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
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    @Autowired
    private ConversionService conversionService;

    @Autowired
    private BlacklistedJwtRepository blacklistedJwtRepository;

    @MockBean
    private FileReader fileReader;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private JwtProvider jwtProvider;

    @MockBean
    private ExchangeAccessor exchangeAccessor;

    private final byte[] FILE_CONTENTS = new byte[1024];

    private final String EXISTING_EMAIL_1 = "test@test";
    private final String EXISTING_USERNAME_1 = "test-user1";
    private final String EXISTING_PASSWORD_1 = "testpass";

    private final String EXISTING_EMAIL_2 = "test2@test";
    private final String EXISTING_USERNAME_2 = "test-user2";
    private final String EXISTING_PASSWORD_2 = "testpass2";

    private final String EXISTING_EMAIL_3 = "test3@test";
    private final String EXISTING_USERNAME_3 = "test-user3";
    private final String EXISTING_PASSWORD_3 = "testpass3";

    private final String EXISTING_VAULT_1 = "test-vault-1";
    private final String EXISTING_VAULT_2 = "test-vault-2";
    private final String EXISTING_VAULT_3 = "test-vault-3";

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

    private final MicroartUser EXISTING_USER_3 = MicroartUser
            .builder()
            .email(EXISTING_EMAIL_3)
            .username(EXISTING_USERNAME_3)
            .password(this.passwordEncoder.encode(EXISTING_PASSWORD_3))
            .build();

    @BeforeAll
    public void init() {
        new Random().nextBytes(this.FILE_CONTENTS);
    }

    @BeforeEach
    public void setup() {
        this.userRepository.save(this.EXISTING_USER_1);
        this.userRepository.save(this.EXISTING_USER_2);
        this.userRepository.save(this.EXISTING_USER_3);

        Vault vault1 = Vault
                .builder()
                .name(this.EXISTING_VAULT_1)
                .owner(this.EXISTING_USER_1)
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
                .owner(this.EXISTING_USER_1)
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
                .owner(this.EXISTING_USER_2)
                .build();

        vault3.isPublic(false);

        Artefact artefact3 = this.artefactRepository.save(Artefact
                .builder()
                .uri(String.format("/mvn/%s/%s/com/test/download/0.0.1-SNAPSHOT/test-12345678.123456-1.jar", this.EXISTING_USERNAME_2, this.EXISTING_VAULT_3))
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
        this.blacklistedJwtRepository.deleteAll();
    }

    private String getAuthHeaderValue(MicroartUser user) {
        return "Bearer " + this.conversionService.convert(this.jwtProvider.getJwt(user), String.class);
    }

    @SneakyThrows
    @Test
    public void returnsListOfAllPublicVaultsWithCorrectUrisWhenUserIsAnonymous() {
        this.mockMvc.perform(get("/browse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value(this.EXISTING_USERNAME_1))
                .andExpect(jsonPath("$.content[0].uri").value(String.format("/browse/%s", this.EXISTING_USERNAME_1)));
    }

    @SneakyThrows
    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "Bearer",
            "Bearer ",
            "Bearer asdfsdfsdfs",
            "Basic",
            "Basic ",
            "Basic asdfs"
    })
    public void returnsListOfAllPublicVaultsWithCorrectUrisWhenAuthHeaderInvalid(String headerValue) { //non-existing = invalid credentials
        this.mockMvc.perform(get("/browse")
                        .header(HttpHeaders.AUTHORIZATION, headerValue))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value(this.EXISTING_USERNAME_1))
                .andExpect(jsonPath("$.content[0].uri").value(String.format("/browse/%s", this.EXISTING_USERNAME_1)));
    }

    @SneakyThrows
    @Test
    public void exploresPublicVaultAndDownloadsFileWhenUserIsAnonymous() {
        when(this.fileReader.readFile(any())).thenReturn(Either.right(this.FILE_CONTENTS));

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

            //if this is the bottom of the repository, replace 'browse' with 'mvn' and download the file. Kinda ugly.
            if (uri.contains("/browse"))
                Assertions.assertEquals(result.getRequest().getRequestURI() + "/" + name, uri);
            else {
                Assertions.assertEquals(result.getRequest().getRequestURI().replace("/browse", "/mvn") + "/" + name, uri);
                mockMvc.perform(MockMvcRequestBuilders
                                .get(uri))
                        .andExpect(status().isOk())
                        .andExpect(content().bytes(this.FILE_CONTENTS));
            }
        }
    }

    @SneakyThrows
    @Test
    public void exploresPublicVaultAndDownloadsFileWhenJwtBlacklisted() {
        when(this.fileReader.readFile(any())).thenReturn(Either.right(this.FILE_CONTENTS));

        String authHeaderValue = this.getAuthHeaderValue(this.EXISTING_USER_1);
        Token token = this.jwtProvider.getJwt(authHeaderValue);

        BlacklistedJwt blacklistedJwt = BlacklistedJwt
                .builder()
                .token(authHeaderValue.substring(7))
                .validity(token.getExp().atOffset(ZoneOffset.UTC).toLocalDateTime())
                .build();

        this.blacklistedJwtRepository.save(blacklistedJwt);

        MvcResult result;
        String uri = "/browse";

        while (!uri.contains("/mvn")) {
            result = this.mockMvc.perform(get(uri)
                            .header(HttpHeaders.AUTHORIZATION, authHeaderValue))
                    .andExpect(status().isOk())
                    .andReturn();

            String content = result.getResponse().getContentAsString();

            uri = JsonPath.parse(content).read("$.content[0].uri");
            String name = JsonPath.parse(content).read("$.content[0].name");
            String[] uriElements = uri.split("/");

            Assertions.assertEquals(uriElements[uriElements.length - 1], name);
            Assertions.assertEquals((Integer) 1, JsonPath.read(result.getResponse().getContentAsString(), "$.content.length()"));

            //if this is the bottom of the repository, replace 'browse' with 'mvn' and download the file. Kinda ugly.
            if (uri.contains("/browse"))
                Assertions.assertEquals(result.getRequest().getRequestURI() + "/" + name, uri);
            else {
                Assertions.assertEquals(result.getRequest().getRequestURI().replace("/browse", "/mvn") + "/" + name, uri);
                mockMvc.perform(MockMvcRequestBuilders
                                .get(uri)
                                .header(HttpHeaders.AUTHORIZATION, authHeaderValue))
                        .andExpect(status().isOk())
                        .andExpect(content().bytes(this.FILE_CONTENTS));
            }
        }
    }

    @SneakyThrows
    @Test
    public void exploresPublicVaultAndDownloadsFileWhenUserIsNonExisting() {
        when(this.fileReader.readFile(any())).thenReturn(Either.right(this.FILE_CONTENTS));

        MvcResult result;
        String uri = "/browse";

        while (!uri.contains("/mvn")) {
            result = this.mockMvc.perform(get(uri)
                            .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(MicroartUser.empty())))
                    .andExpect(status().isOk())
                    .andReturn();

            String content = result.getResponse().getContentAsString();

            uri = JsonPath.parse(content).read("$.content[0].uri");
            String name = JsonPath.parse(content).read("$.content[0].name");
            String[] uriElements = uri.split("/");

            Assertions.assertEquals(uriElements[uriElements.length - 1], name);
            Assertions.assertEquals((Integer) 1, JsonPath.read(result.getResponse().getContentAsString(), "$.content.length()"));

            //if this is the bottom of the repository, replace 'browse' with 'mvn' and download the file. Kinda ugly.
            if (uri.contains("/browse"))
                Assertions.assertEquals(result.getRequest().getRequestURI() + "/" + name, uri);
            else {
                Assertions.assertEquals(result.getRequest().getRequestURI().replace("/browse", "/mvn") + "/" + name, uri);
                mockMvc.perform(MockMvcRequestBuilders
                                .get(uri))
                        .andExpect(status().isOk())
                        .andExpect(content().bytes(this.FILE_CONTENTS));
            }
        }
    }

    @SneakyThrows
    @Test
    public void returnsListOfAllPublicVaultsWithCorrectUrisWhenUserIsAuthorized() {
        this.mockMvc.perform(get("/browse")
                        .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USER_3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].name").value(this.EXISTING_USERNAME_1))
                .andExpect(jsonPath("$.content[0].uri").value(String.format("/browse/%s", this.EXISTING_USERNAME_1)));
    }

    @Test
    @SneakyThrows
    public void exploresPublicVaultAndDownloadsFileWhenUserIsAuthorized() {
        when(this.fileReader.readFile(any())).thenReturn(Either.right(this.FILE_CONTENTS));

        MvcResult result;
        String uri = "/browse";

        while (!uri.contains("/mvn")) {
            result = this.mockMvc.perform(get(uri)
                            .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USER_3)))
                    .andExpect(status().isOk())
                    .andReturn();

            String content = result.getResponse().getContentAsString();

            uri = JsonPath.parse(content).read("$.content[0].uri");
            String name = JsonPath.parse(content).read("$.content[0].name");
            String[] uriElements = uri.split("/");

            Assertions.assertEquals(uriElements[uriElements.length - 1], name);
            Assertions.assertEquals((Integer) 1, JsonPath.read(result.getResponse().getContentAsString(), "$.content.length()"));

            //if this is the bottom of the repository, replace 'browse' with 'mvn' and download the file. Kinda ugly.
            if (uri.contains("/browse"))
                Assertions.assertEquals(result.getRequest().getRequestURI() + "/" + name, uri);
            else {
                Assertions.assertEquals(result.getRequest().getRequestURI().replace("/browse", "/mvn") + "/" + name, uri);
                mockMvc.perform(get(uri)
                                .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USER_3)))
                        .andExpect(status().isOk())
                        .andExpect(content().bytes(this.FILE_CONTENTS));
            }
        }
    }

    @SneakyThrows
    @Test()
    public void returnsListOfAllPublicAndAuthorizedVaultsWithCorrectUrisWhenUserIsAuthorized() {
        this.mockMvc.perform(get("/browse")
                        .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USER_2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value(this.EXISTING_USERNAME_1))
                .andExpect(jsonPath("$.content[0].uri").value(String.format("/browse/%s", this.EXISTING_USERNAME_1)))
                .andExpect(jsonPath("$.content[1].name").value(this.EXISTING_USERNAME_2))
                .andExpect(jsonPath("$.content[1].uri").value(String.format("/browse/%s", this.EXISTING_USERNAME_2)));
    }

    @SneakyThrows
    @Test
    public void exploresPublicAndAuthorizedVaultAndDownloadsFileWhenUserIsAuthorized() {
        when(this.fileReader.readFile(any())).thenReturn(Either.right(this.FILE_CONTENTS));
        this.vaultRepository.delete(this.vaultRepository.findVaultByName(this.EXISTING_VAULT_1).get());
        this.vaultRepository.delete(this.vaultRepository.findVaultByName(this.EXISTING_VAULT_3).get());

        MvcResult result;
        String uri = "/browse";

        while (!uri.contains("/mvn")) {
            result = this.mockMvc.perform(get(uri)
                            .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USER_2)))
                    .andExpect(status().isOk())
                    .andReturn();

            String content = result.getResponse().getContentAsString();

            uri = JsonPath.parse(content).read("$.content[0].uri");
            String name = JsonPath.parse(content).read("$.content[0].name");
            String[] uriElements = uri.split("/");

            Assertions.assertEquals(uriElements[uriElements.length - 1], name);
            Assertions.assertEquals((Integer) 1, JsonPath.read(result.getResponse().getContentAsString(), "$.content.length()"));

            //if this is the bottom of the repository, replace 'browse' with 'mvn' and download the file. Kinda ugly.
            if (uri.contains("/browse"))
                Assertions.assertEquals(result.getRequest().getRequestURI() + "/" + name, uri);
            else {
                HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
                when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
                when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
                when(httpServerExchange.getRequestURI()).thenReturn(result.getRequest().getRequestURI().replace("/browse", "/mvn") + "/" + name, uri);

                Assertions.assertEquals(result.getRequest().getRequestURI().replace("/browse", "/mvn") + "/" + name, uri);
                mockMvc.perform(get(uri)
                                .header(HttpHeaders.AUTHORIZATION, this.getAuthHeaderValue(this.EXISTING_USER_2)))
                        .andExpect(status().isOk())
                        .andExpect(content().bytes(this.FILE_CONTENTS));
            }
        }
    }

    @SneakyThrows
    @Test
    public void returns403whenJwtBlacklistedAndDownloadingFromAuthorizedVault() {
        when(this.fileReader.readFile(any())).thenReturn(Either.right(this.FILE_CONTENTS));
        String uri = this.artefactRepository.findAllByFilename("file2").stream().findFirst().get().getUri();

        HttpServerExchange httpServerExchange = mock(HttpServerExchange.class);
        when(this.exchangeAccessor.getExchange(any())).thenReturn(httpServerExchange);
        when(httpServerExchange.setReasonPhrase(any(String.class))).thenReturn(httpServerExchange);
        when(httpServerExchange.getRequestURI()).thenReturn(uri);


        String authHeaderValue = this.getAuthHeaderValue(this.EXISTING_USER_2);
        Token token = this.jwtProvider.getJwt(authHeaderValue);

        BlacklistedJwt blacklistedJwt = BlacklistedJwt
                .builder()
                .token(authHeaderValue.substring(7))
                .validity(token.getExp().atOffset(ZoneOffset.UTC).toLocalDateTime())
                .build();

        this.blacklistedJwtRepository.save(blacklistedJwt);

        mockMvc.perform(get(uri)
                        .header(HttpHeaders.AUTHORIZATION, authHeaderValue))
                .andExpect(status().isForbidden());
    }
}