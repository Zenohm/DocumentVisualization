package servlets.servlet_util;

import util.JsonCreator;

import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Created by chris on 12/31/15.
 */
public class ResponseUtils {
    private ResponseUtils() {} // can't create me!
    public static void printResponse(ServletResponse res, String responseText){
        try{
            res.getWriter().println(responseText);
        }catch(IOException e){
            e.printStackTrace();
            System.err.println("Error, could not get writer while attempting to send: " + responseText);
        }
    }

    public static <T> void printJsonResponse(ServletResponse res, T response){
        printResponse(res, JsonCreator.toJson(response));
    }

    public static <T> void printPrettyJsonResponse(ServletResponse res, T response){
        printResponse(res, JsonCreator.getPrettyJson(response));
    }
}
