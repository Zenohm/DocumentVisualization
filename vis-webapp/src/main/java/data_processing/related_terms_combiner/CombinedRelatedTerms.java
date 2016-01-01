package data_processing.related_terms_combiner;

import common.Constants;
import common.data.ScoredTerm;
import data_processing.related_terms_combiner.data.RelatedTerm;
import data_processing.related_terms_combiner.data.RelatedTermType;
import full_text_analysis.CompoundRelatedTerms;
import full_text_analysis.TermsAnalyzer;
import reader.LuceneIndexReader;
import searcher.exception.LuceneSearchException;
import synonyms.SynonymAdapter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chris on 12/30/15.
 */
public class CombinedRelatedTerms {

    private CompoundRelatedTerms crt;

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
        } catch (LuceneSearchException e) {
            System.err.println("ERROR: Compound Related Terms Generator Could Not be Created");
            e.printStackTrace();
        }
    }

    public List<RelatedTerm> getRelatedTerms(String term, int docId){
        // Get the compound related terms
        List<ScoredTerm> compoundRelatedTerms = null;
        try {
            if(crt != null){
                compoundRelatedTerms = crt.getCompoundRelatedTerms(term);
            }else{
                System.err.println("ERROR: Compound related terms generator was not initialized");
            }
        } catch (LuceneSearchException e) {
            e.printStackTrace();
        }

        // Get the sentence related terms
        List<ScoredTerm> sentenceRelatedTerms = null;
        try {
            sentenceRelatedTerms = TermsAnalyzer.getTerms(LuceneIndexReader.getInstance().getReader(), docId);
        } catch (LuceneSearchException e) {
            System.err.println("ERROR: Sentence Related Terms Could not be obtained");
            e.printStackTrace();
        }

        // Get the synonyms
        List<ScoredTerm> synonyms = SynonymAdapter.getScoredSynonymsWithMinimalRelation(term);

        // Combined the related terms
        List<RelatedTerm> combinedTerms = combineRelatedTerms(compoundRelatedTerms,
                                                              sentenceRelatedTerms,
                                                              synonyms);

        combinedTerms.sort(Comparator.reverseOrder());

        return combinedTerms;
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
