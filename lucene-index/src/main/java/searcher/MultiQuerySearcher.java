package searcher;

import analyzers.search.SearchAnalyzer;
import com.google.common.collect.Maps;
import document_search.MultiQuerySearch;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import reader.IndexReader;
import searcher.exception.LuceneSearchException;
import common.results.MultiQueryResults;
import common.results.QueryResults;
import util.Searcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by chris on 11/19/15.
 */
public class MultiQuerySearcher extends Searcher implements MultiQuerySearch {
    public MultiQuerySearcher(IndexReader reader) throws LuceneSearchException {
        super(reader, new SearchAnalyzer(WhitespaceTokenizer.class));
    }

    /**
     * Searches for multiple queries
     *
     * @param queries The query terms to search for
     * @return A list of results for the multiple queries
     * @throws IOException
     */
    public List<MultiQueryResults> searchForResults(String... queries) throws IOException {
        // Create a list of queries
        List<Map.Entry<String, Query>> queryList = Arrays.asList(queries).stream() // This cannot be parallel
                .map(this::parseQuery)
                .filter(query -> query != null) // Handle potential nulls.
                .collect(Collectors.toList());

        // Create the boolean query to cover all the cases
        BooleanQuery overallQuery = new BooleanQuery();

        for (Map.Entry<String, Query> query : queryList) {
            overallQuery.add(query.getValue(), BooleanClause.Occur.SHOULD); // Add that the query should occur
        }

        // Search the index for the documents.
        final TopDocs searchResults = searcher.search(overallQuery, 50); // FIXME: Magic number

        return Arrays.asList(searchResults.scoreDocs).stream()
                .map(doc -> {
                    MultiQueryResults queryResults =
                            new MultiQueryResults(doc.doc, (double) doc.score, Arrays.asList(queries));

                    for (Map.Entry<String, Query> query : queryList) {
                        try {
                            double score =
                                    searcher.explain(query.getValue(), doc.doc).getValue();
                            QueryResults results = new QueryResults(doc.doc, query.getKey(), score);
                            queryResults.addQueryResult(results);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return queryResults;
                })
                .collect(Collectors.toList());
    }

    /**
     * Parses the query and relates it to a string
     *
     * @param str String to parse the query for
     * @return A Pair with the String and the query
     */
    private Map.Entry<String, Query> parseQuery(String str) {
        try {
            return Maps.immutableEntry(str, parser.parse(str));
        } catch (ParseException e) {
            e.printStackTrace(); // TODO: Introduce better error handling
        }
        return null;
    }

}
