package searcher;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import searcher.exception.LuceneSearchException;
import searcher.reader.IndexReader;
import searcher.results.MultiQueryResults;
import searcher.results.QueryResults;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chris on 11/19/15.
 */
public class MultiQuerySearcher extends Searcher {
    public MultiQuerySearcher(IndexReader reader) throws LuceneSearchException{
        super(reader);
    }

    public List<MultiQueryResults> searchForResults(String... queries) throws IOException {
        // Create a list of queries
        List<Query> queryList = Arrays.asList(queries).stream()
                .map(this::parseQuery)
                .filter(query -> query != null) // Handle potential nulls.
                .collect(Collectors.toList());

        // Create the boolean query to cover all the cases
        BooleanQuery overallQuery = new BooleanQuery();
        for(Query query : queryList) {
            overallQuery.add(query, BooleanClause.Occur.SHOULD); // Add that the query should occur
        }

        // Search the index for the documents.
        final TopDocs searchResults = searcher.search(overallQuery, 50); // FIXME: Magic number

        return Arrays.asList(searchResults.scoreDocs).stream()
                .map(doc -> {
                    MultiQueryResults queryResults =
                            new MultiQueryResults(doc.doc, overallQuery, (double) doc.score);

                    for (Query query : queryList) {
                        try {
                            double score = searcher.explain(query, doc.doc).getValue();
                            QueryResults results = new QueryResults(doc.doc, query, score);
                            queryResults.addQueryResult(results);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return queryResults;
                })
                .collect(Collectors.toList());
    }

    private Query parseQuery(String str){
        try{
            return parser.parse(str);
        }catch (ParseException e){
            e.printStackTrace(); // TODO: Introduce better error handling
        }
        return null;
    }

}
