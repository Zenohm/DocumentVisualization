package common.data;

/**
 * Scored document
 * Created by chris on 12/15/15.
 */
public class ScoredDocument {
    public final int docId;
    public final double score;
    public ScoredDocument(int docId, double score){
        this.docId = docId;
        this.score = score;
    }

    /**
     * Creates a scored document with ID and score
     * @param docId The document ID to be scored
     * @param score The score for the document
     * @return A scored document object
     */
    public static ScoredDocument of(int docId, double score){
        return new ScoredDocument(docId, score);
    }

    /**
     * Compare to method. Compares a scored term to another score term
     * @param o The other scored term.
     * @return -1 if this term is less than the other term
     * 1 if it is greater than the other term
     * 0 if they are equal
     */
    public int compareTo(ScoredDocument o) {
        if (this.score == o.score) return 0;
        if (this.score > o.score) {
            return 1;
        }
        return -1;
    }
}
