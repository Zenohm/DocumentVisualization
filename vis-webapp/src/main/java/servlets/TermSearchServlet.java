package servlets;

import com.google.gson.GsonBuilder;
import searcher.DocumentSearcher;
import searcher.exception.LuceneSearchException;
import reader.LuceneIndexReader;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * Created by Chris on 10/7/2015.
 */
@WebServlet(value = "/term_search", name = "termSearchServlet")
public class TermSearchServlet extends GenericServlet {

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        String term = req.getParameter("term");
        try {
            DocumentSearcher searcher = new DocumentSearcher(LuceneIndexReader.getInstance());
            res.getWriter().println((new GsonBuilder()).create().toJson(searcher.searchForTerm(term)));
        } catch (LuceneSearchException e) {
            System.err.println("There was an error with the index searcher.");
            e.printStackTrace();
        }
    }
}
