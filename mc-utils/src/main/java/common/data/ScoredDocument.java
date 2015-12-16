package common.data;

/**
 * Created by chris on 12/15/15.
 */
public class ScoredDocument {
    public final int docId;
    public final double score;
    public ScoredDocument(int docId, double score){
        this.docId = docId;
        this.score = score;
    }

    public static ScoredDocument of(int docId, double score){
        return new ScoredDocument(docId, score);
    }
}
