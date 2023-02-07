import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.*;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.json.*;

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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Iterator;

/**
 * Skeleton of a ContinuousIntegrationServer which acts as webhook
 * See the Jetty documentation for API documentation of those classes.
 */
public class ContinuousIntegrationServer extends AbstractHandler {
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {
        // Get the HTTP method of the request, e.g. "GET" or "POST"
        String method = request.getMethod();

        if (method.equals("GET")) {
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(HttpServletResponse.SC_OK);
            baseRequest.setHandled(true);

            System.out.println(target);

            // Simple code for viewing build history
            File database = new File("./database/database.json");
            if (target.equals("/builds.html")) {
                getAllBuilds(database, response);
            } else if (target.startsWith("/builds/")) {
                printBuild(database, target.substring(8), response);
            } else {
                response.getWriter().println("Site doesnt exist");
            }
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
    }

    /**
     * Responds to requests to /builds.html, showing a list with links to all builds
     * @param database which database to use
     * @param response for outputting data to the client
     */
    public void getAllBuilds(File database, HttpServletResponse response) {
        try {
            String s = FileUtils.readFileToString(database);
            JSONObject db = new JSONObject(s);
            Iterator<String> keys = db.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                response.getWriter().println("<a href=\"builds/" + key + "\">" + key + "</a>");
                response.getWriter().println(db.getJSONObject(key).get("commitID"));
                response.getWriter().println("<br>");
            }
            response.getWriter().flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Responds to requests for specific builds
     * @param database which database to use
     * @param buildID which specific build to display
     * @param response for outputting the data to the client
     */
    public void printBuild(File database, String buildID, HttpServletResponse response) {
        try {
            String s = FileUtils.readFileToString(database);
            JSONObject db = new JSONObject(s);
            try {
                JSONObject res = db.getJSONObject(buildID);
                response.getWriter().println("Timestamp: " + buildID + "<br>");
                response.getWriter().println("CommitID: " + res.get("commitID") + "<br>");
                response.getWriter().println("Build Log: " + res.get("log") + "<br>");
            } catch (JSONException e) {
                response.getWriter().println("Build does not exist");
            }
            response.getWriter().flush();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    /**
     * Saves the build information in a persistent database
     *
     * @param commitIdentifier
     * @param buildLogs
     */
    public static void saveBuild(File database, String commitIdentifier, String buildLogs) {
        try {
            String s = FileUtils.readFileToString(database);
            JSONObject db = new JSONObject(s);
            JSONObject build = new JSONObject();
            build.put("commitID", commitIdentifier);
            build.put("log", buildLogs);
            //build.append("timestamp", Timestamp.from(Instant.now()));
            db.put(Timestamp.from(Instant.now()).toString(), build);
            FileWriter writer = new FileWriter(database);
            writer.write(db + "\n");
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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