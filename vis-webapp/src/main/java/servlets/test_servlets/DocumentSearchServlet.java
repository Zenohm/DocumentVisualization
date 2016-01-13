package servlets.test_servlets;

import document_search.DocumentSearcher;
import exception.SearchException;
import api.reader.LuceneIndexReader;
import api.document_search.DocumentSearcherFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import servlets.servlet_util.ResponseUtils;
import servlets.servlet_util.JsonCreator;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * Searches the document database for a term
 * Created by Chris on 10/7/2015.
 */
@WebServlet(value = "/term_search", name = "termSearchServlet")
public class DocumentSearchServlet extends GenericServlet {
    private static final Log log = LogFactory.getLog(DocumentSearchServlet.class);
    /**
     * Searches the document database for a term
     *
     * @param req Required Parameters:
     *            term: The term to search for
     * @param res The response will contain a list of documents and their scores.
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        String term = req.getParameter("term");
        try {
            DocumentSearcher searcher = DocumentSearcherFactory
                    .getDocumentSearcher(LuceneIndexReader.getInstance(), DocumentSearcherFactory.TokenizerType.WHITESPACE_TOKENIZER);
            String response = JsonCreator.toJson(searcher.searchForTerm(term));
            ResponseUtils.printResponse(res, response);
        } catch (SearchException e) {
            log.error("Index searcher had an error.", e);
        }
    }
}
