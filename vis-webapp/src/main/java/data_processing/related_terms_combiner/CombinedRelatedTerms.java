package data_processing.related_terms_combiner;

import api.exception.LuceneSearchException;
import api.reader.LuceneIndexReader;
import api.term_search.CompoundRelatedTerms;
import api.term_search.SentenceRelatedTerms;
import common.Constants;
import common.data.ScoredTerm;
import data_processing.related_terms_combiner.data.RelatedTerm;
import exception.SearchException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import synonyms.SynonymAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Created by chris on 12/30/15.
 */
public class CombinedRelatedTerms {
    private static final Log log = LogFactory.getLog(CombinedRelatedTerms.class);
    private CompoundRelatedTerms crt;
    private SentenceRelatedTerms srt;

    public CombinedRelatedTerms(){
        String resourceDirectory = System.getProperty(Constants.RESOURCE_FOLDER_VAR);
        String stopwordsFile = resourceDirectory + "/" + Constants.STOPWORDS_FILE;
        instantiate(stopwordsFile);
    }

    public CombinedRelatedTerms(String stopwordsFile){
        instantiate(stopwordsFile);
    }

    private void instantiate(String stopwordsFile){
        try {
            crt = new CompoundRelatedTerms(LuceneIndexReader.getInstance(), stopwordsFile);
            srt = new SentenceRelatedTerms();
        } catch (SearchException e) {
            log.error("Compound Related Terms Generator Could Not be Created: " + e.getMessage());
        }
    }

    public List<RelatedTerm> getRelatedTerms(String term, int docId){
        // Get the compound related terms
        ExecutorService pool = Executors.newFixedThreadPool(3);

        long startTime = System.nanoTime();
        // Compound Related Terms
        Future <List<ScoredTerm>> crtFuture = pool.submit(()-> getCompoundRelatedTerms(term));

        // Sentence Related Terms
        Future<List<ScoredTerm>> srtFuture = pool.submit(()-> getSentenceRelatedTerms(term, docId));

        // Synonym related terms
        Future<List<ScoredTerm>> synFuture = pool.submit(()-> getSynonyms(term));

        // Combine related terms
        List<RelatedTerm> combinedTerms;
        try {
            combinedTerms = combineRelatedTerms(crtFuture.get(), srtFuture.get(), synFuture.get());
        } catch (InterruptedException | ExecutionException e) {
            log.error("There was an error combining related terms: " + e.getMessage());
            return Collections.EMPTY_LIST;
        }

        log.info("Total Time To Produce combined related terms to " + term + " in document # "+ docId + ": " + Double.toString((System.nanoTime() - startTime)/Math.pow(10, 9)));
        combinedTerms.sort(Comparator.reverseOrder());

        return combinedTerms;
    }

    private List<ScoredTerm> getCompoundRelatedTerms(String term){
        List<ScoredTerm> compoundRelatedTerms;
        long crtStart = System.nanoTime();
        try {
            if(crt != null){
                compoundRelatedTerms = crt.getRelatedTerms(term);
                log.info("Total Time to produce compound related terms to " + term + ": " + (System.nanoTime() - crtStart)/Math.pow(10,9) +
                ". Got " + compoundRelatedTerms.size() + " compound related terms.");
            }else{
                log.error("Compound related terms generator was not initialized");
                compoundRelatedTerms = null;
            }
        } catch (LuceneSearchException e) {
            e.printStackTrace();
            compoundRelatedTerms = null;
        }

        return compoundRelatedTerms;
    }

    private List<ScoredTerm> getSentenceRelatedTerms(String term, int docId){
        // Get the sentence related terms
        List<ScoredTerm> sentenceRelatedTerms = null;
        long srtStart = System.nanoTime();
        try {
            sentenceRelatedTerms = srt.getDocumentRelatedTerms(docId, term);
            log.info("Total Time to produce sentence related terms to " + term + " in document # " + docId + ": " + (System.nanoTime() - srtStart)/Math.pow(10, 9) +
                    ". Got " + sentenceRelatedTerms.size() + " sentence related terms");
        } catch (SearchException e) {
            log.error("Sentence Related Terms Could not be obtained");
            e.printStackTrace();
        }
        return sentenceRelatedTerms;
    }

    private List<ScoredTerm> getSynonyms(String term){
        long synStart = System.nanoTime();
        List<ScoredTerm> synTerms = SynonymAdapter.getScoredSynonymsWithMinimalRelation(term);
        log.info("Total time to produce synonyms to " + term + ": " + (System.nanoTime() - synStart)/Math.pow(10, 9) +
        ". Got " + synTerms.size() + " synonyms.");
        return synTerms;
    }

    private List<RelatedTerm> combineRelatedTerms(List<ScoredTerm> compound, List<ScoredTerm> sentence, List<ScoredTerm> synonyms){
        // TODO: Need to normalize scores! Probably...
        List<RelatedTerm> allTerms = new ArrayList<>();
        if(compound != null && !compound.isEmpty()){
            allTerms.addAll(compound.stream()
                    .map(rt ->  RelatedTerm.convertScoredTerm(rt, RelatedTerm.RelatedTermType.Compound))
                    .collect(Collectors.toList()));
        }
        if(sentence != null && !sentence.isEmpty()) {
            allTerms.addAll(sentence.stream()
                    .map(rt -> RelatedTerm.convertScoredTerm(rt, RelatedTerm.RelatedTermType.Sentence))
                    .collect(Collectors.toList()));
        }
        if(synonyms != null && !synonyms.isEmpty()){
            allTerms.addAll(synonyms.stream()
                    .map(rt -> RelatedTerm.convertScoredTerm(rt, RelatedTerm.RelatedTermType.Synonym))
                    .collect(Collectors.toList()));
        }

        return allTerms;
    }
}
