package api.term_search;

import api.exception.LuceneSearchException;
import api.reader.IndexReader;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import common.data.ScoredTerm;
import common.dict.Dictionary;
import common.dict.SetDictionary;
import common.dict.TrueSetDictionary;
import exception.SearchException;
import internal.lucene_intf.LuceneReader;
import internal.static_util.FullTextExtractor;
import internal.static_util.TermStemmer;
import internal.static_util.data.TermDocument;
import internal.static_util.scorer.TermRelatednessScorer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.queryparser.classic.ParseException;
import term_search.DocumentRelatedTermsSearcher;
import utilities.StringManip;

import java.text.Normalizer;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by chris on 1/8/16.
 */
public class SentenceRelatedTerms extends LuceneReader implements DocumentRelatedTermsSearcher{

    private static final Log log = LogFactory.getLog(SentenceRelatedTerms.class);
    // Yay for accurate caching methods
    private static Cache<TermDocument, List<ScoredTerm>> cache = CacheBuilder.newBuilder().maximumSize(10000).build();

    public SentenceRelatedTerms() throws SearchException {
    }

    public SentenceRelatedTerms(IndexReader reader) throws SearchException{
        super(reader);
    }

    /**
     * This method uses the getRelatedTerms but uses FullText that is obtained from Lucene.
     *
     * @param docId  The document ID to get the text from
     * @param term   The term to find related terms to
     * @return Sorted list of related terms
     */
    @Override
    public List<ScoredTerm> getDocumentRelatedTerms(int docId, String term) throws LuceneSearchException {
        List<ScoredTerm> relatedTerms;

        TermDocument td = TermDocument.of(docId, term);
        try {
            relatedTerms = cache.get(td, () -> getRelatedTerms(docId, term));
        } catch (ExecutionException e) {
            throw new LuceneSearchException(getClass().toString() + ": ERROR: Error while getting related terms");
        }

        // Don't return null, return an empty list!
        return relatedTerms != null ? relatedTerms : Collections.EMPTY_LIST;
    }

    // Private because this is the uncached version, use cached version, getDocumentRelatedTerms instead.e
    private List<ScoredTerm> getRelatedTerms(int docId, String term) throws LuceneSearchException{
        String fullText = FullTextExtractor.extractText(reader, docId);
        if (fullText.equals(FullTextExtractor.FAILED_TEXT))
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
     * @return         A sorted list of scored terms.
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
                .map(StringManip::removeSmartQuotes) // Handle dumb MS word stuff
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

        Dictionary d = SetDictionary.getInstance();
        Dictionary dict = d == null ? new TrueSetDictionary() : d;

        long numSentences =  filteredSentences.size();
        Map<String, Long> termScores = filteredSentences.stream()
                .map(s -> s.split("\\s")) // Split by words
                .flatMap(Arrays::stream) // Map the string arrays to the stream
                .filter(dict::contains) // Ensure that only real words are allowed
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Set<String> terms = termScores.keySet();

        // Remove single occurance terms
        termScores = termScores.entrySet().stream()
                .filter(e -> e.getValue() != 1)
                .filter(t -> {
                    try {
                        String termStem = TermStemmer.stemTerm(t.getKey());
                        if(terms.contains(termStem))
                            return false;
                        if(termStem.equals(t.getKey()))
                            return true;
                        if(termStem.equals(term))
                            return false;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return true;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // Convert this to a list of scores
        List<ScoredTerm> scores = termScores.keySet().stream()
                .map(s -> new ScoredTerm(s, TermRelatednessScorer.score(term, s)))
                .collect(Collectors.toList());

        // Sort in reverse order
        Collections.sort(scores, Collections.reverseOrder());

        return scores;
    }


}
