package data_processing.related_terms_combiner.alt.data;

/**
 * Created by chris on 3/18/16.
 */
public class Node extends RootNode {
    public final double size;

    public Node(String name, double size) {
        super(name);
        this.size = size;
    }

}
