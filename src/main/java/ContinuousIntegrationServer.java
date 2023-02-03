import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;
import java.io.File;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.gradle.tooling.TestExecutionException;
import org.gradle.tooling.TestLauncher;

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
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        System.out.println(target);

        // here you do all the continuous integration tasks
        // for example
        // 1st clone your repository
        // 2nd compile the code

        response.getWriter().println("CI job done");
    }

    // used to start the CI server in command line
    public static void main(String[] args) throws Exception
    {
        System.out.println("hello");
        // Run tests
        ProjectConnection connection;
        try {
            GradleConnector connector = GradleConnector.newConnector();
            // TODO: change pathname to cloned project
            File projectDir = new File("./");
            connector.forProjectDirectory(projectDir);

            connection = connector.connect();
            TestLauncher launcher = connection.newTestLauncher();
            // TODO: check all classes
            launcher = launcher.withJvmTestClasses("ContinuousIntegrationServerTest");
            launcher.run();
        } catch (TestExecutionException ex) {
            // Tests failed
            // TODO: add appropriate handling of failure
            ex.printStackTrace();
        } catch(Exception ex) {
            ex.printStackTrace();
        }

        // TODO: Gracefully disconnect when done

        Server server = new Server(8080);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }
}