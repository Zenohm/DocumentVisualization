package data_processing.multi_query_processing;

import data_processing.multi_query_processing.multi_query_json_data.DocumentNode;
import data_processing.multi_query_processing.multi_query_json_data.FixedNode;
import data_processing.multi_query_processing.multi_query_json_data.Link;
import data_processing.multi_query_processing.multi_query_json_data.MultiQueryJson;
import searcher.results.MultiQueryResults;
import searcher.results.QueryResults;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts multi-query results to a JSON object
 * Created by chris on 11/22/15.
 */
public class MultiQueryConverter {
    public static final int SIZE_NORMALIZER = 100;
    public static final int MIN_SIZE = 5;

    public static final String[] colors = {"red", "blue", "green"};

    /**
     * Convert a list of MultiQueryResults to a MultiQueryJson object
     * @param results The list of results to convert into the view
     * @return Object that represents the JSON object that can be read by D3.
     */
    public static MultiQueryJson convertToLinksAndNodes(List<MultiQueryResults> results){
        // Generate the JSON Object
        MultiQueryJson jsonObject = new MultiQueryJson();

        // Get a list of the unique terms within the results set.
        ArrayList<String> terms = new ArrayList<>();
        results.stream().forEach(result -> result.terms.stream()
                                                       .filter(term -> !terms.contains(term))
                                                       .forEach(terms::add));

        int numFixedNodes = terms.size();
        double angleBetweenNodes = (2*Math.PI) / numFixedNodes;
        double circleCenter = .5;
        double diameter = .7;
        double radius = diameter / 2.0;

        Map<String, Integer> termIndexes = new HashMap<>();
        ArrayList<FixedNode> fixedNodes = new ArrayList<>();
        // Set positioning information for the fixed nodes
        for(int i = 0; i < terms.size(); i++){
            String currentTerm = terms.get(i); // The current term

            // Figure out the node positioning
            double currentAngle = i * angleBetweenNodes; // first node will be at 0 degrees
            double x = radius*Math.sin(currentAngle) + circleCenter;
            double y = -1*radius*Math.cos(currentAngle) + circleCenter;

            // Do node coloring
            String color = colors[i % colors.length];
            if(i == terms.size() - 1 && color.equals(fixedNodes.get(0).color)){
                color = colors[(i + 1) % colors.length];
            }

            // Give the term a unique id
            int termId = (-1 * i) - 1;

            termIndexes.put(currentTerm, i);
            fixedNodes.add(new FixedNode(currentTerm, termId, color, x, y, currentTerm.split(" ")));
        }

        fixedNodes.forEach(jsonObject.nodes::add);

        for(MultiQueryResults result : results){
            String documentName = "doc"+result.docId;
            int nodeSize = (int)Math.floor(result.score * SIZE_NORMALIZER);
            if(nodeSize < MIN_SIZE) nodeSize = MIN_SIZE;
            String nodeColor = determineColor(result);
            jsonObject.nodes.add(DocumentNode.of(documentName, result.docId, nodeColor,
                    result.docId, nodeSize, result.score));

            int myIndex = jsonObject.nodes.size() - 1;
            for(QueryResults qResult : result.getQueryResults()){
                int sourceIndex = termIndexes.get(qResult.query);
                double linkPower = qResult.score;
                if(linkPower >= .001){
                    jsonObject.links.add(Link.of(sourceIndex, myIndex, linkPower));
                }
            }
        }

        return jsonObject;
    }

    // TODO: This needs to implement something based on the indexes we assign to the RGB assets

    /**
     * Determines the colors of the nodes
     * @param result Result to convert to a color
     * @return The color of the result
     */
    public static String determineColor(MultiQueryResults result){
        return "black";
    }
}
