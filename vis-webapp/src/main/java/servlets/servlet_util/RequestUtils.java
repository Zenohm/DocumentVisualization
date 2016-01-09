package servlets.servlet_util;

import javax.servlet.ServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 12/31/15.
 */
public class RequestUtils {
    private RequestUtils(){} // Can't instantiate
    public static final int MAX_QUERIES = 10;
    public static final String QUERY_STRING = "query";
    public static List<String> getQueries(ServletRequest req){
        List<String> queries = new ArrayList<>();
        for (int queryNum = 1; queryNum < MAX_QUERIES; queryNum++) {
            String queryName = QUERY_STRING + queryNum;
            if (req.getParameterMap().containsKey(queryName)) {
                String q = req.getParameter(queryName);
                queries.add(q);
            }
        }
        return queries;
    }

    public static int getIntegerParameter(ServletRequest req, String param) throws NumberFormatException{
        if(!req.getParameterMap().containsKey(param)){
            System.err.println("Parameter does not exist: " + param + "\n" +
            "On Servlet: " + req.getServletContext().getServerInfo());
        }
        return Integer.parseInt(req.getParameter(param));
    }
}
