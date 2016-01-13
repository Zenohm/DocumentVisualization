package utilities;

import org.apache.commons.lang.StringUtils;

import java.util.Set;

/**
 * String manipulator for removing stopwords.
 * Created by chris on 1/5/16.
 */
public class StringManip {
    private StringManip(){}
    public static String removeStopwords(String original, Set<String> stopwords){
        String output = original;
        for(String stop : stopwords){
            output = StringUtils.replace(output, " " + stop + " ", " ");
        }
        return output;
    }

    /**
     * Splits a text into an array of sentences.
     *
     * @param text Text to split into individual sentences
     * @return An array of strings that contain sentences
     */
    public static String[] splitSentences(String text) {
        return text.split("(?<=[.!?])\\s*");
    }
}
