package searcher.results;

/**
 * Query Results from a search
 * Created by chris on 11/19/15.
 */
public class QueryResults {
    public final int docId;
    public final String query;
    public final double score;

    /**
     * Constructor
     *
     * @param docId The document ID
     * @param query The String query
     * @param score The score that was obtained
     */
    public QueryResults(int docId, String query, double score) {
        this.docId = docId;
        this.query = query;
        this.score = score;
    }
}
