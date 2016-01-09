package util;

import util.data.FixedNode;

import java.util.ArrayList;
import java.util.Map;

/**
 * Generates fixed nodes for the D3 visualizations. (Lots of clever maths)
 * Created by chris on 12/30/15.
 */
public class FixedNodeGenerator {

    static double circleCenter = .5;
    static double diameter = .7;
    static double radius = diameter / 2.0;

    public static final String[] colors = {"red", "blue", "green"};

    public static void generateFixedNodes(Map<String, Integer> termIndexes,
                                          ArrayList<FixedNode> fixedNodes,
                                          String[] terms){

        double angleBetweenNodes = (2 * Math.PI) / terms.length;

        // Set positioning information for the fixed nodes
        for (int i = 0; i < terms.length; i++) {
            String currentTerm = terms[i]; // The current term

            // Figure out the node positioning
            double currentAngle = i * angleBetweenNodes; // first node will be at 0 degrees
            double x = radius * Math.sin(currentAngle) + circleCenter;
            double y = -1 * radius * Math.cos(currentAngle) + circleCenter;

            // Do node coloring
            String color = colors[i % colors.length];
            if (i == terms.length - 1 && color.equals(fixedNodes.get(0).color)) {
                color = colors[(i + 1) % colors.length];
            }

            // Give the term a unique id
            int termId = (-1 * i) - 1;


            termIndexes.put(currentTerm, i);
            fixedNodes.add(FixedNode.of(currentTerm, termId, color, x, y, currentTerm));
        }
    }
}
