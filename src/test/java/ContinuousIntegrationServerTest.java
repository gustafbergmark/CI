import org.gradle.tooling.TestExecutionException;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
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
        Path p = Paths.get("./local");
        try {
            FileUtils.deleteDirectory(p.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void tearDown() {
        Path p = Paths.get("./local");
        try {
            FileUtils.deleteDirectory(p.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tests that the handle method works as it should when the request method is "GET"
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
        assertEquals("Site doesnt exist", stringWriter.toString());
        // Test that "setContentType" and "setStatus are called with the correct arguments
        Mockito.verify(response).setContentType("text/html;charset=utf-8");
        Mockito.verify(response).setStatus(HttpServletResponse.SC_OK);
    }

    @Test
    void checkTestsTrue() {
        ContinuousIntegrationServer ci = new ContinuousIntegrationServer();
        ContinuousIntegrationServer.clone("https://github.com/gustafbergmark/CITest.git", "master");
        String result = ci.checkTests("./local");
        //System.out.println("ANSWER:\n" + result + "END");
        assertTrue(result.contains("BUILD SUCCESSFUL"));
    }

    @Test
    void checkTestsFalse() {
        ContinuousIntegrationServer ci = new ContinuousIntegrationServer();
        ContinuousIntegrationServer.clone("https://github.com/gustafbergmark/CITest.git", "false");
        String result = ci.checkTests("./local");
        //System.out.println("ANSWER:\n" + result + "END");
        assertFalse(result.contains("BUILD SUCCESSFUL"));
    }

    /*@Test
    void checkTestsInvalidInput() {
        ContinuousIntegrationServer ci = new ContinuousIntegrationServer();
        boolean result = ci.checkTests("notAPath", "notAPath");
        assertFalse(result);
    }*/

    @Test
    void testClonePublic() {
        String repoUrl = "https://github.com/gustafbergmark/CITest.git";
        String branch = "master";
        ContinuousIntegrationServer.clone(repoUrl, branch);

    }

    @Test
    void testClonePublicBranch() {
        String repoUrl = "https://github.com/gustafbergmark/CITest.git";
        String branch = "false";
        ContinuousIntegrationServer.clone(repoUrl, branch);

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
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        PrintWriter writer = new PrintWriter(OutputStream.nullOutputStream());
        try {
            Mockito.when(response.getWriter()).thenReturn(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.getAllBuilds(database, response);
    }

    @Test
    void testPrintBuild() {
        ContinuousIntegrationServer server = new ContinuousIntegrationServer();
        File database = new File("./database/databasetest.json");
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        PrintWriter writer = new PrintWriter(OutputStream.nullOutputStream());
        try {
            Mockito.when(response.getWriter()).thenReturn(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        server.printBuild(database, "2023-02-06 14:20:24.596290068", response);
    }

    /*@Test
    void testPerformCI() {
        ContinuousIntegrationServer server = new ContinuousIntegrationServer();
        // Test input is old push webhook
        JSONObject input = new JSONObject("{\"pusher\":{\"name\":\"gustafbergmark\",\"email\":\"gustaf.bergmark@gmail.com\"},\"compare\":\"https://github.com/gustafbergmark/CITest/compare/e586a0bff0ab...f4b67c56251e\",\"head_commit\":{\"committer\":{\"name\":\"GitHub\",\"email\":\"noreply@github.com\",\"username\":\"web-flow\"},\"removed\":[],\"tree_id\":\"b689ebd8826165f68271fc2a19bbbdbc3372c7a1\",\"added\":[],\"author\":{\"name\":\"Gustaf\",\"email\":\"gbergmar@kth.se\",\"username\":\"gustafbergmark\"},\"distinct\":true,\"modified\":[\"README.md\"],\"id\":\"f4b67c56251efa0e617e364b166da70546ae60f8\",\"message\":\"Update README.md\",\"url\":\"https://github.com/gustafbergmark/CITest/commit/f4b67c56251efa0e617e364b166da70546ae60f8\",\"timestamp\":\"2023-02-08T14:06:59+01:00\"},\"before\":\"e586a0bff0ab398a71e0b59265b9fde31f56db6b\",\"created\":false,\"forced\":false,\"repository\":{\"allow_forking\":true,\"stargazers_count\":0,\"is_template\":false,\"pushed_at\":1675861620,\"subscription_url\":\"https://api.github.com/repos/gustafbergmark/CITest/subscription\",\"language\":\"Java\",\"branches_url\":\"https://api.github.com/repos/gustafbergmark/CITest/branches{/branch}\",\"issue_comment_url\":\"https://api.github.com/repos/gustafbergmark/CITest/issues/comments{/number}\",\"labels_url\":\"https://api.github.com/repos/gustafbergmark/CITest/labels{/name}\",\"subscribers_url\":\"https://api.github.com/repos/gustafbergmark/CITest/subscribers\",\"releases_url\":\"https://api.github.com/repos/gustafbergmark/CITest/releases{/id}\",\"svn_url\":\"https://github.com/gustafbergmark/CITest\",\"id\":598573126,\"has_discussions\":false,\"master_branch\":\"master\",\"forks\":0,\"archive_url\":\"https://api.github.com/repos/gustafbergmark/CITest/{archive_format}{/ref}\",\"git_refs_url\":\"https://api.github.com/repos/gustafbergmark/CITest/git/refs{/sha}\",\"forks_url\":\"https://api.github.com/repos/gustafbergmark/CITest/forks\",\"visibility\":\"public\",\"statuses_url\":\"https://api.github.com/repos/gustafbergmark/CITest/statuses/{sha}\",\"ssh_url\":\"git@github.com:gustafbergmark/CITest.git\",\"license\":null,\"full_name\":\"gustafbergmark/CITest\",\"size\":71,\"languages_url\":\"https://api.github.com/repos/gustafbergmark/CITest/languages\",\"html_url\":\"https://github.com/gustafbergmark/CITest\",\"collaborators_url\":\"https://api.github.com/repos/gustafbergmark/CITest/collaborators{/collaborator}\",\"clone_url\":\"https://github.com/gustafbergmark/CITest.git\",\"name\":\"CITest\",\"pulls_url\":\"https://api.github.com/repos/gustafbergmark/CITest/pulls{/number}\",\"default_branch\":\"master\",\"hooks_url\":\"https://api.github.com/repos/gustafbergmark/CITest/hooks\",\"trees_url\":\"https://api.github.com/repos/gustafbergmark/CITest/git/trees{/sha}\",\"tags_url\":\"https://api.github.com/repos/gustafbergmark/CITest/tags\",\"private\":false,\"contributors_url\":\"https://api.github.com/repos/gustafbergmark/CITest/contributors\",\"has_downloads\":true,\"notifications_url\":\"https://api.github.com/repos/gustafbergmark/CITest/notifications{?since,all,participating}\",\"open_issues_count\":0,\"description\":null,\"created_at\":1675770337,\"watchers\":0,\"keys_url\":\"https://api.github.com/repos/gustafbergmark/CITest/keys{/key_id}\",\"deployments_url\":\"https://api.github.com/repos/gustafbergmark/CITest/deployments\",\"has_projects\":true,\"archived\":false,\"has_wiki\":true,\"updated_at\":\"2023-02-07T12:01:42Z\",\"comments_url\":\"https://api.github.com/repos/gustafbergmark/CITest/comments{/number}\",\"stargazers_url\":\"https://api.github.com/repos/gustafbergmark/CITest/stargazers\",\"disabled\":false,\"git_url\":\"git://github.com/gustafbergmark/CITest.git\",\"has_pages\":false,\"owner\":{\"gists_url\":\"https://api.github.com/users/gustafbergmark/gists{/gist_id}\",\"repos_url\":\"https://api.github.com/users/gustafbergmark/repos\",\"following_url\":\"https://api.github.com/users/gustafbergmark/following{/other_user}\",\"starred_url\":\"https://api.github.com/users/gustafbergmark/starred{/owner}{/repo}\",\"login\":\"gustafbergmark\",\"followers_url\":\"https://api.github.com/users/gustafbergmark/followers\",\"type\":\"User\",\"url\":\"https://api.github.com/users/gustafbergmark\",\"subscriptions_url\":\"https://api.github.com/users/gustafbergmark/subscriptions\",\"received_events_url\":\"https://api.github.com/users/gustafbergmark/received_events\",\"avatar_url\":\"https://avatars.githubusercontent.com/u/1709398?v=4\",\"events_url\":\"https://api.github.com/users/gustafbergmark/events{/privacy}\",\"html_url\":\"https://github.com/gustafbergmark\",\"name\":\"gustafbergmark\",\"site_admin\":false,\"id\":1709398,\"gravatar_id\":\"\",\"email\":\"gustaf.bergmark@gmail.com\",\"node_id\":\"MDQ6VXNlcjE3MDkzOTg=\",\"organizations_url\":\"https://api.github.com/users/gustafbergmark/orgs\"},\"commits_url\":\"https://api.github.com/repos/gustafbergmark/CITest/commits{/sha}\",\"compare_url\":\"https://api.github.com/repos/gustafbergmark/CITest/compare/{base}...{head}\",\"git_commits_url\":\"https://api.github.com/repos/gustafbergmark/CITest/git/commits{/sha}\",\"topics\":[],\"blobs_url\":\"https://api.github.com/repos/gustafbergmark/CITest/git/blobs{/sha}\",\"git_tags_url\":\"https://api.github.com/repos/gustafbergmark/CITest/git/tags{/sha}\",\"merges_url\":\"https://api.github.com/repos/gustafbergmark/CITest/merges\",\"downloads_url\":\"https://api.github.com/repos/gustafbergmark/CITest/downloads\",\"has_issues\":true,\"web_commit_signoff_required\":false,\"url\":\"https://github.com/gustafbergmark/CITest\",\"contents_url\":\"https://api.github.com/repos/gustafbergmark/CITest/contents/{+path}\",\"mirror_url\":null,\"milestones_url\":\"https://api.github.com/repos/gustafbergmark/CITest/milestones{/number}\",\"teams_url\":\"https://api.github.com/repos/gustafbergmark/CITest/teams\",\"fork\":false,\"issues_url\":\"https://api.github.com/repos/gustafbergmark/CITest/issues{/number}\",\"stargazers\":0,\"events_url\":\"https://api.github.com/repos/gustafbergmark/CITest/events\",\"issue_events_url\":\"https://api.github.com/repos/gustafbergmark/CITest/issues/events{/number}\",\"assignees_url\":\"https://api.github.com/repos/gustafbergmark/CITest/assignees{/user}\",\"open_issues\":0,\"watchers_count\":0,\"node_id\":\"R_kgDOI62ARg\",\"homepage\":null,\"forks_count\":0},\"base_ref\":null,\"ref\":\"refs/heads/false\",\"deleted\":false,\"sender\":{\"gists_url\":\"https://api.github.com/users/gustafbergmark/gists{/gist_id}\",\"repos_url\":\"https://api.github.com/users/gustafbergmark/repos\",\"following_url\":\"https://api.github.com/users/gustafbergmark/following{/other_user}\",\"starred_url\":\"https://api.github.com/users/gustafbergmark/starred{/owner}{/repo}\",\"login\":\"gustafbergmark\",\"followers_url\":\"https://api.github.com/users/gustafbergmark/followers\",\"type\":\"User\",\"url\":\"https://api.github.com/users/gustafbergmark\",\"subscriptions_url\":\"https://api.github.com/users/gustafbergmark/subscriptions\",\"received_events_url\":\"https://api.github.com/users/gustafbergmark/received_events\",\"avatar_url\":\"https://avatars.githubusercontent.com/u/1709398?v=4\",\"events_url\":\"https://api.github.com/users/gustafbergmark/events{/privacy}\",\"html_url\":\"https://github.com/gustafbergmark\",\"site_admin\":false,\"id\":1709398,\"gravatar_id\":\"\",\"node_id\":\"MDQ6VXNlcjE3MDkzOTg=\",\"organizations_url\":\"https://api.github.com/users/gustafbergmark/orgs\"},\"commits\":[{\"committer\":{\"name\":\"GitHub\",\"email\":\"noreply@github.com\",\"username\":\"web-flow\"},\"removed\":[],\"tree_id\":\"b689ebd8826165f68271fc2a19bbbdbc3372c7a1\",\"added\":[],\"author\":{\"name\":\"Gustaf\",\"email\":\"gbergmar@kth.se\",\"username\":\"gustafbergmark\"},\"distinct\":true,\"modified\":[\"README.md\"],\"id\":\"f4b67c56251efa0e617e364b166da70546ae60f8\",\"message\":\"Update README.md\",\"url\":\"https://github.com/gustafbergmark/CITest/commit/f4b67c56251efa0e617e364b166da70546ae60f8\",\"timestamp\":\"2023-02-08T14:06:59+01:00\"}],\"after\":\"f4b67c56251efa0e617e364b166da70546ae60f8\"}\n");
        File database = new File("./database/testsave.json");
        FileWriter writer = null;
        try {
            // Create empty database and mock response
            writer = new FileWriter(database);
            writer.write("{}");
            writer.close();
            HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

            // Test performCI
            server.performCI(input, response,database);

            // Delete database
            FileUtils.delete(database);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }*/
}