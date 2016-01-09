package data_processing.related_terms_combiner.data;

import data_processing.related_terms_combiner.CombinedRelatedTerms;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 12/30/15.
 */
public class RelatedTermResult {
    public final String term;
    public final int docId;
    private final List<RelatedTerm> rterms;

    private RelatedTermResult(String term, int docId) {
        this.term = term;
        this.docId = docId;
        rterms = new ArrayList<>();
    }

    public static RelatedTermResult createResult(CombinedRelatedTerms crt, String term, int docId){
        RelatedTermResult result = new RelatedTermResult(term, docId);
        result.rterms.addAll(crt.getRelatedTerms(term, docId));
        return result;
    }

    /**
     * Returns the results
     * @return returns the list of the related terms
     */
    public List<RelatedTerm> getResults(){
        return new ArrayList<>(rterms);
    }
}
