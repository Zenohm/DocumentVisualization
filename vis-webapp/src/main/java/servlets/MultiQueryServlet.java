package servlets;

import com.google.gson.GsonBuilder;
import searcher.MultiQuerySearcher;
import searcher.exception.LuceneSearchException;
import searcher.reader.LuceneIndexReader;
import searcher.results.MultiQueryResults;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chris on 11/19/15.
 */
@WebServlet(value = "/multi_term_search", name = "multiTermSearch")
public class MultiQueryServlet extends GenericServlet {
    public static final int MAX_QUERIES = 10;
    public static final String QUERY_STRING = "query";

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        try{
            List<String> queries = new ArrayList<>();
            for(int queryNum = 1; queryNum < MAX_QUERIES; queryNum++){
                String queryName = QUERY_STRING + queryNum;
                if(req.getParameterMap().containsKey(queryName)){
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
            res.getWriter().println((new GsonBuilder()).create().toJson(queryResults));
        }catch (LuceneSearchException e){
            System.err.println("There was an error with the multi query servlet");
            e.printStackTrace();
        }
    }
}
