package api.term_search;

import internal.term_utils.TermLocationsSearcher;
import common.data.TermLocations;
import internal.analyzers.filters.NumberFilter;
import internal.analyzers.search.SearchAnalyzer;
import common.data.ScoredTerm;
import common.StopwordsProvider;
import internal.static_util.scorer.TermRelatednessScorer;
import internal.static_util.tokenizer.DocumentTokenizer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import api.reader.IndexReader;
import api.exception.LuceneSearchException;
import term_search.RelatedTermsSearcher;
import internal.lucene_intf.Searcher;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * This class implements a compound related terms search.
 * Created by chris on 12/16/15.
 */
public class CompoundRelatedTerms extends Searcher implements RelatedTermsSearcher {

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
        getStream(termLocations).forEach(loc ->{
            String[] contents = DocumentTokenizer.getInstance().getTokenizedText(loc.docId);
            if(contents == null){
                System.err.println("Error Getting Tokenized Contents for: " + loc.docId);
                return;
            }

            getStream(loc.getLocations())
                    .filter(location -> location + 1 < contents.length)
                    .map(location -> new ImmutablePair<>(contents[location].toLowerCase().trim(),
                            contents[location + 1].toLowerCase().trim()))
                    .filter(content -> !stopwords.contains(content.getRight()))
                    .filter(content -> !NumberFilter.isNumeric(content.getRight()))
                    .map(content -> content.getLeft() + " " + content.getRight())
                    .forEach(potentialCompoundTerms::add);

            getStream(loc.getLocations())
                    .filter(location -> location - 1 >= 0)
                    .map(location -> new ImmutablePair<>(contents[location].toLowerCase().trim(),
                            contents[location - 1].toLowerCase().trim()))
                    .filter(content -> !stopwords.contains(content.getRight()))
                    .filter(content -> !NumberFilter.isNumeric(content.getRight()))
                    .map(content -> content.getRight() + " " + content.getLeft())
                    .forEach(potentialCompoundTerms::add);
        });

        return TermRelatednessScorer.getRankedTermsWithScores(term, potentialCompoundTerms, 0);
    }

    private <E> Stream<E> getStream(List<E> list){
        if(list.size() > 100){
            return list.parallelStream();
        }else{
            return list.stream();
        }
    }
}
