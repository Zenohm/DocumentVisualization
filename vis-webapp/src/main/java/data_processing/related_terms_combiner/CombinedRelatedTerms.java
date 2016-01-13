package data_processing.related_terms_combiner;

import api.term_search.SentenceRelatedTerms;
import common.Constants;
import common.data.ScoredTerm;
import data_processing.related_terms_combiner.data.RelatedTerm;
import data_processing.related_terms_combiner.data.RelatedTermType;
import api.term_search.CompoundRelatedTerms;
import exception.SearchException;
import api.reader.LuceneIndexReader;
import api.exception.LuceneSearchException;
import synonyms.SynonymAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by chris on 12/30/15.
 */
public class CombinedRelatedTerms {

    private CompoundRelatedTerms crt;
    private SentenceRelatedTerms srt;

    public CombinedRelatedTerms(){
        String resourceDirectory = System.getenv(Constants.RESOURCE_FOLDER_VAR);
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
        } catch (LuceneSearchException e) {
            System.err.println("ERROR: Compound Related Terms Generator Could Not be Created");
            e.printStackTrace();
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
            System.err.println("[CombinedRelatedTerms] : ERROR: There was an error while geting combined related terms");
            e.printStackTrace();
            return Collections.EMPTY_LIST;
        }

        System.out.println("Total Time To Produce combined related terms to " + term + " in document # "+ docId + ": " + Double.toString((System.nanoTime() - startTime)/Math.pow(10, 9)));
        combinedTerms.sort(Comparator.reverseOrder());

        return combinedTerms;
    }

    private List<ScoredTerm> getCompoundRelatedTerms(String term){
        List<ScoredTerm> compoundRelatedTerms;
        long crtStart = System.nanoTime();
        try {
            if(crt != null){
                compoundRelatedTerms = crt.getRelatedTerms(term);
            }else{
                System.err.println("ERROR: Compound related terms generator was not initialized");
                compoundRelatedTerms = null;
            }
        } catch (LuceneSearchException e) {
            e.printStackTrace();
            compoundRelatedTerms = null;
        }
        System.out.println("Total Time to produce compound related terms to " + term + ": " + (System.nanoTime() - crtStart)/Math.pow(10,9));
        return compoundRelatedTerms;
    }

    private List<ScoredTerm> getSentenceRelatedTerms(String term, int docId){
        // Get the sentence related terms
        List<ScoredTerm> sentenceRelatedTerms = null;
        long srtStart = System.nanoTime();
        try {
            sentenceRelatedTerms = srt.getDocumentRelatedTerms(docId, term);
        } catch (SearchException e) {
            System.err.println("ERROR: Sentence Related Terms Could not be obtained");
            e.printStackTrace();
        }
        System.out.println("Total Time to produce sentence related terms to " + term + " in document # " + docId + ": " + (System.nanoTime() - srtStart)/Math.pow(10, 9) +
                ". Got " + sentenceRelatedTerms.size() + " sentence related terms");
        return sentenceRelatedTerms;
    }

    private List<ScoredTerm> getSynonyms(String term){
        long synStart = System.nanoTime();
        List<ScoredTerm> synTerms = SynonymAdapter.getScoredSynonymsWithMinimalRelation(term);
        System.out.println("Total time to produce synonyms to " + term + ": " + (System.nanoTime() - synStart)/Math.pow(10, 9));
        return synTerms;
    }

    private List<RelatedTerm> combineRelatedTerms(List<ScoredTerm> compound, List<ScoredTerm> sentence, List<ScoredTerm> synonyms){
        // TODO: Need to normalize scores!
        List<RelatedTerm> allTerms = new ArrayList<>();
        if(compound != null && !compound.isEmpty()){
            allTerms.addAll(compound.stream()
                    .map(rt ->  RelatedTerm.convertScoredTerm(rt, RelatedTermType.Compound))
                    .collect(Collectors.toList()));
        }
        if(sentence != null && !sentence.isEmpty()) {
            allTerms.addAll(sentence.stream()
                    .map(rt -> RelatedTerm.convertScoredTerm(rt, RelatedTermType.Sentence))
                    .collect(Collectors.toList()));
        }
        if(synonyms != null && !synonyms.isEmpty()){
            allTerms.addAll(synonyms.stream()
                    .map(rt -> RelatedTerm.convertScoredTerm(rt, RelatedTermType.Synonym))
                    .collect(Collectors.toList()));
        }

        return allTerms;
    }
}
