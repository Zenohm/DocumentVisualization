package data_processing.related_terms_combiner.data;

import util.data.Node;

/**
 * Created by chris on 12/30/15.
 */
public class TermNode extends Node {
    public static boolean FIXED = true;
    public static boolean NOT_FIXED = false;
    public final RelatedTermType termType;
    public final double size;
    public TermNode(boolean fixed, String name, int id, String color, RelatedTermType termType, double size) {
        super(fixed, name, id, color);
        this.termType = termType;
        this.size = size;
    }

    public static TermNode of(boolean fixed, String name, int id, String color, RelatedTermType termType, double size){
        return new TermNode(fixed, name, id, color, termType, size);
    }
}
