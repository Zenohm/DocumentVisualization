package data_processing.data;

import java.util.HashSet;
import java.util.Set;

/**
 * Generic Node, represents data
 * Created by chris on 11/22/15.
 */
public abstract class Node {
    public final boolean fixed;
    public final String name;
    public final int id;
    public final String color;
    public final Set<String> colors; // For multicolor support

    public Node(boolean fixed, String name, int id, String color) {
        this.fixed = fixed;
        this.name = name;
        this.id = id;
        this.color = color;
        this.colors = new HashSet<>();
        this.colors.add(color);
    }

    public void addColor(String c){
        this.colors.add(c);
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Node){
            Node n = (Node)o;
            return n.name.equals(this.name);
        }
        return false;
    }
}
