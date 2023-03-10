import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpHeaders;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.Response;
import org.gradle.tooling.*;
import org.json.*;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.TestExecutionException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import java.nio.file.*;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Skeleton of a ContinuousIntegrationServer which acts as webhook
 * See the Jetty documentation for API documentation of those classes.
 */
public class ContinuousIntegrationServer extends AbstractHandler {
    /**
     * Handles incoming requests to server
     * @param target The target of the request - either a URI or a name.
     * @param baseRequest The original unwrapped request object.
     * @param request The request either as the {@link Request}
     * object or a wrapper of that request. The {@link HttpConnection#getCurrentConnection()}
     * method can be used access the Request object if required.
     * @param response The response as the {@link Response}
     * object or a wrapper of that request. The {@link HttpConnection#getCurrentConnection()}
     * method can be used access the Response object if required.
     * @throws IOException
     */
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException {
        // Get the HTTP method of the request, e.g. "GET" or "POST"
        // The method is "GET" when running the server locally
        // The method is "POST" when a webhook event occurs
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
                response.getWriter().print("Site doesnt exist");
            }
        } else if (method.equals("POST")) {
            // Read the payload from the webhook and convert it to a JSON object
            String pl = request.getReader().lines().collect(Collectors.joining("\n"));
            JSONObject payload = new JSONObject(pl);

            // Add code below to do the CI tasks
            File database = new File("./database/database.json");
            performCI(payload, response,database);

            // Assume that this is needed here as well after everything is done
            baseRequest.setHandled(true);
        }
    }

    /**
     * Performs the CI
     * @param payload the payload from the HTTP request
     * @param response, the response of request
     */
    public void performCI(JSONObject payload, HttpServletResponse response, File database) throws IOException {
        System.out.println(payload);
        // Clone repo
        // Get the specific git ref that triggered the webhook, for example: "refs/heads/main"
        String ref = payload.getString("ref");
        String[] refList = ref.split("/");
        String branch = refList[refList.length-1];
        JSONObject repo = payload.getJSONObject("repository");
        String clone_url = repo.getString("clone_url");
        clone(clone_url, branch);

        // Perform testing
        String buildlogs = checkTests("./local");
        buildlogs = buildlogs.replace("\n", "<br>");
        boolean success = buildlogs.contains("BUILD SUCCESSFUL");

        // Save result
        JSONObject head_commit = payload.getJSONObject("head_commit");
        String commitID = head_commit.getString("id");
        String url = saveBuild(database, commitID, buildlogs);

        // Respond with commit status
        response.setStatus(200);
        String state = success ? "success" : "failure";
        String owner = repo.getJSONObject("owner").getString("login");
        String reponame = repo.getString("name");
        String sha = payload.getString("after");

        JSONObject responsePayload = new JSONObject();
        responsePayload
                .put("state", state)
                .put("target_url", url);

        String postUrl = "https://api.github.com/repos/"+ owner + "/" + reponame + "/statuses/" + sha;

        System.out.println("postUrl: " + postUrl);

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(postUrl);
        StringEntity postingString = new StringEntity(responsePayload.toString());
        post.setEntity(postingString);
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        post.setHeader(HttpHeaders.ACCEPT, "application/vnd.github+json");

        // Read oauth token from file
        String oauth = FileUtils.readFileToString(new File("oauthtoken.secret"));
        post.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + oauth);
        post.setHeader("X-GitHub-Api-Version", "2022-11-28");
        HttpResponse restapiresponse = httpClient.execute(post);

        System.out.println("REST api post request response: " + restapiresponse.toString());

        Path p = Paths.get("./local");
        try {
            FileUtils.deleteDirectory(p.toFile());
        } catch (IOException e) {
            e.printStackTrace();
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
                response.getWriter().println("Build Log: <br>" + res.get("log") + "<br>");
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
    * @return String build result. Will contain "BUILD SUCCESSFUL" if successful build with passed tests.
    * */
    public String checkTests(String projectPath) {
        ProjectConnection connection;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            // Main entry point to the Gradle tooling API
            GradleConnector connector = GradleConnector.newConnector().useDistribution(new URI("https://services.gradle.org/distributions/gradle-7.5.1-bin.zip"));
            File projectDir = new File(projectPath);
            // Connect gradle to project
            connector.forProjectDirectory(projectDir);
            connection = connector.connect();
            BuildLauncher build = connection.newBuild();
            build.forTasks("test");
            build.setStandardOutput(output);
            build.run();
            connection.close();
        } catch (TestExecutionException ex) {
            System.out.println("WARNING: Tests failed.");
        } catch (BuildException ex) {
            System.out.println("WARNING: Build failed.");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        String result = new String(output.toByteArray());
        return  result;
    }

    /**
     * Saves the build information in a persistent database
     * @param commitIdentifier the ID of the commit
     * @param buildLogs Gradle build logs
     * @return url to build
     */
    public static String saveBuild(File database, String commitIdentifier, String buildLogs) {
        String url = "http://188.150.30.242:8090/builds/";
        try {
            String s = FileUtils.readFileToString(database);
            JSONObject db = new JSONObject(s);
            JSONObject build = new JSONObject();
            build.put("commitID", commitIdentifier);
            build.put("log", buildLogs);
            //build.append("timestamp", Timestamp.from(Instant.now()));
            String ID = Timestamp.from(Instant.now()).toString();
            db.put(ID, build);
            url += ID;
            FileWriter writer = new FileWriter(database);
            writer.write(db + "\n");
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return url;
    }

    /**
     * Starts CI server
     * @param args
     * @throws Exception
     */
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
     * This function clones a repo from a URL into a local folder on a specified branch.
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