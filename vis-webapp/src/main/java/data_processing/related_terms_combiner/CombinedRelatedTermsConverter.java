package data_processing.related_terms_combiner;

import api.exception.LuceneSearchException;
import api.reader.LuceneIndexReader;
import data_processing.FixedNodeGenerator;
import data_processing.data.D3ConvertibleJson;
import data_processing.data.FixedNode;
import data_processing.data.Link;
import data_processing.related_terms_combiner.data.RelatedTerm;
import data_processing.related_terms_combiner.data.RelatedTermResult;
import data_processing.related_terms_combiner.data.SizedFixedNode;
import data_processing.related_terms_combiner.data.TermNode;
import internal.term_utils.TermQueryScore;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chris on 12/30/15.
 */
public class CombinedRelatedTermsConverter {
    private static final Log log = LogFactory.getLog(CombinedRelatedTermsConverter.class);
    private static TermQueryScore scorer;
    static{
        try {
            scorer = new TermQueryScore(LuceneIndexReader.getInstance());
        } catch (LuceneSearchException e) {
            e.printStackTrace();
        }
    }
    public static D3ConvertibleJson convertToLinksAndNodes(RelatedTermResult... results){
        TermQueryScore scorer;
        try {
            scorer = new TermQueryScore(LuceneIndexReader.getInstance());
        } catch (LuceneSearchException e) {
            log.error("Cannot initialize scorer", e);
            return null; // Null because cannot score
        }
        D3ConvertibleJson jsonObject = new D3ConvertibleJson();
        String[] terms = new String[results.length];
        for(int i = 0; i<results.length; i++){
            terms[i] = results[i].term;
        }

        Map<String, Integer> termIndexes = new HashMap<>();
        ArrayList<FixedNode> fixedNodes = new ArrayList<>();
        FixedNodeGenerator.generateFixedNodes(termIndexes, fixedNodes, terms);

        // Adding the fixed nodes
        double maxScore = 0;
        Map<String, Double> scoreMap = new HashMap<>();
        for(FixedNode n : fixedNodes){
            double score = scorer.getScore(n.name, results[0].docId, TermQueryScore.QueryType.Basic);
            maxScore = score > maxScore ? score : maxScore;
            scoreMap.put(n.name, score);
        }

        // Do the adjustments!
        for(FixedNode n : fixedNodes){
            double score = 1000 * scoreMap.get(n.name);
            double logScore = Math.log(1 + score);
            SizedFixedNode sn = SizedFixedNode.of(n, logScore);
            jsonObject.nodes.add(sn);
        }

        FixedNode gravityNode = SizedFixedNode.of("", -999, "white", .5, .5, "");
        int gravityNodeIndex = jsonObject.nodes.size();
        jsonObject.nodes.add(gravityNode);

        // Get the results
        int numNodes = 0;
        int removedNodes = 0;
        for(RelatedTermResult result : results){
            // Add the related terms as nodes
            int sourceIndex = termIndexes.get(result.term);
            for(RelatedTerm rTerm : result.getResults()){
//                EasyLogger.log(result.term + "_comb_rel_terms", rTerm);
                double size = scorer.getScore(rTerm.getText(), result.docId, TermQueryScore.QueryType.Multiword);
                if(size < .0001){
                    removedNodes++;
                    continue;
                }
                int myIndex = jsonObject.nodes.size();
                int id = rTerm.getText().hashCode();
                String color = fixedNodes.get(sourceIndex).color; // Get the color of the source
                double linkPower = rTerm.getScore();
                if(linkPower >= .001){
                    TermNode newNode = TermNode.of(TermNode.NOT_FIXED, rTerm.getText(), id, color, rTerm.type, size, result.term);
                    int iOfNode = jsonObject.nodes.indexOf(newNode);
                    if(iOfNode != -1){
                        log.info(newNode.name + " is already in the visualization, skipping node creation.");
                        // If the node is already there, do not add a new node, supplement the old node
                        jsonObject.links.add(Link.of(sourceIndex, iOfNode, linkPower));
                        jsonObject.links.add(Link.of(gravityNodeIndex, iOfNode, .075));
                    }else{
                        // Add a new node if the node is not there
                        jsonObject.nodes.add(newNode);
                        jsonObject.links.add(Link.of(sourceIndex, myIndex, linkPower));
                        jsonObject.links.add(Link.of(gravityNodeIndex, myIndex, .075));
                    }

                    numNodes++;
                }else{
                    removedNodes++;
                }
            }
        }
        log.info("Number of nodes: " + numNodes);
        log.info("Removed Nodes: " + removedNodes);
        return jsonObject;
    }

}
