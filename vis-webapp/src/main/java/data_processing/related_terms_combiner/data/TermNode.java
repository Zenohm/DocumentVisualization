package data_processing.related_terms_combiner.data;

import data_processing.data.Node;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by chris on 12/30/15.
 */
public class TermNode extends Node {
    public static boolean FIXED = true;
    public static boolean NOT_FIXED = false;
    public final RelatedTerm.RelatedTermType termType;
    public final double size;
    public final Set<String> termsRelatedTo;
    public TermNode(boolean fixed, String name, int id, String color, RelatedTerm.RelatedTermType termType, double size, String relatedTo) {
        super(fixed, name, id, color);
        this.termType = termType;
        this.size = size;
        this.termsRelatedTo = new HashSet<>();
        this.termsRelatedTo.add(relatedTo);
    }

    public static TermNode of(boolean fixed, String name, int id, String color, RelatedTerm.RelatedTermType termType, double size, String relatedTo){
        return new TermNode(fixed, name, id, color, termType, size, relatedTo);
    }

    public void addRelatedTerm(String relatedTo){
        termsRelatedTo.add(relatedTo);
    }

    @Override
    public boolean equals(Object o){
        return super.equals(o);
    }
}
