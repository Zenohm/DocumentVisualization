package data_processing.related_terms_combiner.alt.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 3/18/16.
 */
public class RootNode {
    public final String name;
    public final List<Node> children;

    public RootNode(String name) {
        this.name = name;
        children = new ArrayList<>();
    }

}
