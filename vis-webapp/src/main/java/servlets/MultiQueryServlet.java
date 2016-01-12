package servlets;

import com.google.gson.GsonBuilder;
import data_processing.multi_query_processing.MultiQueryConverter;
import document_search.MultiQuerySearch;
import servlets.servlet_util.RequestUtils;
import util.data.D3ConvertibleJson;
import reader.LuceneIndexReader;
import api.document_search.MultiQuerySearcher;
import searcher.exception.LuceneSearchException;
import common.results.MultiQueryResults;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Performs a multi-query search. Can be set in vis mode or non-vis mode
 * Created by chris on 11/19/15.
 */
@WebServlet(value = "/multi_term_search", name = "multiTermSearch")
public class MultiQueryServlet extends HttpServlet {


    /**
     * Servlet Service for doing multi query searches
     *
     * @param req Required Parameters:
     *            query*: Queries to be used. This determines what to search for
     *            Optional Parameters:
     *            vis: If this parameter is included, then the output will be in visualization format
     * @param res JSON representation of the query search.
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        try {
            List<String> queries = RequestUtils.getQueries(req);
            req.getSession().setAttribute("queries", queries); // TODO: Remove the magic string
            String[] queryStringArray = new String[queries.size()];
            queryStringArray = queries.toArray(queryStringArray);
            MultiQuerySearch searcher =
                    new MultiQuerySearcher(LuceneIndexReader.getInstance());
            List<MultiQueryResults> queryResults =
                    searcher.searchForResults(queryStringArray);
            if (req.getParameterMap().containsKey("vis")) {
                D3ConvertibleJson converted = MultiQueryConverter.convertToLinksAndNodes(queryResults);
                res.getWriter().println((new GsonBuilder().setPrettyPrinting()).create().toJson(converted));
            } else {
                res.getWriter().println((new GsonBuilder()).create().toJson(queryResults));
            }


        } catch (LuceneSearchException e) {
            System.err.println("There was an error with the multi query servlet");
            e.printStackTrace();
        }
    }



}
