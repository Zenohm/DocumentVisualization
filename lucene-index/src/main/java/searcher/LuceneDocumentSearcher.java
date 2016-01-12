package searcher;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import common.Constants;
import common.data.ScoredDocument;
import document_search.DocumentSearcher;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import reader.IndexReader;
import searcher.exception.LuceneSearchException;
import util.ListUtils;
import util.Searcher;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Searches the index for documents
 * Created by chris on 10/5/15.
 */
public class LuceneDocumentSearcher extends Searcher implements DocumentSearcher {
    private static Cache<String, TopDocs> cache = CacheBuilder.newBuilder().maximumSize(Constants.MAX_CACHE_SIZE).build();

    public LuceneDocumentSearcher(IndexReader reader, Analyzer analyzer) throws LuceneSearchException {
        super(reader, analyzer);
    }

    /**
     * Returns a list of scored documents given a term
     *
     * @param term Term to search for
     * @return List of scored documents for the search term
     * @throws LuceneSearchException
     */
    public List<ScoredDocument> searchForTerm(String term) throws LuceneSearchException {
        // Searches and returns the max number of documents
        TopDocs search;
        try {
            search = cache.get(term, () -> {
                Query query = parser.parse(term);
                return searcher.search(query, reader.getReader().numDocs());
            });
        } catch (ExecutionException e) {
            System.err.println("There was an error creating the cache.");
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(search.scoreDocs)
                .stream()
                .map(doc -> ScoredDocument.of(doc.doc, (double) doc.score))
                .collect(Collectors.toList());
    }

    /**
     * Searches for a set of terms, only returns the first N
     *
     * @param term  The term to search for documents that contain it
     * @param limit Limit for the number of documents
     * @return A list of scored documents of size limit or smaller.
     * @throws LuceneSearchException
     */
    public List<ScoredDocument> searchForTerm(String term, int limit) throws LuceneSearchException {
        return ListUtils.getSublist(searchForTerm(term), limit);
    }


}
