package utilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by chris on 1/16/16.
 */
public class EasyLogger {
    private EasyLogger() {} // This is just a static class.
    private static final Log log = LogFactory.getLog(EasyLogger.class);
    private static final Set<String> beenOpened = new HashSet<>();
    public static final String log_dir = "easy_logs";
    static{
        new File(log_dir).mkdir();
    }

    public synchronized static <T> void log (String log_file, T logMessage){
        String filename = getFilename(log_file);
        if(!beenOpened.contains(filename)){
            beenOpened.add(filename);
            try {
                Files.delete(Paths.get(filename));
            } catch (IOException e) {
                log.error("Could not delete file: " + filename);
            }
        }

        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)))) {
            out.println(logMessage.toString());
        }catch (IOException e) {
            log.error("Could not log to file: " + log_file, e);
        }
    }

    public static String getFilename(String log_file){
        return log_dir + "/" + log_file + ".txt";
    }

}
