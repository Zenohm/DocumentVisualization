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

package searcher;

import common.ScoredTerm;
import indexer.PDFAnalyzer;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.IndexReader;
import searcher.exception.LuceneSearchException;
import searcher.reader.LuceneIndexReader;
import util.FullTextExtractor;
import util.IndexerConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Chris on 9/24/2015.
 */
public class TermsAnalyzer {
    private static final List<String> STOPWORDS = new ArrayList<>();

    // Initialize the stopwords with the stopwords file
    static {
        String filename = System.getenv(IndexerConstants.RESOURCE_FOLDER_VAR) + "/" + IndexerConstants.STOPWORDS_FILE;
        Scanner s = null;
        try {
            s = new Scanner(new File(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (s != null && s.hasNextLine()) {
            STOPWORDS.add(s.nextLine());
        }
        if (s != null) s.close();
    }

    /**
     * This method uses the getRelatedTerms but uses FullText that is obtained from Lucene.
     * @param reader Use this index reader to get the document
     * @param docId  The document ID to get the text from
     * @param term   The term to find related terms to
     * @return Sorted list of related terms
     */
    public static List<ScoredTerm> getRelatedTermsInDocument(IndexReader reader, int docId, String term) throws LuceneSearchException {
        String fullText = FullTextExtractor.extractFullText(reader, docId);

        if(fullText.equals(FullTextExtractor.FAILED_TEXT))
            throw new LuceneSearchException("Failed to extract fulltext");

        return getRelatedTerms(fullText, term);
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
        long startTime = System.nanoTime();

        // Oh look stopwords
        String stopwordRegex = getStopwordRegex();

        // Get sentences
        List<String> sentences = Arrays.asList(splitSentences(fullText)).stream()
                .map(s -> s.replaceAll("\\p{Punct}", "")) // Remove punctuation
                .map(s -> s.replaceAll(stopwordRegex, " ")) // Remove stop words
                .map(s -> s.replaceAll("\\s+", " ")) // Remove excessive spaces
                .map(s -> s.replaceAll("^\\s", "")) // Remove starting spaces
                .filter(s -> s.contains(term)) // Filter to only those sentences containing a term
                .map(s -> s.replaceAll("\\s*\\b" + term + "\\b\\s*", "")) // Remove the term from the sentence
                .collect(Collectors.toList());

        // Remove all the null or empty strings
        sentences.removeAll(Collections.singletonList(""));

        // Print all the sentences (for debug)
        sentences.stream().forEach(System.out::println);

        // Collect all the scores
        Map<String, Integer> termScores = new HashMap<>();
        for (String s : sentences) {
            String[] tokens = s.split("\\s");
            for (String t : tokens) {
                termScores.put(t, termScores.getOrDefault(t, 0) + 1);
            }
        }

        // Convert this to a list of scores
        List<ScoredTerm> scores = convertToScoredTerm(termScores, sentences.size());

        // Sort in reverse order
        Collections.sort(scores, Collections.reverseOrder());

        long endTime = System.nanoTime();
        System.out.println("Total time to produce related terms: " + ((endTime - startTime) / Math.pow(10, 9)));

        return scores;
    }

    /**
     * Get all the stopwords as a regular expression
     * @return The stop words as a regular expression
     */
    private static String getStopwordRegex() {
        return StringUtils.join(STOPWORDS.stream().map(s -> {
            String newRegex = "\\s*\\b";
            newRegex += s;
            newRegex += "\\b\\s*";
            return newRegex;
        }).collect(Collectors.toList()), "|");
    }

    /**
     * Get the most common terms in the document based on the reader and the document id
     * @param reader The index reader
     * @param docId The document id
     * @return A list of the most common terms (With stopwords removed)
     * @throws LuceneSearchException
     */
    public static List<ScoredTerm> getTerms(IndexReader reader, int docId) throws LuceneSearchException{
        String fullText = FullTextExtractor.extractFullText(reader, docId);

        if (fullText.equals(FullTextExtractor.FAILED_TEXT))
            throw new LuceneSearchException("Failed to get the full text.");

        // Get the regular expression for the stopwords
        String stopwordRegex = getStopwordRegex();

        String filteredFulltext = Arrays.asList(fullText.split(" ")).stream() // Split based on spaces
                .map(s -> s.replaceAll("\\p{Punct}", "")) // Remove punctuation
                .map(s -> s.replaceAll(stopwordRegex, " ")) // Remove stop words
                .map(s -> s.replaceAll("\\s+", " ")) // Remove excessive spaces
                .map(s -> s.replaceAll("^\\s", "")) // Remove starting spaces
                .collect(Collectors.joining(" "));

        return getTerms(filteredFulltext);
    }

    /**
     * Gets the most common terms within the text
     * @param fullText The text to extract terms from
     * @return A list of scored terms
     * @throws LuceneSearchException
     */
    public static List<ScoredTerm> getTerms(String fullText) throws LuceneSearchException{
        // TODO: Add a limit to the number of terms (otherwise the terms may not be significant

        // Spin up a PDF analyzer.
        PDFAnalyzer analyzer = new PDFAnalyzer(System.getenv(IndexerConstants.RESOURCE_FOLDER_VAR) + "/" + IndexerConstants.STOPWORDS_FILE);

        try {
            TokenStream stream = analyzer.tokenStream(IndexerConstants.FIELD_CONTENTS, fullText);
            CharTermAttribute attr = stream.addAttribute(CharTermAttribute.class);
            Map<String, Integer> termScores = new HashMap<>();
            stream.reset();
            while(stream.incrementToken()){
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
     * @param fullText The full text to extract the terms from
     * @param limit - The maximum number of terms to be returned in the list
     * @return The top 0-limit terms of the getTerms(fullText) call.
     * @throws LuceneSearchException
     */
    public static List<ScoredTerm> getTopTerms(String fullText, int limit) throws LuceneSearchException {
        List<ScoredTerm> terms = getTerms(fullText);
        return limitTermSize(terms, limit);
    }

    /**
     * Extracts the top given number of terms from the Lucene index using the given document number
     * @param reader The index reader
     * @param docId The document ID
     * @param limit The maximum number of terms to returm
     * @return A list of [limit] size of index terms
     * @throws LuceneSearchException
     */
    public static List<ScoredTerm> getTopTerms(IndexReader reader, int docId, int limit) throws LuceneSearchException {
        List<ScoredTerm> terms = getTerms(reader, docId);
        return limitTermSize(terms, limit);
    }


    private static List<ScoredTerm> limitTermSize(List<ScoredTerm> terms, int limit) {
        if(terms.size() < limit) {
            limit = terms.size();
        }
        return new ArrayList<>(terms.subList(0, limit));
    }


    /**
     * Splits a text into an array of sentences.
     * @param text Text to split into individual sentences
     * @return An array of strings that contain sentences
     */
    private static String[] splitSentences(String text) {
        return text.split("(?<=[.!?])\\s*");
    }


    /**
     * Converts the map of terms to a List of scored terms
     * @param terms Map of terms
     * @return List of scored terms
     */
    public static List<ScoredTerm> convertToScoredTerm(Map<String, Integer> terms){
        return convertToScoredTerm(terms, 1.0);
    }

    /**
     * Converts the map of terms to a list of scored terms, uses the normalizer that is given
     * @param terms The map of terms to use
     * @param normalizer a normalizing constant
     * @return List of scored terms
     */
    public static List<ScoredTerm> convertToScoredTerm(Map<String, Integer> terms, double normalizer){
        return terms.entrySet().stream()
                .map(e -> new ScoredTerm(e.getKey(), (double) e.getValue() / normalizer))
                .collect(Collectors.toList());
    }

    // Make the terms analyzer private so that you can't create one (static class)
    private TermsAnalyzer() {}
}
