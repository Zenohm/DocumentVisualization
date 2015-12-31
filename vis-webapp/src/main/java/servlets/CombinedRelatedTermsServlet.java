package servlets;

import com.google.gson.GsonBuilder;
import common.data.ScoredTerm;
import data_processing.related_terms_combiner.CombinedRelatedTerms;
import full_text_analysis.TermsAnalyzer;
import reader.LuceneIndexReader;
import searcher.exception.LuceneSearchException;
import util.JsonCreator;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;

/**
 * Created by chris on 12/30/15.
 */
@WebServlet(value = "/combined_terms", name = "CombinedTerms")
public class CombinedRelatedTermsServlet extends GenericServlet {

    private CombinedRelatedTerms combined;

    public CombinedRelatedTermsServlet(){
        super();
        combined = new CombinedRelatedTerms();
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        try {
            int docId;
            if (req.getParameterMap().containsKey("docId")) {
                docId = Integer.parseInt(req.getParameter("docId"));
            } else {
                throw new LuceneSearchException("No document ID.");
            }

            String term;
            if (req.getParameterMap().containsKey("term")) {
                term = req.getParameter("term");
            } else {
                throw new LuceneSearchException("No Term");
            }

            res.getWriter().println(JsonCreator.getPrettyJson(combined.getRelatedTerms(term, docId)));


        } catch (LuceneSearchException | NumberFormatException e) {
            e.printStackTrace();
            // TODO: Better logging for debugging
            res.getWriter().println("<h1>ERROR</h1><p>" + e.toString() + "</p>");
        }
    }
}
