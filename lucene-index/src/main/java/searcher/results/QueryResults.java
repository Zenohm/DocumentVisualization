package searcher.results;

import org.apache.lucene.search.Query;

/**
 * Created by chris on 11/19/15.
 */
public class QueryResults {
    public final int docId;
    public final Query query;
    public final double score;
    public QueryResults(int docId, Query query, double score){
        this.docId = docId;
        this.query = query;
        this.score = score;
    }
}
