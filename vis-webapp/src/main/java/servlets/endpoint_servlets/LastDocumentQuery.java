package servlets.endpoint_servlets;

import servlets.servlet_util.ResponseUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by chris on 1/6/16.
 */
@WebServlet(value = "/last_doc", name = "LastDocServlet")
public class LastDocumentQuery extends HttpServlet {
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException{
        if(req.getParameterMap().containsKey("docId")){
            req.getSession().setAttribute("docId", req.getParameter("docId"));
        }
        Object attr = req.getSession().getAttribute("docId");
        if(attr != null){
            ResponseUtils.printJsonResponse(res, attr);
        }
    }
}
