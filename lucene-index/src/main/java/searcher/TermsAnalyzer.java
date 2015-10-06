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

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.index.IndexReader;
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
    public static List<ScoredTerm> getRelatedTermsInDocument(IndexReader reader, int docId, String term) {
        try {

            // Remove all the newlines
            String fullText = reader.document(docId).get(IndexerConstants.FIELD_CONTENTS)
                    .replaceAll("\\r\\n|\\r|\\n", " ").toLowerCase();

            return getRelatedTerms(fullText, term);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
        String stopwordRegex = StringUtils.join(STOPWORDS.stream().map(s -> {
            String newRegex = "\\s*\\b";
            newRegex += s;
            newRegex += "\\b\\s*";
            return newRegex;
        }).collect(Collectors.toList()), "|");

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
        List<ScoredTerm> scores = termScores.entrySet().stream()
                .map(e -> new ScoredTerm(e.getKey(), (double) e.getValue() / sentences.size()))
                .collect(Collectors.toList());

        // Sort in reverse order
        Collections.sort(scores, Collections.reverseOrder());

        long endTime = System.nanoTime();
        System.out.println("Total time to produce related terms: " + ((endTime - startTime) / Math.pow(10, 9)));

        return scores;
    }

    /**
     * Splits a text into an array of sentences.
     * @param text Text to split into individual sentences
     * @return An array of strings that contain sentences
     */
    private static String[] splitSentences(String text) {
        return text.split("(?<=[.!?])\\s*");
    }
}
