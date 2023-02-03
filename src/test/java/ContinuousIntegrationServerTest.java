import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


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
        String branch = "master";
        ContinuousIntegrationServer.clone(repoUrl, branch);
        Path p = Paths.get("./local");
        try {
            FileUtils.deleteDirectory(p.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    void testClonePublicBranch() {
        String repoUrl = "https://github.com/gustafbergmark/CI.git";
        String branch = "2-implement-cloning-from-github";
        ContinuousIntegrationServer.clone(repoUrl, branch);
        Path p = Paths.get("./local");

        try {
            FileUtils.deleteDirectory(p.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}