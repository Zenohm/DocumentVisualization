package data_processing.multi_query_processing.multi_query_json_data;

import java.util.Arrays;
import java.util.List;

/**
 * Created by chris on 11/22/15.
 */
public class FixedNode extends GenericNode {
    public final double x;
    public final double y;
    public final List<String> text_content;
    public FixedNode(String name, int id, int color, double x, double y, String... text_content){
        super(true,  name, id, color);
        this.x = x;
        this.y = y;
        this.text_content = Arrays.asList(text_content);
    }
}
