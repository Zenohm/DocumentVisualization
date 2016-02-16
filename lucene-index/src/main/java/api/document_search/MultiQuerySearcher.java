package api.document_search;

import api.exception.LuceneSearchException;
import api.reader.IndexReader;
import com.google.common.collect.Maps;
import common.results.MultiQueryResults;
import common.results.QueryResults;
import document_search.MultiQuerySearch;
import internal.analyzers.search.SearchAnalyzer;
import internal.lucene_intf.Searcher;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by chris on 11/19/15.
 */
public class MultiQuerySearcher extends Searcher implements MultiQuerySearch {

    /**
     * The arbitrary, magical number to default to when no specific number of queries has been given.
     */
    private static final int DEFAULT_DOC_LIMIT = 50;

    public MultiQuerySearcher(IndexReader reader) throws LuceneSearchException {
        super(reader, new SearchAnalyzer(WhitespaceTokenizer.class));
    }

    /**
     * Searches for multiple queries using the default document limit.
     *
     * @param queries The query terms to search for
     * @return A list of results for the multiple queries
     * @throws IOException
     */
    public List<MultiQueryResults> searchForResults(String... queries) throws IOException {
        return searchForResults(DEFAULT_DOC_LIMIT, queries);
    }

    /**
     * Searches for multiple queries
     *
     * @param queries The query terms to search for
     * @return A list of results for the multiple queries
     * @throws IOException
     */
    public List<MultiQueryResults> searchForResults(int docLimit, String... queries) throws IOException {
        // Create a list of queries
        ArrayList<Map.Entry<String, Query>> queryList = (ArrayList<Map.Entry<String, Query>>) Arrays
                .asList(queries).stream() // This cannot be parallel
                .map(this::parseQuery)
                .filter(query -> query != null) // Handle potential nulls.
                .collect(Collectors.toList());

        // Create the boolean query to cover all the cases
        BooleanQuery overallQuery = new BooleanQuery();

        for (Map.Entry<String, Query> query : queryList) {
            overallQuery.add(query.getValue(), BooleanClause.Occur.SHOULD); // Add that the query should occur
        }

        // Search the index for the documents.
        final TopDocs searchResults = searcher.search(overallQuery, docLimit);

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
