package data_processing.related_terms_combiner;

import data_processing.related_terms_combiner.data.RelatedTerm;
import data_processing.related_terms_combiner.data.RelatedTermResult;
import data_processing.related_terms_combiner.data.TermNode;
import util.FixedNodeGenerator;
import util.data.D3ConvertibleJson;
import util.data.FixedNode;
import util.data.Link;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by chris on 12/30/15.
 */
public class CombinedRelatedTermsConverter {
    public static D3ConvertibleJson convertToLinksAndNodes(RelatedTermResult... results){
        D3ConvertibleJson jsonObject = new D3ConvertibleJson();

        String[] terms = new String[results.length];
        for(int i = 0; i<results.length; i++){
            terms[i] = results[i].term;
        }

        Map<String, Integer> termIndexes = new HashMap<>();
        ArrayList<FixedNode> fixedNodes = new ArrayList<>();
        FixedNodeGenerator.generateFixedNodes(termIndexes, fixedNodes, terms);

        // Adding the fixed nodes
        fixedNodes.forEach(jsonObject.nodes::add);

        // Get the results
        for(RelatedTermResult result : results){

            // Add the related terms as nodes
            int sourceIndex = termIndexes.get(result.term);
            for(RelatedTerm rTerm : result.getResults()){
                int myIndex = jsonObject.nodes.size() - 1;
                int id = rTerm.getText().hashCode();
                String color = determineColor(rTerm);
                double linkPower = rTerm.getScore();
                jsonObject.nodes.add(TermNode.of(rTerm.getText(), id, color, rTerm.type));
                if(linkPower >= .001){
                    jsonObject.links.add(Link.of(sourceIndex, myIndex, linkPower));
                }
            }
        }
        return jsonObject;
    }

    private static String determineColor(RelatedTerm rTerm){
        return "orange";
    }

}
