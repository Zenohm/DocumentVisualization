package servlets;

import com.google.gson.GsonBuilder;
import reader.LuceneIndexReader;
import searcher.DocumentSearcher;
import searcher.DocumentSearcherFactory;
import searcher.TokenizerType;
import searcher.exception.LuceneSearchException;

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
public class TermSearchServlet extends GenericServlet {

    /**
     * Searches the document database for a term
     * @param req
     * Required Parameters:
     *    term: The term to search for
     * @param res The response will contain a list of documents and their scores.
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        String term = req.getParameter("term");
        try {
            DocumentSearcher searcher = DocumentSearcherFactory
                    .getDocumentSearcher(LuceneIndexReader.getInstance(), TokenizerType.WHITESPACE_TOKENIZER);
            res.getWriter().println((new GsonBuilder()).create().toJson(searcher.searchForTerm(term)));
        } catch (LuceneSearchException e) {
            System.err.println("There was an error with the index searcher.");
            e.printStackTrace();
        }
    }
}
