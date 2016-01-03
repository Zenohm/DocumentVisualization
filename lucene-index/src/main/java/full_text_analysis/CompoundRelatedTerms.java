package full_text_analysis;

import access_utils.FullTextTokenizer;
import access_utils.TermLocationsSearcher;
import access_utils.data.TermLocations;
import analyzers.filters.NumberFilter;
import analyzers.search.SearchAnalyzer;
import common.Constants;
import common.data.ScoredTerm;
import data.StopwordsProvider;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.document.Document;
import reader.IndexReader;
import searcher.exception.LuceneSearchException;
import util.Searcher;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * This class implements a compound related terms search.
 * Created by chris on 12/16/15.
 */
public class CompoundRelatedTerms extends Searcher{
    private static Map<Integer, String[]> tokenCache = new ConcurrentHashMap<>();
    private int cacheHits = 0;
    private int cacheMisses = 0;

    private Set<String> stopwords =
            StopwordsProvider.getProvider().getStopwords();
    public CompoundRelatedTerms(IndexReader reader, String stopwordFile) throws LuceneSearchException {
        super(reader, new SearchAnalyzer(KeywordTokenizer.class));
        stopwords = StopwordsProvider.getProvider(stopwordFile).getStopwords();
    }

    public List<ScoredTerm> getCompoundRelatedTerms(String term) throws LuceneSearchException{
        // TODO: Potentially stem the term that we are looking for?
        TermLocationsSearcher tlSearcher = new TermLocationsSearcher(reader);
        List<TermLocations> termLocations = tlSearcher.getLocationsOfTerm(term);

        cacheHits = 0;
        cacheMisses = 0;
        // Goes through terms list to determine the potential compound terms.
        Set<String> potentialCompoundTerms = Collections.newSetFromMap(new ConcurrentHashMap<>());
        getStream(termLocations).forEach(loc ->{
            String[] contents = getTokenizedText(loc.docId);
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

//        System.out.println("Token Cache Hits: " + cacheHits + " Token Cache Misses: " + cacheMisses);

        return TermRelatednessScorer.getRankedTermsWithScores(term, potentialCompoundTerms, 0);
    }

    private <E> Stream<E> getStream(List<E> list){
        if(list.size() > 100){
            return list.parallelStream();
        }else{
            return list.stream();
        }
    }

    private String[] getTokenizedText(int docId){
        if(tokenCache.containsKey(docId)){
            cacheHits++;
            return tokenCache.get(docId);
        }

        cacheMisses++;
        Document doc;
        try {
            doc = reader.getReader().document(docId);
        } catch (IOException e) {
            System.err.println("There was an error getting document " + docId + ": " + e.getMessage());
            return null;
        }

        String[] contents;
        try {
            String[] values = doc.getValues(Constants.FIELD_CONTENTS);
            String totalContent = "";
            for(String content : values){
                totalContent += content + " ";
            }
            contents = FullTextTokenizer.tokenizeText(totalContent);
            tokenCache.put(docId, contents);
        } catch (IOException e) {
            System.err.println("There was an error while tokenizing the text for " + docId + ": " + e.getMessage());
            return null;
        }

        return contents;
    }



}
