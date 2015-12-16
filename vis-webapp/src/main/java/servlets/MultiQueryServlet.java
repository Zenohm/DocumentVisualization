package servlets;

import com.google.gson.GsonBuilder;
import data_processing.multi_query_processing.MultiQueryConverter;
import data_processing.multi_query_processing.multi_query_json_data.MultiQueryJson;
import reader.LuceneIndexReader;
import searcher.MultiQuerySearcher;
import searcher.exception.LuceneSearchException;
import searcher.results.MultiQueryResults;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Performs a multi-query search. Can be set in vis mode or non-vis mode
 * Created by chris on 11/19/15.
 */
@WebServlet(value = "/multi_term_search", name = "multiTermSearch")
public class MultiQueryServlet extends GenericServlet {
    public static final int MAX_QUERIES = 10;
    public static final String QUERY_STRING = "query";

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
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        try {
            List<String> queries = new ArrayList<>();
            for (int queryNum = 1; queryNum < MAX_QUERIES; queryNum++) {
                String queryName = QUERY_STRING + queryNum;
                if (req.getParameterMap().containsKey(queryName)) {
                    String q = req.getParameter(queryName);
                    queries.add(q);
                }
            }
            String[] queryStringArray = new String[queries.size()];
            queryStringArray = queries.toArray(queryStringArray);
            MultiQuerySearcher searcher =
                    new MultiQuerySearcher(LuceneIndexReader.getInstance());
            List<MultiQueryResults> queryResults =
                    searcher.searchForResults(queryStringArray);
            if (req.getParameterMap().containsKey("vis")) {
                MultiQueryJson converted = MultiQueryConverter.convertToLinksAndNodes(queryResults);
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
