package searcher;

import com.google.common.collect.Maps;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import searcher.analyzer.SearchAnalyzer;
import searcher.exception.LuceneSearchException;
import searcher.reader.IndexReader;
import util.IndexerConstants;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by chris on 10/5/15.
 */
public class DocumentSearcher {
    private IndexReader reader;
    private IndexSearcher searcher;
    private QueryParser parser;
    private Analyzer analyzer;
    public DocumentSearcher(IndexReader reader) throws LuceneSearchException {
        if(!reader.isInitialized()) throw new LuceneSearchException("DocumentSearcher: IndexReader Not Initialized");
        this.reader = reader;
        this.searcher = new IndexSearcher(reader.getReader());
        this.analyzer = new SearchAnalyzer();
        this.parser = new QueryParser(IndexerConstants.FIELD_CONTENTS, analyzer);
    }

    public List<Map.Entry<Double, Integer>> searchForTerm(String term) throws LuceneSearchException{
        try {
            Query query = parser.parse(term);
            System.out.println("Searching for query: " + query.toString());
            final TopDocs search = searcher.search(query, 50);

            return Arrays.asList(search.scoreDocs)
                    .stream()
                    .map(doc -> Maps.immutableEntry((double)doc.score, doc.doc))
                    .collect(Collectors.toList());

        } catch (ParseException e) {
            throw new LuceneSearchException("DocumentSearcher: Parse exception while searching for term: "
                    + e.toString());
        } catch (IOException e) {
            throw new LuceneSearchException("DocumentSearcher: IO Exception " + e.toString());
        }
    }
}
