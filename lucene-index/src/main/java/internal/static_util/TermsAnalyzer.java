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
import common.Constants;
import common.ScoredTermConverter;
import common.StopwordsProvider;
import common.data.ScoredTerm;
import internal.analyzers.search.StemmingTermAnalyzer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.IndexReader;
import utilities.ListUtils;
import utilities.StringManip;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
            List<ScoredTerm> scores = ScoredTermConverter.convertToScoredTerm(termScores);
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


    // Make the terms analyzer private so that you can't create one (static class)
    private TermsAnalyzer() {}
}
