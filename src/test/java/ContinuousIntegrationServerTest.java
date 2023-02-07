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
        JSONObject input = new JSONObject("{\"pusher\":{\"name\":\"gustafbergmark\",\"email\":\"gustaf.bergmark@gmail.com\"},\"compare\":\"https://github.com/gustafbergmark/CI/compare/assessment\",\"head_commit\":{\"committer\":{\"name\":\"GitHub\",\"email\":\"noreply@github.com\",\"username\":\"web-flow\"},\"removed\":[],\"tree_id\":\"d15aa2c3b1520596e9879d077e794bcf3e4f15e7\",\"added\":[\"database/database.json\",\"database/databasetest.json\"],\"author\":{\"name\":\"Glace97\",\"email\":\"57321597+Glace97@users.noreply.github.com\",\"username\":\"Glace97\"},\"distinct\":true,\"modified\":[\"build.gradle\",\"src/main/java/ContinuousIntegrationServer.java\",\"src/test/java/ContinuousIntegrationServerTest.java\"],\"id\":\"ea770f6db45ad51476ffc8c1834a0cf4c0d44f50\",\"message\":\"Merge pull request #14 from gustafbergmark/12-p7-keep-the-persistent-history-of-all-builds\\n\\nfeat: adds functionality for persistent build history. Closes #12\",\"url\":\"https://github.com/gustafbergmark/CI/commit/ea770f6db45ad51476ffc8c1834a0cf4c0d44f50\",\"timestamp\":\"2023-02-07T10:40:33+01:00\"},\"before\":\"0000000000000000000000000000000000000000\",\"created\":true,\"forced\":false,\"repository\":{\"allow_forking\":true,\"stargazers_count\":1,\"is_template\":false,\"pushed_at\":1675764317,\"subscription_url\":\"https://api.github.com/repos/gustafbergmark/CI/subscription\",\"language\":\"Java\",\"branches_url\":\"https://api.github.com/repos/gustafbergmark/CI/branches{/branch}\",\"issue_comment_url\":\"https://api.github.com/repos/gustafbergmark/CI/issues/comments{/number}\",\"labels_url\":\"https://api.github.com/repos/gustafbergmark/CI/labels{/name}\",\"subscribers_url\":\"https://api.github.com/repos/gustafbergmark/CI/subscribers\",\"releases_url\":\"https://api.github.com/repos/gustafbergmark/CI/releases{/id}\",\"svn_url\":\"https://github.com/gustafbergmark/CI\",\"id\":596080501,\"has_discussions\":false,\"master_branch\":\"master\",\"forks\":0,\"archive_url\":\"https://api.github.com/repos/gustafbergmark/CI/{archive_format}{/ref}\",\"git_refs_url\":\"https://api.github.com/repos/gustafbergmark/CI/git/refs{/sha}\",\"forks_url\":\"https://api.github.com/repos/gustafbergmark/CI/forks\",\"visibility\":\"public\",\"statuses_url\":\"https://api.github.com/repos/gustafbergmark/CI/statuses/{sha}\",\"ssh_url\":\"git@github.com:gustafbergmark/CI.git\",\"license\":null,\"full_name\":\"gustafbergmark/CI\",\"size\":45,\"languages_url\":\"https://api.github.com/repos/gustafbergmark/CI/languages\",\"html_url\":\"https://github.com/gustafbergmark/CI\",\"collaborators_url\":\"https://api.github.com/repos/gustafbergmark/CI/collaborators{/collaborator}\",\"clone_url\":\"https://github.com/gustafbergmark/CI.git\",\"name\":\"CI\",\"pulls_url\":\"https://api.github.com/repos/gustafbergmark/CI/pulls{/number}\",\"default_branch\":\"master\",\"hooks_url\":\"https://api.github.com/repos/gustafbergmark/CI/hooks\",\"trees_url\":\"https://api.github.com/repos/gustafbergmark/CI/git/trees{/sha}\",\"tags_url\":\"https://api.github.com/repos/gustafbergmark/CI/tags\",\"private\":false,\"contributors_url\":\"https://api.github.com/repos/gustafbergmark/CI/contributors\",\"has_downloads\":true,\"notifications_url\":\"https://api.github.com/repos/gustafbergmark/CI/notifications{?since,all,participating}\",\"open_issues_count\":6,\"description\":\"Java Continuous Integration implementation\",\"created_at\":1675254658,\"watchers\":1,\"keys_url\":\"https://api.github.com/repos/gustafbergmark/CI/keys{/key_id}\",\"deployments_url\":\"https://api.github.com/repos/gustafbergmark/CI/deployments\",\"has_projects\":true,\"archived\":false,\"has_wiki\":true,\"updated_at\":\"2023-02-03T11:46:19Z\",\"comments_url\":\"https://api.github.com/repos/gustafbergmark/CI/comments{/number}\",\"stargazers_url\":\"https://api.github.com/repos/gustafbergmark/CI/stargazers\",\"disabled\":false,\"git_url\":\"git://github.com/gustafbergmark/CI.git\",\"has_pages\":false,\"owner\":{\"gists_url\":\"https://api.github.com/users/gustafbergmark/gists{/gist_id}\",\"repos_url\":\"https://api.github.com/users/gustafbergmark/repos\",\"following_url\":\"https://api.github.com/users/gustafbergmark/following{/other_user}\",\"starred_url\":\"https://api.github.com/users/gustafbergmark/starred{/owner}{/repo}\",\"login\":\"gustafbergmark\",\"followers_url\":\"https://api.github.com/users/gustafbergmark/followers\",\"type\":\"User\",\"url\":\"https://api.github.com/users/gustafbergmark\",\"subscriptions_url\":\"https://api.github.com/users/gustafbergmark/subscriptions\",\"received_events_url\":\"https://api.github.com/users/gustafbergmark/received_events\",\"avatar_url\":\"https://avatars.githubusercontent.com/u/1709398?v=4\",\"events_url\":\"https://api.github.com/users/gustafbergmark/events{/privacy}\",\"html_url\":\"https://github.com/gustafbergmark\",\"name\":\"gustafbergmark\",\"site_admin\":false,\"id\":1709398,\"gravatar_id\":\"\",\"email\":\"gustaf.bergmark@gmail.com\",\"node_id\":\"MDQ6VXNlcjE3MDkzOTg=\",\"organizations_url\":\"https://api.github.com/users/gustafbergmark/orgs\"},\"commits_url\":\"https://api.github.com/repos/gustafbergmark/CI/commits{/sha}\",\"compare_url\":\"https://api.github.com/repos/gustafbergmark/CI/compare/{base}...{head}\",\"git_commits_url\":\"https://api.github.com/repos/gustafbergmark/CI/git/commits{/sha}\",\"topics\":[],\"blobs_url\":\"https://api.github.com/repos/gustafbergmark/CI/git/blobs{/sha}\",\"git_tags_url\":\"https://api.github.com/repos/gustafbergmark/CI/git/tags{/sha}\",\"merges_url\":\"https://api.github.com/repos/gustafbergmark/CI/merges\",\"downloads_url\":\"https://api.github.com/repos/gustafbergmark/CI/downloads\",\"has_issues\":true,\"web_commit_signoff_required\":false,\"url\":\"https://github.com/gustafbergmark/CI\",\"contents_url\":\"https://api.github.com/repos/gustafbergmark/CI/contents/{+path}\",\"mirror_url\":null,\"milestones_url\":\"https://api.github.com/repos/gustafbergmark/CI/milestones{/number}\",\"teams_url\":\"https://api.github.com/repos/gustafbergmark/CI/teams\",\"fork\":false,\"issues_url\":\"https://api.github.com/repos/gustafbergmark/CI/issues{/number}\",\"stargazers\":1,\"events_url\":\"https://api.github.com/repos/gustafbergmark/CI/events\",\"issue_events_url\":\"https://api.github.com/repos/gustafbergmark/CI/issues/events{/number}\",\"assignees_url\":\"https://api.github.com/repos/gustafbergmark/CI/assignees{/user}\",\"open_issues\":6,\"watchers_count\":1,\"node_id\":\"R_kgDOI4d3dQ\",\"homepage\":null,\"forks_count\":0},\"base_ref\":\"refs/heads/master\",\"ref\":\"refs/heads/assessment\",\"deleted\":false,\"sender\":{\"gists_url\":\"https://api.github.com/users/gustafbergmark/gists{/gist_id}\",\"repos_url\":\"https://api.github.com/users/gustafbergmark/repos\",\"following_url\":\"https://api.github.com/users/gustafbergmark/following{/other_user}\",\"starred_url\":\"https://api.github.com/users/gustafbergmark/starred{/owner}{/repo}\",\"login\":\"gustafbergmark\",\"followers_url\":\"https://api.github.com/users/gustafbergmark/followers\",\"type\":\"User\",\"url\":\"https://api.github.com/users/gustafbergmark\",\"subscriptions_url\":\"https://api.github.com/users/gustafbergmark/subscriptions\",\"received_events_url\":\"https://api.github.com/users/gustafbergmark/received_events\",\"avatar_url\":\"https://avatars.githubusercontent.com/u/1709398?v=4\",\"events_url\":\"https://api.github.com/users/gustafbergmark/events{/privacy}\",\"html_url\":\"https://github.com/gustafbergmark\",\"site_admin\":false,\"id\":1709398,\"gravatar_id\":\"\",\"node_id\":\"MDQ6VXNlcjE3MDkzOTg=\",\"organizations_url\":\"https://api.github.com/users/gustafbergmark/orgs\"},\"commits\":[],\"after\":\"ea770f6db45ad51476ffc8c1834a0cf4c0d44f50\"}\n");
        server.performCI(input);
        Path p = Paths.get("./local");
        try {
            FileUtils.deleteDirectory(p.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}