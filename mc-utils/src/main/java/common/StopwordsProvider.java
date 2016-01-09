package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chris on 1/3/16.
 */
public class StopwordsProvider {
    private final Set<String> stopwords = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private static String filename;
    private static StopwordsProvider provider;

    private StopwordsProvider(String filename) {
        Scanner s = null;
        StopwordsProvider.filename = filename;
        System.err.println("Rebuilding stopwords for: " + filename);
        try{
            s = new Scanner(new File(filename));
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }

        while (s != null && s.hasNextLine()){
            stopwords.add(s.nextLine());
        }
        if(s != null){
            s.close();
        }else{
            System.err.println("Stopwords file not found! File: " + filename);
        }
    }

    public static synchronized StopwordsProvider getProvider(String filename){
        if(provider == null || !StopwordsProvider.filename.equals(filename)){
            provider = new StopwordsProvider(filename);
        }
        return provider;
    }

    public static StopwordsProvider getProvider(){
        String DEFAULT_STOP_FILE = System.getenv(Constants.RESOURCE_FOLDER_VAR) + "/" + Constants.STOPWORDS_FILE;
        return getProvider(DEFAULT_STOP_FILE);
    }

    public Set<String> getStopwords(){
        return stopwords;
    }


}
