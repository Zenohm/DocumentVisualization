package server_utils.data;

/**
 * Node that does not move, has a set position relative to the total width.
 * Created by chris on 11/22/15.
 */
public class FixedNode extends Node {
    public final double xLoc;
    public final double yLoc;
    public final String text_content;

    public FixedNode(String name, int id, String color, double xLoc, double yLoc, String text_content) {
        super(true, name, id, color);
        this.xLoc = xLoc;
        this.yLoc = yLoc;
        this.text_content = text_content;
    }

    public static FixedNode of(String name, int id, String color, double xLoc, double yLoc, String text_content) {
        return new FixedNode(name, id, color, xLoc, yLoc, text_content);
    }
}
