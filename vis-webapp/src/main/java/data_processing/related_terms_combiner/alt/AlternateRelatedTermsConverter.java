package data_processing.related_terms_combiner.alt;

import api.exception.LuceneSearchException;
import api.reader.LuceneIndexReader;
import com.google.common.collect.ImmutableMap;
import data_processing.related_terms_combiner.alt.data.Node;
import data_processing.related_terms_combiner.alt.data.RootNode;
import data_processing.related_terms_combiner.data.RelatedTerm;
import data_processing.related_terms_combiner.data.RelatedTermResult;
import internal.term_utils.TermQueryScore;

import java.util.Map;

/**
 * Created by chris on 3/18/16.
 */
public class AlternateRelatedTermsConverter {
    private static TermQueryScore scorer;
    static{
        try {
            scorer =  new TermQueryScore(LuceneIndexReader.getInstance());
        } catch (LuceneSearchException e) {
            e.printStackTrace();
        }
    }

    private final static Map<RelatedTerm.RelatedTermType, String> COLORS =
            ImmutableMap.of(RelatedTerm.RelatedTermType.Compound, "blue",
                            RelatedTerm.RelatedTermType.Sentence, "red",
                            RelatedTerm.RelatedTermType.Synonym, "green");

    public static RootNode convertToD3(RelatedTermResult... results){
        RootNode root = new RootNode("flare", "black");
        for(RelatedTermResult result : results){
            Node childNode = new Node(result.term, 1, "lightblue");
            root.children.add(childNode);
            for(RelatedTerm term : result.getResults()){
                // TODO: add color changing and other stuff to this, later
                double size = scorer.getScore(term.getText(),
                                              result.docId,
                                              TermQueryScore.QueryType.Multiword);
                if(size < .0001)
                    continue;

                Node newNode = new Node(term.getText(), size, COLORS.get(term.type));
                childNode.children.add(newNode);
            }
        }
        return root;
    }


}
