package utilities;

import common.StopwordsProvider;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * String manipulator for removing stopwords.
 * Created by chris on 1/5/16.
 */
public class StringManip {
    private StringManip(){}
    public static String removeStopwords(String original, Set<String> stopwords){
        return original.replaceAll(getMultiwordRegexString(stopwords), " ");
    }

    public static String removeStopwords(String original, Pattern p){
        return p.matcher(original).replaceAll(" ");
    }

    /**
     * Removes stopwords using the default stopwords provider
     * @param original The original string
     * @return The string with the stopwords removed
     */
    public static String removeStopwords(String original){
        return removeStopwords(original, StopwordsProvider.getProvider().getRegex());
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

    /**
     * Removes The numbers from a string!
     * @param s The string to remove numbers from
     * @return A string without numbers
     */
    public static String removeNumbers(String s){
        return Arrays.asList(s.split(" ")).stream()
                .filter(str -> !StringFilters.isNumeric(str))
                .collect(Collectors.joining(" "));
    }

    public static String removeTerm(String s, String toRemove){
        return s.replaceAll(getOnlyWordRegex(toRemove), " ");
    }

    public static String getMultiwordRegexString(Set<String> stopwords) {
        return StringUtils.join(stopwords.parallelStream()
                                         .map(StringManip::getOnlyWordRegex)
                                         .collect(Collectors.toList()), "|");
    }

    /**
     * Creates a string representation of a regex that contains ONLY a word and may or may not be surrounded by whitespace.
     * @param term The term
     * @return The regex string
     */
    private static String getOnlyWordRegex(String term){
        String newRegex = "\\s*\\b";
        newRegex += term;
        newRegex += "\\b\\s*";
        return newRegex;
    }

    public static String replaceSmartQuotes(String str) {
        String retVal = str;
        retVal = retVal.replaceAll( "[\u2018\u2019\u201A\u201B\u2032\u2035]", "'" );
        retVal = retVal.replaceAll("[\u201C\u201D\u201E\u201F\u2033\u2036]","\"");
        return retVal;
    }
}
