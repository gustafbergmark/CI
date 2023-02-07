import org.gradle.tooling.TestExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import org.apache.commons.io.FileUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

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

    @Test
    void testSaveBuild() {
        String commitID = "abc124";
        String log = "Build Successful";
        File database = new File("./database/testsave.json");
        FileWriter writer = null;
        try {
            // Create empty database
            writer = new FileWriter(database);
            writer.write("{}");
            writer.close();

            ContinuousIntegrationServer.saveBuild(database, commitID, log);

            // Delete database
            FileUtils.delete(database);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGetAllBuilds() {
        ContinuousIntegrationServer server = new ContinuousIntegrationServer();
        File database = new File("./database/databasetest.json");
        HttpServletResponse response = new HttpServletResponse() {
            @Override
            public void addCookie(Cookie cookie) {

            }

            @Override
            public boolean containsHeader(String name) {
                return false;
            }

            @Override
            public String encodeURL(String url) {
                return null;
            }

            @Override
            public String encodeRedirectURL(String url) {
                return null;
            }

            @Override
            public String encodeUrl(String url) {
                return null;
            }

            @Override
            public String encodeRedirectUrl(String url) {
                return null;
            }

            @Override
            public void sendError(int sc, String msg) throws IOException {

            }

            @Override
            public void sendError(int sc) throws IOException {

            }

            @Override
            public void sendRedirect(String location) throws IOException {

            }

            @Override
            public void setDateHeader(String name, long date) {

            }

            @Override
            public void addDateHeader(String name, long date) {

            }

            @Override
            public void setHeader(String name, String value) {

            }

            @Override
            public void addHeader(String name, String value) {

            }

            @Override
            public void setIntHeader(String name, int value) {

            }

            @Override
            public void addIntHeader(String name, int value) {

            }

            @Override
            public void setStatus(int sc) {

            }

            @Override
            public void setStatus(int sc, String sm) {

            }

            @Override
            public String getCharacterEncoding() {
                return null;
            }

            @Override
            public String getContentType() {
                return null;
            }

            @Override
            public ServletOutputStream getOutputStream() throws IOException {
                return null;
            }

            @Override
            public PrintWriter getWriter() throws IOException {
                return new PrintWriter(OutputStream.nullOutputStream());
            }

            @Override
            public void setCharacterEncoding(String charset) {

            }

            @Override
            public void setContentLength(int len) {

            }

            @Override
            public void setContentType(String type) {

            }

            @Override
            public void setBufferSize(int size) {

            }

            @Override
            public int getBufferSize() {
                return 0;
            }

            @Override
            public void flushBuffer() throws IOException {

            }

            @Override
            public void resetBuffer() {

            }

            @Override
            public boolean isCommitted() {
                return false;
            }

            @Override
            public void reset() {

            }

            @Override
            public void setLocale(Locale loc) {

            }

            @Override
            public Locale getLocale() {
                return null;
            }
        };
        server.getAllBuilds(database, response);
    }

    @Test
    void testPrintBuild() {
        ContinuousIntegrationServer server = new ContinuousIntegrationServer();
        File database = new File("./database/databasetest.json");
        HttpServletResponse response = new HttpServletResponse() {
            @Override
            public void addCookie(Cookie cookie) {

            }

            @Override
            public boolean containsHeader(String name) {
                return false;
            }

            @Override
            public String encodeURL(String url) {
                return null;
            }

            @Override
            public String encodeRedirectURL(String url) {
                return null;
            }

            @Override
            public String encodeUrl(String url) {
                return null;
            }

            @Override
            public String encodeRedirectUrl(String url) {
                return null;
            }

            @Override
            public void sendError(int sc, String msg) throws IOException {

            }

            @Override
            public void sendError(int sc) throws IOException {

            }

            @Override
            public void sendRedirect(String location) throws IOException {

            }

            @Override
            public void setDateHeader(String name, long date) {

            }

            @Override
            public void addDateHeader(String name, long date) {

            }

            @Override
            public void setHeader(String name, String value) {

            }

            @Override
            public void addHeader(String name, String value) {

            }

            @Override
            public void setIntHeader(String name, int value) {

            }

            @Override
            public void addIntHeader(String name, int value) {

            }

            @Override
            public void setStatus(int sc) {

            }

            @Override
            public void setStatus(int sc, String sm) {

            }

            @Override
            public String getCharacterEncoding() {
                return null;
            }

            @Override
            public String getContentType() {
                return null;
            }

            @Override
            public ServletOutputStream getOutputStream() throws IOException {
                return null;
            }

            @Override
            public PrintWriter getWriter() throws IOException {
                return new PrintWriter(OutputStream.nullOutputStream());
            }

            @Override
            public void setCharacterEncoding(String charset) {

            }

            @Override
            public void setContentLength(int len) {

            }

            @Override
            public void setContentType(String type) {

            }

            @Override
            public void setBufferSize(int size) {

            }

            @Override
            public int getBufferSize() {
                return 0;
            }

            @Override
            public void flushBuffer() throws IOException {

            }

            @Override
            public void resetBuffer() {

            }

            @Override
            public boolean isCommitted() {
                return false;
            }

            @Override
            public void reset() {

            }

            @Override
            public void setLocale(Locale loc) {

            }

            @Override
            public Locale getLocale() {
                return null;
            }
        };
        server.printBuild(database, "2023-02-06 14:20:24.596290068", response);
    }
}