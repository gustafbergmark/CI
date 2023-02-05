import org.gradle.tooling.TestExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ContinuousIntegrationServerTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void checkTestsTrue() {
        ContinuousIntegrationServer ci = new ContinuousIntegrationServer();
        boolean result = ci.checkTests("./", "./src/test/java/checkTestTrue/");
        assertTrue(result);
    }

    @Test
    void checkTestsFalse() {
        ContinuousIntegrationServer ci = new ContinuousIntegrationServer();
        boolean result = ci.checkTests("./", "./src/test/java/checkTestFalse/");
        assertFalse(result);
    }

    @Test
    void checkTestsInvalidInput() {
        ContinuousIntegrationServer ci = new ContinuousIntegrationServer();
        boolean result = ci.checkTests("notAPath", "notAPath");
        assertFalse(result);
    }
}