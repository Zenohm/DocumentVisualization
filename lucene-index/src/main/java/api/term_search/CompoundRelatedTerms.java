package api.term_search;

import api.exception.LuceneSearchException;
import api.reader.IndexReader;
import common.StopwordsProvider;
import common.data.ScoredTerm;
import common.data.TermLocations;
import common.dict.Dictionary;
import common.dict.SetDictionary;
import common.dict.TrueSetDictionary;
import internal.analyzers.search.SearchAnalyzer;
import internal.lucene_intf.Searcher;
import internal.static_util.scorer.TermRelatednessScorer;
import internal.static_util.tokenizer.DocumentTokenizer;
import internal.term_utils.TermLocationsSearcher;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import term_search.RelatedTermsSearcher;
import utilities.StringFilters;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * This class implements a compound related terms search.
 * Created by chris on 12/16/15.
 */
public class CompoundRelatedTerms extends Searcher implements RelatedTermsSearcher {
    private static final Log log = LogFactory.getLog(CompoundRelatedTerms.class);
    private Set<String> stopwords = StopwordsProvider.getProvider().getStopwords();
    public CompoundRelatedTerms(IndexReader reader, String stopwordFile) throws LuceneSearchException {
        super(reader, new SearchAnalyzer(KeywordTokenizer.class));
        stopwords = StopwordsProvider.getProvider(stopwordFile).getStopwords();
    }

    @Override
    public List<ScoredTerm> getRelatedTerms(String term) throws LuceneSearchException{
        TermLocationsSearcher tlSearcher = new TermLocationsSearcher(reader);
        List<TermLocations> termLocations = tlSearcher.getLocationsOfTerm(term);

        // Goes through terms list to determine the potential compound terms.
        Set<String> potentialCompoundTerms = Collections.newSetFromMap(new ConcurrentHashMap<>());

        // For each term location, get its potential compound terms.
        getStream(termLocations).forEach(loc -> potentialCompoundTerms.addAll(getPotentialCompoundTerms(loc)));

        return TermRelatednessScorer.getRankedTermsWithScores(term, potentialCompoundTerms, 0);
    }

    private Set<String> getPotentialCompoundTerms(TermLocations loc){
        Set<String> potentialCompoundTerms = Collections.newSetFromMap(new ConcurrentHashMap<>());
        String[] contents = DocumentTokenizer.getInstance().getTokenizedText(loc.docId);
        if(contents == null){
            log.error("Error Getting Tokenized Contents for: " + loc.docId);
            return Collections.EMPTY_SET;
        }
        Dictionary d = SetDictionary.getInstance();
        final Dictionary dict = d == null ? new TrueSetDictionary() : d;


        getStream(loc.getLocations())
                .filter(location -> location + 1 < contents.length)
                .map(location -> new ImmutablePair<>(contents[location].toLowerCase().trim(),
                        contents[location + 1].toLowerCase().trim()))
                .filter(content -> !stopwords.contains(content.getRight()))
                .filter(content -> !StringFilters.isNumeric(content.getRight()))
                .filter(content -> dict.contains(content.getRight().toLowerCase()))
                .map(content -> content.getLeft() + " " + content.getRight())
                .forEach(potentialCompoundTerms::add);

        getStream(loc.getLocations())
                .filter(location -> location - 1 >= 0)
                .map(location -> new ImmutablePair<>(contents[location].toLowerCase().trim(),
                        contents[location - 1].toLowerCase().trim()))
                .filter(content -> !stopwords.contains(content.getRight()))
                .filter(content -> !StringFilters.isNumeric(content.getRight()))
                .filter(content -> dict.contains(content.getRight().toLowerCase()))
                .map(content -> content.getRight() + " " + content.getLeft())
                .forEach(potentialCompoundTerms::add);

        return potentialCompoundTerms;
    }

    private <E> Stream<E> getStream(List<E> list){
        if(list.size() > 100){
            return list.parallelStream();
        }else{
            return list.stream();
        }
    }
}
