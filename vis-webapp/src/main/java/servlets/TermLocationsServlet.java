package servlets;

import access_utils.TermLocationsSearcher;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang.StringUtils;
import reader.LuceneIndexReader;
import searcher.exception.LuceneSearchException;
import util.JsonCreator;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * Created by chris on 12/16/15.
 */
@WebServlet(value = "/term_locs", name = "TermLocationsServlet")
public class TermLocationsServlet extends GenericServlet {
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        try {
            String term = StringUtils.trim(req.getParameter("term"));
            TermLocationsSearcher searcher =
                    new TermLocationsSearcher(LuceneIndexReader.getInstance());
            res.getWriter().println(JsonCreator.toJson(searcher.getLocationsOfTerm(term)));
        } catch (LuceneSearchException e) {
            System.err.println("There was an error with the Term Locations servlet");
            e.printStackTrace();
        }

    }
}
