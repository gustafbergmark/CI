import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.io.FileUtils;


class ContinuousIntegrationServerTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testClonePublic() {
        String repoUrl = "https://github.com/gustafbergmark/CI.git";
        ContinuousIntegrationServer.clone(repoUrl);
        Path p = Paths.get("./local");
        try {
            FileUtils.deleteDirectory(p.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}