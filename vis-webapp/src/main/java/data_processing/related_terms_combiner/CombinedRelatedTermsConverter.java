package data_processing.related_terms_combiner;

import data_processing.related_terms_combiner.data.RelatedTerm;
import data_processing.related_terms_combiner.data.RelatedTermResult;
import data_processing.related_terms_combiner.data.SizedFixedNode;
import data_processing.related_terms_combiner.data.TermNode;
import api.reader.LuceneIndexReader;
import internal.term_utils.TermQueryScore;
import api.exception.LuceneSearchException;
import data_processing.FixedNodeGenerator;
import data_processing.data.D3ConvertibleJson;
import data_processing.data.FixedNode;
import data_processing.data.Link;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by chris on 12/30/15.
 */
public class CombinedRelatedTermsConverter {
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
            e.printStackTrace();
            System.err.println("Cannot initialize scorer");
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

        // Get the results
        int numNodes = 0;
        int removedNodes = 0;
        for(RelatedTermResult result : results){
            // Add the related terms as nodes
            int sourceIndex = termIndexes.get(result.term);
            for(RelatedTerm rTerm : result.getResults()){
                double size = scorer.getScore(rTerm.getText(), result.docId, TermQueryScore.QueryType.Basic);
                if(size < .05){
                    removedNodes++;
                    continue;
                }
                int myIndex = jsonObject.nodes.size() - 1;
                int id = rTerm.getText().hashCode();
                String color = fixedNodes.get(sourceIndex).color; // Get the color of the source
                double linkPower = rTerm.getScore();
                if(linkPower >= .01){
                    jsonObject.nodes.add(TermNode.of(TermNode.NOT_FIXED, rTerm.getText(), id, color, rTerm.type, size, result.term));
                    jsonObject.links.add(Link.of(sourceIndex, myIndex, linkPower));
                    numNodes++;
                }else{
                    removedNodes++;
                }
            }
        }
        System.out.println("Number of nodes: " + numNodes);
        System.out.println("Removed Nodes:" + removedNodes);
        return jsonObject;
    }

}
