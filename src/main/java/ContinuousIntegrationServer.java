import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;

import java.io.IOException;
import java.util.stream.Collectors;
import org.json.*;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

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

    // used to start the CI server in command line
    public static void main(String[] args) throws Exception
    {
        Server server = new Server(8080);
        server.setHandler(new ContinuousIntegrationServer());
        server.start();
        server.join();
    }
}