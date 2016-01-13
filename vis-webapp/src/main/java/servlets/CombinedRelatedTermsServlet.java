package servlets;

import data_processing.related_terms_combiner.CombinedRelatedTerms;
import api.exception.LuceneSearchException;
import servlets.servlet_util.RequestUtils;
import servlets.servlet_util.ResponseUtils;
import servlets.servlet_util.ServletConstant;
import server_utils.JsonCreator;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

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
            if (req.getParameterMap().containsKey(ServletConstant.DOC_ID)) {
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

            String response = JsonCreator.getPrettyJson(combined.getRelatedTerms(term, docId));
            ResponseUtils.printResponse(res, response);

        } catch (LuceneSearchException | NumberFormatException e) {
            e.printStackTrace();
            // TODO: Better logging for debugging
            res.getWriter().println("<h1>ERROR</h1><p>" + e.toString() + "</p>");
        }
    }
}
