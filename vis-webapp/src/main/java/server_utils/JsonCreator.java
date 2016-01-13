package server_utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * JSON Creator
 * Created by chris on 12/30/15.
 */
public class JsonCreator {

    private static Gson json = (new GsonBuilder()).create();
    private static Gson pjson = (new GsonBuilder().setPrettyPrinting()).create();

    public static <T> String getCompressedJson(T object){
        return json.toJson(object);
    }

    public static <T> String getPrettyJson(T object){
        return pjson.toJson(object);
    }

    public static <T> String toJson(T object){
        return getCompressedJson(object);
    }
}
