package data_processing.related_terms_combiner.alt;

import data_processing.related_terms_combiner.alt.data.Node;
import data_processing.related_terms_combiner.alt.data.RootNode;
import data_processing.related_terms_combiner.data.RelatedTerm;
import data_processing.related_terms_combiner.data.RelatedTermResult;

/**
 * Created by chris on 3/18/16.
 */
public class AlternateRelatedTermsConverter {

    public static RootNode convertToD3(RelatedTermResult... results){
        RootNode root = new RootNode("flare");
        for(RelatedTermResult result : results){
            Node childNode = new Node(result.term, 50);
            root.children.add(childNode);
            for(RelatedTerm term : result.getResults()){
                // TODO: add color changing and other stuff to this, later
                Node newNode = new Node(term.getText(), term.getScore());
                childNode.children.add(newNode);
            }
        }
        return root;
    }


}
