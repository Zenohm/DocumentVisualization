package servlets.test_servlets;

import api.exception.LuceneSearchException;
import api.reader.LuceneIndexReader;
import internal.term_utils.TermLocationsSearcher;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import servlets.servlet_util.JsonCreator;
import servlets.servlet_util.ResponseUtils;

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
    private static final Log log = LogFactory.getLog(TermLocationsServlet.class);
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        try {
            String term = StringUtils.trim(req.getParameter("term"));
            TermLocationsSearcher searcher =
                    new TermLocationsSearcher(LuceneIndexReader.getInstance());

            String response = JsonCreator.toJson(searcher.getLocationsOfTerm(term));
            ResponseUtils.printResponse(res, response);
        } catch (LuceneSearchException e) {
            log.error("There was an error with the Term Locations servlet", e);
        }

    }
}
