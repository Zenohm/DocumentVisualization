package servlets;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * Created by chris on 10/13/15.
 */
@WebServlet(value = "/related_terms", name = "relatedTermsServlet")
public class RelatedTermsServlet extends GenericServlet {

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        System.err.println("Not implemented yet");
        res.getWriter().println("<h1>ERROR</h1>");
    }
}
