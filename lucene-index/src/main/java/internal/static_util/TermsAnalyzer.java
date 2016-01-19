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

package internal.static_util;

import api.exception.LuceneSearchException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import common.Constants;
import common.StopwordsProvider;
import common.data.ScoredTerm;
import internal.analyzers.search.StemmingTermAnalyzer;
import internal.static_util.data.TermDocument;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import utilities.EasyLogger;
import utilities.ListUtils;
import utilities.StringManip;

import java.io.IOException;
import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Chris on 9/24/2015.
 */
public class TermsAnalyzer {
    private static final Log log = LogFactory.getLog(TermsAnalyzer.class);
    private static final Set<String> stopwords;
    static {
        // Ensure stopwords are initialized before stopword regex
        stopwords = StopwordsProvider.getProvider().getStopwords();
    }

    // Yay for accurate caching methods
    private static Cache<TermDocument, List<ScoredTerm>> cache = CacheBuilder.newBuilder().maximumSize(10000).build();

    /**
     * This method uses the getRelatedTerms but uses FullText that is obtained from Lucene.
     *
     * @param reader Use this index api.reader to get the document
     * @param docId  The document ID to get the text from
     * @param term   The term to find related terms to
     * @return Sorted list of related terms
     */
    public static List<ScoredTerm> getRelatedTermsInDocument(IndexReader reader, int docId, String term) throws LuceneSearchException {
        List<ScoredTerm> relatedTerms;

        TermDocument td = TermDocument.of(docId, term);
        try {
            relatedTerms = cache.get(td, () -> getRelatedTerms(reader, docId, term));
        } catch (ExecutionException e) {
            throw new LuceneSearchException("[TermsAnalyzer] ERROR: Error while getting related terms");
        }

        // Don't return null, return an empty list!
        return relatedTerms != null ? relatedTerms : Collections.EMPTY_LIST;
    }

    private static List<ScoredTerm> getRelatedTerms(IndexReader reader, int docId, String term) throws LuceneSearchException{
        String fullText = FullTextExtractor.extractText(reader, docId);
        if (fullText.equals(FullTextExtractor.FAILED_TEXT))
            throw new LuceneSearchException("Failed to extract fulltext");
        return getRelatedTerms(fullText, term);
    }

    /**
     * This method uses the getRelatedTerms but uses FullText that is obtained from Lucene.
     *
     * @param reader Use this index api.reader to get the document
     * @param docId  The document ID to get the text from
     * @param term   The term to find related terms to
     * @param limit  Limits the number of terms that can be returned
     * @return Sorted list of related terms
     */
    public static List<ScoredTerm> getRelatedTermsInDocument(IndexReader reader, int docId, String term, int limit) throws LuceneSearchException {
        return ListUtils.getSublist(getRelatedTermsInDocument(reader, docId, term), limit);
    }

    /**
     * This method gets the related terms in the fullText string based on the term.
     * The Related terms are determined by first breaking the fullText into sentences. The sentences are filtered
     * to only contain those that contain the search term that we are looking for related sentences to.
     * Then, removing punctuation, stopwords, and the search term itself.
     * Then the other terms are stored in a map alongside the number of times that they occur. This map is flattened
     * into a list of ScoredTerms and the score is computed by dividing the frequency by the number of sentences.
     *
     * @param fullText The fulltext of the document
     * @param term     The term to find related terms to
     * @return A sorted list of scored terms.
     */
    public static List<ScoredTerm> getRelatedTerms(String fullText, String term) {
        String sTerm = term;
        try {
            sTerm = TermStemmer.stemTerm(term);
        } catch (ParseException e) {
            log.error("Could not stem term due to a parsing exception.");
        }
        final String stemmedTerm = sTerm;

        List<String> sentences = Arrays.asList(StringManip.splitSentences(fullText));

        // Logging for debug
//        EasyLogger.log(term + "_sentences", sentences.stream().collect(Collectors.joining("\n")));

        // Get the term scores
        List<String> filteredSentences = sentences.parallelStream()
                .filter(s -> s.contains(stemmedTerm))
                .map(StringManip::replaceSmartQuotes) // Handle dumb MS word stuff
                .map(s -> Normalizer.normalize(s, Normalizer.Form.NFD)) // Normalize the text!
                .map(s -> s.replaceAll("\\p{Punct}", " ")) // Remove punctuation
                .map(s -> s.replaceAll("^[\\p{L}\\p{N}]+", " ")) // Remove punctuation, again, just to be sure we got it all.
                .map(s -> StringManip.removeTerm(s, term))
                .map(s -> StringManip.removeTerm(s, stemmedTerm))
                .map(StringManip::removeStopwords) // Remove stop words (:-D)
                .map(StringManip::removeNumbers)
                .map(s -> s.replaceAll("\\s+", " ")) // Remove excessive spaces
                .map(s -> s.replaceAll("^\\s", "")) // Remove starting spaces
                .filter(s -> !s.isEmpty()) // Remove all the empty strings
                .collect(Collectors.toList());

        long numSentences =  filteredSentences.size();
        Map<String, Long> termScores = filteredSentences.stream()
                .map(s -> s.split("\\s")) // Split by words
                .flatMap(Arrays::stream) // Map the string arrays to the stream
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        EasyLogger.log(term + "_term_scores", termScores.entrySet().stream().map(e -> e.getKey() + ": " + e.getValue()).collect(Collectors.joining("\n")));

        // Convert this to a list of scores
        List<ScoredTerm> scores = convertToScoredTerm(termScores, numSentences);

        // Sort in reverse order
        Collections.sort(scores, Collections.reverseOrder());

        return scores;
    }

    /**
     * Get the most common terms in the document based on the api.reader and the document id
     *
     * @param reader The index api.reader
     * @param docId  The document id
     * @return A list of the most common terms (With stopwords removed)
     * @throws LuceneSearchException
     */
    public static List<ScoredTerm> getTerms(IndexReader reader, int docId) throws LuceneSearchException {
        String fullText = FullTextExtractor.extractText(reader, docId);

        if (fullText.equals(FullTextExtractor.FAILED_TEXT))
            throw new LuceneSearchException("Failed to get the full text.");

        String filteredFulltext = Arrays.asList(fullText.split(" ")).parallelStream() // Split based on spaces
                .map(s -> s.replaceAll("\\p{Punct}", "")) // Remove punctuation
                .map(s -> StringManip.removeStopwords(s, stopwords)) // Remove stop words
                .map(s -> s.replaceAll("\\s+", " ")) // Remove excessive spaces
                .map(s -> s.replaceAll("^\\s", "")) // Remove starting spaces
                .collect(Collectors.joining(" "));

        return getTerms(filteredFulltext);
    }

    /**
     * Gets the most common terms within the text
     *
     * @param fullText The text to extract terms from
     * @return A list of scored terms
     * @throws LuceneSearchException
     */
    public static List<ScoredTerm> getTerms(String fullText) throws LuceneSearchException {
        // TODO: Add a limit to the number of terms (otherwise the terms may not be significant

        // Spin up a PDF analyzer.
        Analyzer analyzer = new StemmingTermAnalyzer(System.getenv(Constants.RESOURCE_FOLDER_VAR) + "/" + Constants.STOPWORDS_FILE);

        try {
            TokenStream stream = analyzer.tokenStream(Constants.FIELD_CONTENTS, fullText);
            CharTermAttribute attr = stream.addAttribute(CharTermAttribute.class);
            Map<String, Integer> termScores = new HashMap<>();
            stream.reset();
            while (stream.incrementToken()) {
                String term = attr.toString();
                termScores.put(term, termScores.getOrDefault(term, 0) + 1);
            }
            stream.end();
            stream.close();

            // Get scores and sort
            List<ScoredTerm> scores = convertToScoredTerm(termScores);
            Collections.sort(scores, Collections.reverseOrder());

            return scores;

        } catch (IOException e) {
            e.printStackTrace();
            throw new LuceneSearchException("TermAnalyzer: Failed to produce terms " +
                    "due to an error with the analyzer");
        }
    }

    /**
     * Extracts up to the given number of most common terms
     *
     * @param fullText The full text to extract the terms from
     * @param limit    - The maximum number of terms to be returned in the list
     * @return The top 0-limit terms of the getTerms(fullText) call.
     * @throws LuceneSearchException
     */
    public static List<ScoredTerm> getTopTerms(String fullText, int limit) throws LuceneSearchException {
        List<ScoredTerm> terms = getTerms(fullText);
        return ListUtils.getSublist(terms, limit);
    }

    /**
     * Extracts the top given number of terms from the Lucene index using the given document number
     *
     * @param reader The index api.reader
     * @param docId  The document ID
     * @param limit  The maximum number of terms to returm
     * @return A list of [limit] size of index terms
     * @throws LuceneSearchException
     */
    public static List<ScoredTerm> getTopTerms(IndexReader reader, int docId, int limit) throws LuceneSearchException {
        List<ScoredTerm> terms = getTerms(reader, docId);
        return ListUtils.getSublist(terms, limit);
    }


    /**
     * Converts the map of terms to a List of scored terms
     *
     * @param terms Map of terms
     * @return List of scored terms
     */
    public static List<ScoredTerm> convertToScoredTerm(Map<String, Integer> terms) {
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

    // Make the terms analyzer private so that you can't create one (static class)
    private TermsAnalyzer() {}
}