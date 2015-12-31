package data_processing.related_terms_combiner.data;

import util.data.Node;

/**
 * Created by chris on 12/30/15.
 */
public class TermNode extends Node {
    public final RelatedTermType termType;
    public TermNode(String name, int id, String color, RelatedTermType termType) {
        super(false, name, id, color);
        this.termType = termType;
    }

    public static TermNode of(String name, int id, String color, RelatedTermType termType){
        return new TermNode(name, id, color, termType);
    }
}
