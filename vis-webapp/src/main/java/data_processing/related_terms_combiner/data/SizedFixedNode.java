package data_processing.related_terms_combiner.data;

import util.data.FixedNode;

/**
 * Created by chris on 1/6/16.
 */
public class SizedFixedNode extends FixedNode{
    public final double size;
    public SizedFixedNode(FixedNode node, double size){
        super(node.name, node.id, node.color, node.xLoc, node.yLoc, node.text_content);
        this.size = size;
    }

    public static SizedFixedNode of(FixedNode node, double size){
        return new SizedFixedNode(node, size);
    }
}
