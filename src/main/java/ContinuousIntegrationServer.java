import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;

import java.util.stream.Collectors;
import org.json.*;

import java.io.File;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.TestExecutionException;
import org.gradle.tooling.TestLauncher;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import java.nio.file.*;
/**
 Skeleton of a ContinuousIntegrationServer which acts as webhook
 See the Jetty documentation for API documentation of those classes.
 */
public class ContinuousIntegrationServer extends AbstractHandler
{
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException
    {
        // Get the HTTP method of the request, e.g. "GET" or "POST"
        String method = request.getMethod();

        if (method.equals("GET")) {
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);

            System.out.println(target);

            response.getWriter().print("CI job done");
        } else if (method.equals("POST")) {
            // Read the payload from the webhook and convert it to a JSON object
            String pl = request.getReader().lines().collect(Collectors.joining("\n"));
            JSONObject payload = new JSONObject(pl);
            // Get the specific git ref that triggered the webhook, for example: "refs/heads/main"
            String ref = payload.getString("ref");
            // Add code below to do the CI tasks

            // Assume that this is needed here as well after everything is done
            baseRequest.setHandled(true);

        }
        // Don't know if there will be any other HTTP methods than "GET" or "POST"
        // The method is "GET" when running the server locally
        // The method is "POST" when a webhook event occurs
    }

    /**
    * Run tests using Gradle Tooling API.
    * @param projectPath: path to project repo to run CI on
    * @param testDirPath: path to test folder
    * @return boolean result. Returns true if all given tests pass, otherwise returns false.
    * */
    public boolean checkTests(String projectPath, String testDirPath) {
        ProjectConnection connection;
        try {
            // Main entry point to the Gradle tooling API
            GradleConnector connector = GradleConnector.newConnector();

            File projectDir = new File(projectPath);
            File testDir = new File(testDirPath);
            File[] testFiles = testDir.listFiles();

            // Connect gradle to project
            connector.forProjectDirectory(projectDir);
            connection = connector.connect();

            TestLauncher launcher = connection.newTestLauncher();

            if(testFiles != null) {  // Check that test files exists
                for (File testFile : testFiles) {
                    if (!testFile.getName().endsWith(".java")) {continue;} //Skip folders

                    // Adds test file to launcher
                    String fileName = testFile.getName().replaceFirst("[.][^.]+$", "");
                    launcher = launcher.withJvmTestClasses(fileName);

                    // Run all tests in given test file
                    launcher.run();
                }
            } else {
                System.out.println("No test files to run.");
                return false;
            }
            connection.close();
        } catch (TestExecutionException ex) {
            System.out.println("Tests failed.");
            ex.printStackTrace();
            return false;
        }
        System.out.println("Tests passed.");
        return true;
    }

    // used to start the CI server in command line
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }

    public static void clone(String repoUrl) {
        Path p = Paths.get("./local");
        try {
            System.out.println("Cloning "+repoUrl+" into "+repoUrl);
            Git.cloneRepository()
                    .setURI(repoUrl)
                    .setDirectory(p.toFile())
                    .call();
            System.out.println("Completed Cloning");
        } catch (GitAPIException e) {
            System.out.println("Exception occurred while cloning repo");
            e.printStackTrace();
        }
    }
}