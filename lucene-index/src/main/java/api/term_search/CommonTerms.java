package api.term_search;

import api.exception.LuceneSearchException;
import api.reader.IndexReader;
import common.ScoredTermConverter;
import common.StopwordsProvider;
import common.data.ScoredTerm;
import exception.SearchException;
import internal.lucene_intf.LuceneReader;
import internal.static_util.FullTextExtractor;
import internal.static_util.tokenizer.FullTextTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import term_search.CommonTermsSearcher;
import utilities.StringFilters;
import utilities.StringManip;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by chris on 1/21/16.
 */
public class CommonTerms extends LuceneReader implements CommonTermsSearcher {
    private static final Log log = LogFactory.getLog(CommonTerms.class);

    public CommonTerms() throws LuceneSearchException {
    }

    public CommonTerms(IndexReader reader) throws SearchException{
        super(reader);
    }

    @Override
    public List<ScoredTerm> getCommonTerms(int docId) throws SearchException{
        String fullText = FullTextExtractor.extractText(reader, docId);

        if (fullText.equals(FullTextExtractor.FAILED_TEXT))
            throw new LuceneSearchException("Failed to get the full text.");

        List<ScoredTerm> terms = null;
        try {
            String[] tokens = FullTextTokenizer.tokenizeText(fullText);
            StopwordsProvider stop = StopwordsProvider.getProvider();
            Map<String, Long> termCounts = Arrays.asList(tokens).parallelStream()
                    .map(StringManip::removeSmartQuotes)
                    .map(s -> Normalizer.normalize(s, Normalizer.Form.NFD))
                    .filter(s -> !StringFilters.isNumeric(s))
                    .map(s -> s.replaceAll("\\p{Punct}", " "))
                    .map(s -> s.replaceAll("^[\\p{L}\\p{N}]+", " ")) // Remove punctuation, again, just to be sure we got it all.
                    .filter(stop::notStopword)
                    .map(s -> s.replaceAll("\\s+", "")) // Remove spaces
                    .map(s -> s.replaceAll("^\\s+", ""))
                    .filter(s -> s.equals(""))
                    .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

            terms = ScoredTermConverter.convertToScoredTerm(termCounts);
        }catch (IOException e) {
            e.printStackTrace();
        }

        return terms != null ? terms : Collections.emptyList();
    }
}
