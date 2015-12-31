package servlets;

import servlets.servlet_util.RequestUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 12/31/15.
 */
public class SecondTierServlet extends HttpServlet {

    public static final String USE_PREVIOUS = "prev";

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res){
        // We have the option of using the previous or not based on request
        List<String> queries;
        if(req.getParameterMap().containsKey(USE_PREVIOUS)){
            Object obj = req.getSession().getAttribute("queries");
            if(obj != null){
                queries = (List<String>)obj;
            }else{
                System.err.println("ERROR: Requested to use previous with no previous queries");
                return;
            }
        }else{
            queries = RequestUtils.getQueries(req);
        }


    }
}
