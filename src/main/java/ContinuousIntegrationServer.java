import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;
import org.json.*;
import java.util.Calendar;

import java.io.File;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.TestExecutionException;
import org.gradle.tooling.TestLauncher;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.CloneCommand;
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

            response.setStatus(200);

            //1. Clone project
            //2. Build project (?)
            //3. Run tests

            boolean passed = true;
            String state = "";
            if(passed) {
                // Set state to success
                state = "success";
            } else {
                // Set state to fail
                state = "failure";
            }

            Date date = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
            String strDate = dateFormat.format(date);

            //JSONObject repo = new JSONObject(payload.get("repository"));
            //String url = repo.getString("full_name");

            JSONObject responsePayload = new JSONObject();
            responsePayload
                    .append("url", "url")
                    .append("state", state)
                    .append("target_url", "target_url")
                    .append("created_at", strDate);

            // Assume that this is needed here as well after everything is done
            baseRequest.setHandled(true);

            //
            /*
            * {
                "url": "https://github.com/gustafbergmark/CI.git/",
                "state": "success",  "failure"
                "target_url": "https://ci.example.com/1000/output",
                "created_at": "2012-07-20T01:19:13Z"
            *
            * */
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

    /**
     * Inspiration for the cloning implementation was taken from https://onecompiler.com/posts/3sqk5x3td/how-to-clone-a-git-repository-programmatically-using-java,
     * Dependencies for GitApi was added to the settings in order for this to work.
     * This function clones a repo from an URL into a local folder on a specified branch.
     * @param repoUrl
     * @param branch
     */
    public static void clone(String repoUrl, String branch) {
        Path p = Paths.get("./local");

        try {
            System.out.println("Cloning " + repoUrl + " into local folder" + " at branch " + branch);
            CloneCommand gc = Git.cloneRepository().setURI(repoUrl).setDirectory(p.toFile());
            // check if branch is in master, otherwise switch branch
            if(branch == "master"){
                gc.call();
            }
            else{
                gc.setBranch(branch);
                gc.call();
            }
            System.out.println("Completed cloning at branch " + branch);
        } catch (GitAPIException e) {
            System.out.println("Exception occurred with GitAPI when cloning repo");
            e.printStackTrace();
        }
    }
}