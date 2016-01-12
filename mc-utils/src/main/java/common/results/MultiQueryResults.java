package common.results;

import java.util.ArrayList;
import java.util.List;

/**
 * The results of a MultiQuery search
 * Contains the results of searches for all the individual terms
 * Created by chris on 11/19/15.
 */
public class MultiQueryResults {
    public final int docId;
    public final double score;
    public final List<String> terms;
    private final List<QueryResults> results;

    /**
     * Build a Multiquery result
     *
     * @param docId Document ID
     * @param score Overall Score for the Document
     * @param terms A list of the terms that were searched for
     */
    public MultiQueryResults(int docId, double score, List<String> terms) {
        this.docId = docId;
        this.terms = new ArrayList<>(terms);
        this.score = score;
        results = new ArrayList<>();
    }

    /**
     * Adds a query result to the list
     *
     * @param res The result to add
     */
    public void addQueryResult(QueryResults res) {
        results.add(res);
    }

    /**
     * Returns an array reference to the query results
     *
     * @return The list of query results
     */
    public QueryResults[] getQueryResults() {
        QueryResults[] resultArray = new QueryResults[results.size()];
        return results.toArray(resultArray);
    }
}
