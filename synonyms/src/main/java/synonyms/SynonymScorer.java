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
package synonyms;

import common.data.ScoredDocument;
import common.data.ScoredTerm;
import searcher.DocumentSearcher;
import searcher.DocumentSearcherFactory;
import searcher.TokenizerType;
import searcher.exception.LuceneSearchException;
import reader.LuceneIndexReader;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Chris on 11/6/15.
 */
public class SynonymScorer {

    /**
     * The minimum a synonym must score to avoid being eliminated
     */
    private static final double DEFAULT_CUTOFF_RATIO = .50;

    /**
     * Given a word and a set of its synonym, returns an ordered list of synonyms ranked from most relevant to least.
     *
     * @param original The word you want to find ranked synonyms for
     * @param synonyms The set of synonyms for the original word
     * @return A list of scoredTerms, in descending order of their scores
     */
    public static List<ScoredTerm> getRankedSynonymsWithScores(String original, Set<String> synonyms) {
        // Call getRankedSynonymsWithScores with the default ratio
        return getRankedSynonymsWithScores(original, synonyms, DEFAULT_CUTOFF_RATIO);
    }

    /**
     * Given a word and a set of its synonym, returns an ordered list of synonyms ranked from most relevant to least.
     *
     * @param original The word you want to find ranked synonyms for
     * @param synonyms The set of synonyms for the original word
     * @param minRelevanceRatio An optional parameter for the minimum a synonym must score to be returned. If none
     *                          given, .50 is assumed.
     * @return A list of scoredTerms, in descending order of their scores.
     */
    public static List<ScoredTerm> getRankedSynonymsWithScores(String original, Set<String> synonyms,
                                                               double minRelevanceRatio) {
        // Handle null/empty cases
        if (original == null || synonyms == null || synonyms.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        // A HashMap with the word as the key, and its corresponding score
        List<ScoredTerm> scoredSynonyms = new ArrayList<>();

        // Open up a parallel stream on the synonyms to perform the doc search on them all
        synonyms.parallelStream()
                .forEach(synonym -> scoredSynonyms.add(new ScoredTerm(synonym, score(original, synonym))));

        // TODO: NOTICE: if the change is made to use a 'top ten' type, refactor to just take first 'x' terms
        // Trim the fat - anything below relevance rank gets the D
        List<ScoredTerm> relevantTerms = getRelevantSynonyms(scoredSynonyms, minRelevanceRatio);

        // Use common.data.ScoredTerm's built-in comparator for sorting purposes
        relevantTerms.sort(ScoredTerm::compareTo);
        // It is by default in ascending order; we want most relevant first, so reverse it
        relevantTerms.sort(Comparator.reverseOrder());

        // If there were no relevant terms, return null.
        // TODO: throw a NoRelevantTerms exception?
        return relevantTerms.size() > 0 ? relevantTerms : Collections.EMPTY_LIST;
    }

    /**
     * Helper method to run ScoredTerms through a filter and give the axe to ones that fall below the cutoff point.
     *
     * @param scoredTerms A list of terms that have scores.
     * @param minRelevanceRatio The minimum score a term must have to avoid being cut.  If none is given,
     *                          #DEFAULT_CUTOFF_RATIO# is assumed.
     * @return A trimmed list of scored terms, containing only synonyms who scored greater than #minRelevanceRatio#
     */
    private static List<ScoredTerm> getRelevantSynonyms(List<ScoredTerm> scoredTerms, double minRelevanceRatio) {
        // If a cutoff is specified, use it, otherwise use the default.
        List<ScoredTerm> relevantSynonyms = scoredTerms.stream()
                .filter(scoredTerm -> scoredTerm.getScore() >= minRelevanceRatio)
                .collect(Collectors.toList());
        return relevantSynonyms;
    }

    /**
     * Gets the ratio of documents containing original AND synonym / documents containing the synonym
     *
     * @param original The original word
     * @param synonym A synonym of #original#
     * @return A double representing #docs(BOTH original AND synonym)/#docs(synonym)
     */
    private static double score(String original, String synonym) {
        // Search for original AND synonym
        double numContainingOriginalAndSynonym = getNumOfDocuments(original, synonym); // LuceneSearch for number of docs containing BOTH

        // Search for docs containing synonym
        double numContainingSynonym = getNumOfDocuments(synonym);

        // Return containingBoth/containingSynonym while avoiding division by zero.
        return numContainingSynonym == 0 ? 0 : (numContainingOriginalAndSynonym / numContainingSynonym);
    }

    /**
     * Gets the number of documents word appears in the document index.
     *
     * @param words The word(s) to do a documentSearch for.
     * @return The number of documents containing (all of) #words#
     */
    private static int getNumOfDocuments(String ... words) {
        // Handle idiot cases
        if (words == null || words.length == 0) {
            return -1;
        }

        // Attempt a lucene search for #words# to find relevant docs.
        try {
            // Initialize Lucene stuff
            DocumentSearcher searcher = DocumentSearcherFactory
                            .getDocumentSearcher(LuceneIndexReader.getInstance(), TokenizerType.KEYWORD_TOKENIZER);

            // Search for the first term
            List<ScoredDocument> originalWordResults = searcher.searchForTerm(words[0]);
            if (words.length == 1) {
                // If there was only 1 term, we're done.  Return the length of the results.
                return originalWordResults.size();
            } else { // You've complicated things a bit.
                // Make a set of all the pdf ids for the original word
                Set<Integer> docIds = originalWordResults.stream().map(ScoredDocument::getDocId).collect(Collectors.toSet());

                // Go through each word
                for (String word : words) {
                    // Keep only the ids for pdfs that contained all of #words#.
                    docIds.retainAll(
                            searcher.searchForTerm(word) // Do the search
                                    .stream().map(ScoredDocument::getDocId).collect(Collectors.toSet()));
                }
                // Return the number of pdfs that the passed through #words# share
                return docIds.size();
            }
        } catch (LuceneSearchException e) {
            // If something weird happens, log an error, and throw an exception
            System.err.println("There was an error with the index searcher.");
            e.printStackTrace();
            return -1;
            // TODO: Throw exception instead
        }
    }
}