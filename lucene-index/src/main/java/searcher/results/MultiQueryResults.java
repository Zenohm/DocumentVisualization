package searcher.results;

import org.apache.lucene.search.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chris on 11/19/15.
 */
public class MultiQueryResults {
    public final int docId;
    public final double score;
    public final List<String> terms;
    private final List<QueryResults> results;
    public MultiQueryResults(int docId, double score, List<String> terms){
        this.docId = docId;
        this.terms = new ArrayList<>(terms);
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
