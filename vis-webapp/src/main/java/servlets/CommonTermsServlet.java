package servlets;

import com.google.gson.GsonBuilder;
import searcher.TermsAnalyzer;
import searcher.exception.LuceneSearchException;
import searcher.reader.LuceneIndexReader;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;
import common.ScoredTerm;

/**
 * Created by chris on 10/13/15.
 */
@WebServlet(value = "/common_terms", name = "commonTermsServlet")
public class CommonTermsServlet extends GenericServlet{
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        try {
            int docId = Integer.parseInt(req.getParameter("docId"));
            int limit = -1;
            List<ScoredTerm> terms;
            if(req.getParameterMap().containsKey("limit")) {
                limit = Integer.parseInt(req.getParameter("limit"));
                terms = TermsAnalyzer.getTopTerms(LuceneIndexReader.getInstance().getReader(), docId, limit);
            } else {
                terms = TermsAnalyzer.getTerms(LuceneIndexReader.getInstance().getReader(), docId);
            }

            res.getWriter().println((new GsonBuilder()).create().toJson(terms));

        } catch (LuceneSearchException | NumberFormatException e) {
            e.printStackTrace();
            // TODO: Better logging for debugging
            res.getWriter().println("<h1>ERROR</h1>" + e.toString());
        }
    }
}
