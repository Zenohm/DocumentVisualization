package common;

import common.data.ScoredTerm;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utilities for converting Maps of strings and numbers to lists of scored terms.
 * Created by chris on 1/21/16.
 */
public class ScoredTermConverter {
    /**
     * Converts the map of terms to a List of scored terms
     *
     * @param terms Map of terms
     * @return List of scored terms
     */
    public static List<ScoredTerm> convertToScoredTerm(Map<String, ? extends Number> terms) {
        return convertToScoredTerm(terms, 1.0);
    }

    /**
     * Converts the map of terms to a list of scored terms, uses the normalizer that is given
     *
     * @param terms      The map of terms to use
     * @param normalizer a normalizing constant
     * @return List of scored terms
     */
    public static List<ScoredTerm> convertToScoredTerm(Map<String, ? extends Number> terms, double normalizer) {
        return terms.entrySet().parallelStream()
                .map(e -> new ScoredTerm(e.getKey(), e.getValue().doubleValue() / normalizer))
                .collect(Collectors.toList());
    }
}
