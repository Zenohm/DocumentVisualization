package data_processing.multi_query_processing.multi_query_json_data;

/**
 * Representation of the Link for JSON object
 * Created by chris on 11/22/15.
 */
public class Link {
    public final int source;
    public final int target;
    public final double link_power;

    public Link(int source, int target, double link_power) {
        this.source = source;
        this.target = target;
        this.link_power = link_power;
    }

    public static Link of(int source, int target, double linkPower) {
        return new Link(source, target, linkPower);
    }
}
