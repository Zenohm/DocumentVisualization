package servlets;

import com.google.gson.GsonBuilder;
import common.data.ScoredTerm;
import full_text_analysis.TermsAnalyzer;
import reader.LuceneIndexReader;
import searcher.exception.LuceneSearchException;
import servlets.servlet_util.RequestUtils;
import servlets.servlet_util.ServletConstant;
import util.JsonCreator;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;

/**
 * Utilized for searches on related terms. Finds related terms in a document.
 * Created by chris on 10/13/15.
 */
@WebServlet(value = "/related_terms", name = "relatedTermsServlet")
public class RelatedTermsServlet extends GenericServlet {

    /**
     * Related Terms Service
     *
     * @param req Required Parameters:
     *            docId: The id of the document that is used as the basis for finding related terms
     *            term: The term to find the related terms for
     *            Optional Parameters:
     *            limit: Limit the number of terms that are returned
     * @param res Response contains a list of Scored Termss
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        try {
            int docId;
            if (req.getParameterMap().containsKey("docId")) {
                docId = RequestUtils.getIntegerParameter(req, ServletConstant.DOC_ID);
            } else {
                throw new LuceneSearchException("No document ID.");
            }

            String term;
            if (req.getParameterMap().containsKey("term")) {
                term = req.getParameter("term");
            } else {
                throw new LuceneSearchException("No Term");
            }


            List<ScoredTerm> terms;
            if (req.getParameterMap().containsKey("limit")) {
                int limit = RequestUtils.getIntegerParameter(req, "limit");
                terms = TermsAnalyzer.getRelatedTermsInDocument(LuceneIndexReader.getInstance().getReader(), docId, term, limit);
            } else {
                terms = TermsAnalyzer.getRelatedTermsInDocument(LuceneIndexReader.getInstance().getReader(), docId, term);
            }

            res.getWriter().println(JsonCreator.toJson(terms));

        } catch (LuceneSearchException | NumberFormatException e) {
            e.printStackTrace();
            // TODO: Better logging for debugging
            res.getWriter().println("<h1>ERROR</h1><p>" + e.toString() + "</p>");
        }
    }
}
