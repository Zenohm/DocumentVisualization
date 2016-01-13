package internal.static_util;

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by chris on 1/13/16.
 */
public class StringFilters {
    /**
     * Checks if a string is numeric
     *
     * @param str String to check if it is a number
     * @return True if the string is a number
     */
    public static boolean isNumeric(String str) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);
        formatter.parse(str, pos);
        boolean IsANumber = str.length() == pos.getIndex();

        Pattern p = Pattern.compile("[0-9]");
        Matcher m = p.matcher(str);
        boolean containsNumber = m.matches();

        return IsANumber || containsNumber;
    }

    public static String removeNumbers(String s){
        return Arrays.asList(s.split(" ")).stream()
                .filter(str -> !StringFilters.isNumeric(str))
                .collect(Collectors.joining(" "));
    }
}
