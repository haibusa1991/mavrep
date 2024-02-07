

import com.personal.microart.persistence.repositories.UserRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Random;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class UploadFileTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private final byte[] FILE_CONTENTS = new byte[128000];

    @BeforeAll
    public void init() {
        new Random().nextBytes(this.FILE_CONTENTS);
    }

    @BeforeEach
    public void setup(){
        long count = this.userRepository.count();
    }

    @Test
    public void returns403onAnonymousUser() {

    }

    @Test
    public void returns403onInvalidCredentials() {

    }

    @Test
    public void returns400onUploadInExistingUnauthorizedVault() {

    }

    @Test
    public void returns200onUploadInExistingAuthorizedVault() {

    }

    @Test
    public void returns200onUploadInExistingOwnVault() {

    }

    @Test
    public void returns200onUploadInNonExistingOwnVault() {

    }

    @Test
    public void returns403onUploadInNonExistingNotOwnVault() {

    }

    @Test
    public void returns400onInvalidFilename() { //multiple bad names to be tested

    }
}