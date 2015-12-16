package util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 12/15/15.
 */
public class ListUtils {
    /**
     * Gets the top amount in the list
     *
     * @param list The list
     * @param n    The number of items at the beginning of the list to get
     * @param <T>
     * @return A list with n elements
     */
    public static <T> List<T> getSublist(List<T> list, int n) {
        if (list.size() < n) {
            n = list.size();
        }
        return new ArrayList<>(list.subList(0, n));
    }
}
