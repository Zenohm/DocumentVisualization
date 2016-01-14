/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Chris Bellis, Chris Perry
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package internal.static_util.scorer;

import api.reader.LuceneIndexReader;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import common.Constants;
import common.data.ScoredTerm;
import internal.static_util.QueryUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Scores terms based on how related they are.
 * Created by Chris P. on 11/6/15.
 */
public class TermRelatednessScorer {

    /**
     * The minimum a synonym must score to avoid being eliminated
     */
    private static final double DEFAULT_CUTOFF_RATIO = .50;
    private static final Log log = LogFactory.getLog(TermRelatednessScorer.class);
    private static IndexSearcher searcher;

    static{
        searcher = new IndexSearcher(LuceneIndexReader.getInstance().getReader());
    }

    /**
     * Caching utilized to speed up the processing of scores.
     */
    private static Cache<String, Integer> cache = CacheBuilder.newBuilder().maximumSize(Constants.MAX_CACHE_SIZE).build();

    /**
     * Given a word and a set of its synonym, returns an ordered list of terms ranked from most relevant to least.
     *
     * @param original The word you want to find ranked terms for
     * @param terms The set of terms for related to the original
     * @return A list of scoredTerms, in descending order of their scores
     */
    public static List<ScoredTerm> getRankedTermsWithScores(String original, Set<String> terms) {
        // Call getRankedTermsWithScores with the default ratio
        return getRankedTermsWithScores(original, terms, DEFAULT_CUTOFF_RATIO);
    }

    /**
     * Given a word and a set of its related relatedTerms, returns an ordered list of relatedTerms ranked from most relevant to least.
     *
     * @param original          The word you want to find ranked relatedTerms for
     * @param relatedTerms          The set of terms related to the original
     * @param minRelevanceRatio An optional parameter for the minimum a synonym must score to be returned. If none
     *                          given, .50 is assumed.
     * @return A list of scoredTerms, in descending order of their scores.
     */
    public static List<ScoredTerm> getRankedTermsWithScores(String original, Set<String> relatedTerms,
                                                            double minRelevanceRatio) {
        // Handle null/empty cases
        if (original == null || relatedTerms == null || relatedTerms.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        // A HashMap with the word as the key, and its corresponding score
        List<ScoredTerm> scoredTerms = Collections.synchronizedList(new ArrayList<>());

        // Open up a parallel stream on the relatedTerms to perform the doc search on them all
        relatedTerms.parallelStream()
                .forEach(term -> scoredTerms.add(new ScoredTerm(term, score(original, term))));

        // TODO: NOTICE: if the change is made to use a 'top ten' type, refactor to just take first 'x' relatedTerms
        // Trim the fat - anything below relevance rank gets the D
        List<ScoredTerm> relevantTerms = getRelevantTerms(scoredTerms, minRelevanceRatio);

        // Use common.data.ScoredTerm's built-in comparator for sorting purposes
        // It is by default in ascending order; we want most relevant first, so reverse it
        Collections.sort(relevantTerms, Comparator.reverseOrder());

        // If there were no relevant relatedTerms, return null.
        // TODO: throw a NoRelevantTerms exception?
        return relevantTerms.size() > 0 ? relevantTerms : Collections.EMPTY_LIST;
    }

    /**
     * Helper method to run ScoredTerms through a filter and give the axe to ones that fall below the cutoff point.
     *
     * @param scoredTerms       A list of terms that have scores.
     * @param minRelevanceRatio The minimum score a term must have to avoid being cut.  If none is given,
     *                          #DEFAULT_CUTOFF_RATIO# is assumed.
     * @return A trimmed list of scored terms, containing only terms who scored greater than #minRelevanceRatio#
     */
    private static List<ScoredTerm> getRelevantTerms(List<ScoredTerm> scoredTerms, double minRelevanceRatio) {
        // If a cutoff is specified, use it, otherwise use the default.
        return scoredTerms.stream()
                .filter(scoredTerm -> scoredTerm.getScore() >= minRelevanceRatio)
                .collect(Collectors.toList());
    }

    /**
     * Gets the ratio of documents containing original AND otherWord / documents containing the otherWord
     *
     * @param original The original word
     * @param otherWord  A otherWord of #original#
     * @return A double representing #docs(BOTH original AND otherWord)/#docs(otherWord)
     */
    public static double score(String original, String otherWord) {
        // Search for original AND otherWord
        double numContainingOriginalAndOtherWord = getNumOfDocuments(original, otherWord); // LuceneSearch for number of docs containing BOTH

        // Search for docs containing otherWord
        double numContainingOriginal = getNumOfDocuments(original);

        // Return containingBoth/containingOriginal while avoiding division by zero.
        return numContainingOriginal == 0 ? 0 : (numContainingOriginalAndOtherWord / numContainingOriginal);
    }

    /**
     * Gets the number of documents word appears in the document index.
     *
     * @param words The word(s) to do a documentSearch for.
     * @return The number of documents containing (all of) #words#
     */
    private static int getNumOfDocuments(String... words) {
        // Handle idiot cases
        if (words == null || words.length == 0) {
            return 0;
        }

        Query q = QueryUtils.mustContainWords(words);

        // Get the search call
        Callable<Integer> search = () -> {
            try {
                return searcher.count(q);
            } catch (IOException e) {
                log.error(TermRelatednessScorer.class.toString() + ": ERROR: Could not get term count for query " +
                        q.toString() + ".");
                return 0; // Assume no documents then.
            }
        };

        // Get the number of documents from the cache.
        int numDocuments = 0;
        try {
            numDocuments = cache.get(q.toString(), search);
        } catch (ExecutionException e) {
            log.error("[TermRelatednessScorer] ERROR: There was an error while populating the cache: "
                    + e.getLocalizedMessage());
            log.debug(e.getStackTrace());
        }
        return numDocuments;
    }
}