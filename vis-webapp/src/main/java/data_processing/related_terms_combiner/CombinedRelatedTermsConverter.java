package data_processing.related_terms_combiner;

import data_processing.related_terms_combiner.data.RelatedTerm;
import data_processing.related_terms_combiner.data.RelatedTermResult;
import data_processing.related_terms_combiner.data.SizedFixedNode;
import data_processing.related_terms_combiner.data.TermNode;
import org.apache.lucene.queryparser.classic.ParseException;
import reader.LuceneIndexReader;
import searcher.TermQueryScore;
import searcher.exception.LuceneSearchException;
import util.FixedNodeGenerator;
import util.TermStemmer;
import util.data.D3ConvertibleJson;
import util.data.FixedNode;
import util.data.Link;

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
            String sTerm = n.name;
            try{
                sTerm = TermStemmer.stemTerm(sTerm);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            double score = scorer.getScore(n.name, results[0].docId);
            maxScore = score > maxScore ? score : maxScore;
            scoreMap.put(n.name, score);
        }

        // Do the adjustments!
        for(FixedNode n : fixedNodes){
            double score = 1000 * scoreMap.get(n.name) / maxScore;
            double logScore = 10 * Math.log(1 + score);
            SizedFixedNode sn = SizedFixedNode.of(n, logScore);
            jsonObject.nodes.add(sn);
        }

        // Get the results
        for(RelatedTermResult result : results){
            // Add the related terms as nodes
            int sourceIndex = termIndexes.get(result.term);
            for(RelatedTerm rTerm : result.getResults()){
                int myIndex = jsonObject.nodes.size() - 1;
                int id = rTerm.getText().hashCode();
                String color = determineColor(rTerm);
                double linkPower = rTerm.getScore();
                double size = scorer.getScore(rTerm.getText(), result.docId);
                jsonObject.nodes.add(TermNode.of(TermNode.NOT_FIXED, rTerm.getText(), id, color, rTerm.type, size));
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
