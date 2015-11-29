package data_processing.multi_query_processing.multi_query_json_data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 11/22/15.
 */
public class MultiQueryJson {
    public final List<GenericNode> nodes;
    public final List<Link> links;
    public MultiQueryJson(){
        nodes = new ArrayList<>();
        links = new ArrayList<>();
    }
}
