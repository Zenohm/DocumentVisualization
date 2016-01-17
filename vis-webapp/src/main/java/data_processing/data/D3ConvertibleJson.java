package data_processing.data;

import servlets.servlet_util.JsonCreator;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON representation of the nodes and links in the D3 visualization.
 * Created by chris on 11/22/15.
 */
public class D3ConvertibleJson {
    public final List<Node> nodes;
    public final List<Link> links;

    public D3ConvertibleJson() {
        nodes = new ArrayList<>();
        links = new ArrayList<>();
    }

    public String toString(){
        return JsonCreator.toJson(this);
    }
}
