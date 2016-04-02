package data_processing.related_terms_combiner.data;

import common.data.ScoredTerm;

/**
 * Created by chris on 12/30/15.
 */
public class RelatedTerm extends ScoredTerm{
    public final RelatedTermType type;

    public RelatedTerm(String term, RelatedTermType type, double score) {
        super(term, score);
        this.type = type;
    }

    public static RelatedTerm of(String term, double score, RelatedTermType type) {
        return new RelatedTerm(term, type, score);
    }

    public static RelatedTerm convertScoredTerm(ScoredTerm term, RelatedTermType type){
        return RelatedTerm.of(term.getText(), term.getScore(), type);
    }

    public boolean equals(Object o){
        return super.equals(o);
    }

    /**
     * Created by chris on 12/30/15.
     */
    public enum RelatedTermType {
        Compound, Sentence, Synonym
    }

    @Override
    public String toString() {
        return super.toString() + ": " + type.toString();
    }
}
