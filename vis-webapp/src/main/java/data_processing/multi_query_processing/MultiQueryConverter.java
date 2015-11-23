package data_processing.multi_query_processing;

import com.google.common.collect.ImmutableMap;
import data_processing.multi_query_processing.multi_query_json_data.DocumentNode;
import data_processing.multi_query_processing.multi_query_json_data.FixedNode;
import data_processing.multi_query_processing.multi_query_json_data.Link;
import data_processing.multi_query_processing.multi_query_json_data.MultiQueryJson;
import data_processing.multi_query_processing.util.Coordinates;
import searcher.results.MultiQueryResults;
import searcher.results.QueryResults;

import java.util.*;

/**
 * Created by chris on 11/22/15.
 */
public class MultiQueryConverter {
    public static final int SIZE_NORMALIZER = 100;
    public static final int MIN_SIZE = 5;

    public static final Map<Integer, Integer> indexColors = ImmutableMap.of(0, 0xFF0000,
                                                                            1, 0x00FF00,
                                                                            2, 0x0000FF);


    public static final Map<Integer, Coordinates> indexLocs = ImmutableMap.of(0, Coordinates.of(1.0 / 2.0, 1.0 / 5.0),
                                                                         1, Coordinates.of(1.0 / 5.0, 4.0 / 5.0),
                                                                         3, Coordinates.of(4.0 / 5.0, 4.0 / 5.0));

    public static MultiQueryJson convertToLinksAndNodes(List<MultiQueryResults> results){
        Map<String, Integer> fixedTermIndexes = new HashMap<>();
        MultiQueryJson jsonObject = new MultiQueryJson();
        // Iterate over the set of results (we need the indexes of all search terms)
        int index = 0;
        for(MultiQueryResults result : results){
            for(String term : result.terms){
                if(!fixedTermIndexes.containsKey(term)) {
                    fixedTermIndexes.put(term, index);

                    // Need to add the FixedNode
                    int termId = -1 * index - 1;
                    int color = indexColors.get(index);
                    Coordinates coords = indexLocs.get(index);
                    jsonObject.nodes.add(new FixedNode(term, termId, color, coords.x, coords.y, term.split(" ")));

                    // Add one to the index and do a bounds check, I don't want to do color math.
                    index++;
                    if (index == 3) break;
                }
            }
            // We don't support more than 3 terms (we could, but I don't want to do the color math at the moment.
            if(index == 3) break;
        }

        for(MultiQueryResults result : results){
            String documentName = "doc"+result.docId;
            int nodeSize = (int)Math.floor(result.score * SIZE_NORMALIZER);
            if(nodeSize < MIN_SIZE) nodeSize = MIN_SIZE;
            int nodeColor = determineColor(result);
            jsonObject.nodes.add(new DocumentNode(documentName, result.docId, nodeColor,
                    result.docId, nodeSize, result.score));

            int myIndex = jsonObject.nodes.size() - 1;
            for(QueryResults qResult : result.getQueryResults()){
                int sourceIndex = fixedTermIndexes.get(qResult.query);
                double linkPower = qResult.score;
                jsonObject.links.add(Link.of(sourceIndex, myIndex, linkPower));
            }

        }

        return jsonObject;
    }

    // TODO: This needs to implement something based on the indexes we assign to the RGB assets
    public static int determineColor(MultiQueryResults result){
        return 0xFFFFFF;
    }
}
