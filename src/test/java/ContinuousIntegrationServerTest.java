import org.gradle.tooling.TestExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.mockito.Mockito;

import java.io.PrintWriter;
import java.io.StringWriter;

class ContinuousIntegrationServerTest {

    @BeforeEach
    void setUp() {
    }

    @AfterEach
    void tearDown() {
    }

    /**
     Tests that the handle method works as it should when the request method is "GET"
     */
    @Test
    public void testHandleGet() throws Exception {
        ContinuousIntegrationServer handler = new ContinuousIntegrationServer();
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Request baseRequest = Mockito.mock(Request.class);
        Mockito.when(request.getMethod()).thenReturn("GET");
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        Mockito.when(response.getWriter()).thenReturn(printWriter);

        handler.handle("", baseRequest, request, response);
        assertEquals("CI job done", stringWriter.toString());
        // Test that "setContentType" and "setStatus are called with the correct arguments
        Mockito.verify(response).setContentType("text/html;charset=utf-8");
        Mockito.verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    // TODO: Add test to assert that the handle function works as it should when the request method is "POST".
    // Do that when that part of the code is done
  
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