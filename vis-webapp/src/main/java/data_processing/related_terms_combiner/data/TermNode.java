package data_processing.related_terms_combiner.data;

import data_processing.data.Node;

/**
 * Created by chris on 12/30/15.
 */
public class TermNode extends Node {
    public static boolean FIXED = true;
    public static boolean NOT_FIXED = false;
    public final RelatedTerm.RelatedTermType termType;
    public final double size;
    public final String relatedTo;
    public TermNode(boolean fixed, String name, int id, String color, RelatedTerm.RelatedTermType termType, double size, String relatedTo) {
        super(fixed, name, id, color);
        this.termType = termType;
        this.size = size;
        this.relatedTo = relatedTo;
    }

    public static TermNode of(boolean fixed, String name, int id, String color, RelatedTerm.RelatedTermType termType, double size, String relatedTo){
        return new TermNode(fixed, name, id, color, termType, size, relatedTo);
    }
}
