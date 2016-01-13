package servlets.servlet_util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Created by chris on 12/31/15.
 */
public class ResponseUtils {
    private static final Log log = LogFactory.getLog(ResponseUtils.class);
    private ResponseUtils() {} // can't create me!
    public static void printResponse(ServletResponse res, String responseText){
        try{
            res.getWriter().println(responseText);
        }catch(IOException e){
            e.printStackTrace();
            log.error("Could not get writer while attempting to send: " + responseText, e);
        }
    }

    public static <T> void printJsonResponse(ServletResponse res, T response){
        printResponse(res, JsonCreator.toJson(response));
    }

    public static <T> void printPrettyJsonResponse(ServletResponse res, T response){
        printResponse(res, JsonCreator.getPrettyJson(response));
    }
}
