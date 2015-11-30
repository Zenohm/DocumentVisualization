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
@WebServlet(value = "/related_terms", name = "relatedTermsServlet")
public class RelatedTermsServlet extends GenericServlet {

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        try {
            int docId;
            if(req.getParameterMap().containsKey("docId")){
                docId = Integer.parseInt(req.getParameter("docId"));
            }else{
                throw new LuceneSearchException("No document ID.");
            }

            String term;
            if(req.getParameterMap().containsKey("term")){
                term = req.getParameter("term");
            }else{
                throw new LuceneSearchException("No Term");
            }


            List<ScoredTerm> terms;
            if(req.getParameterMap().containsKey("limit")){
                int limit = Integer.parseInt(req.getParameter("limit"));
                terms = TermsAnalyzer.getRelatedTermsInDocument(LuceneIndexReader.getInstance().getReader(), docId, term, limit);
            } else {
                terms = TermsAnalyzer.getRelatedTermsInDocument(LuceneIndexReader.getInstance().getReader(), docId, term);
            }

            res.getWriter().println((new GsonBuilder()).create().toJson(terms));

        } catch (LuceneSearchException | NumberFormatException e) {
            e.printStackTrace();
            // TODO: Better logging for debugging
            res.getWriter().println("<h1>ERROR</h1><p>" + e.toString() + "</p>");
        }
    }
}
