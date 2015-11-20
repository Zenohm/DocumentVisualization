package searcher.results;

import org.apache.lucene.search.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 11/19/15.
 */
public class MultiQueryResults {
    private final List<QueryResults> results;
    public final int docId;
    public final Query query;
    public final double score;
    public MultiQueryResults(int docId, Query query, double score){
        this.docId = docId;
        this.query = query;
        this.score = score;
        results = new ArrayList<>();
    }

    public void addQueryResult(QueryResults res){
        results.add(res);
    }

    public QueryResults[] getQueryResults() {
        QueryResults[] resultArray = new QueryResults[results.size()];
        return results.toArray(resultArray);
    }
}
