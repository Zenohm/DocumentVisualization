package data_processing.multi_query_processing.util;

/**
 * Created by chris on 11/22/15.
 */
public class Coordinates {
    public final double x;
    public final double y;
    public Coordinates(double x, double y){
        this.x = x;
        this.y = y;
    }

    public static Coordinates of(double x, double y){
        return new Coordinates(x, y);
    }
}
