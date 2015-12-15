package searcher;

import com.google.common.collect.Maps;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import searcher.exception.LuceneSearchException;
import reader.IndexReader;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by chris on 10/5/15.
 */
public class DocumentSearcher extends Searcher {
    public DocumentSearcher(IndexReader reader) throws LuceneSearchException {
        super(reader);
    }

    public List<Map.Entry<Double, Integer>> searchForTerm(String term) throws LuceneSearchException {
        try {
            Query query = parser.parse(term);
            System.out.print("Searching for query: " + query.toString());
            final TopDocs search = searcher.search(query, 50);
            System.out.print(". Found " + search.totalHits + " documents matching your query.\n");

            return Arrays.asList(search.scoreDocs)
                    .stream()
                    .map(doc -> Maps.immutableEntry((double) doc.score, doc.doc))
                    .collect(Collectors.toList());
        } catch (ParseException e) {
            throw new LuceneSearchException("DocumentSearcher: Parse exception while searching for term: "
                    + e.toString());
        } catch (IOException e) {
            throw new LuceneSearchException("DocumentSearcher: IO Exception " + e.toString());
        }
    }


}
