package api.document_search;

import api.exception.LuceneSearchException;
import api.reader.IndexReader;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import common.Constants;
import common.data.ScoredDocument;
import document_search.DocumentSearcher;
import internal.lucene_intf.Searcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.TopDocs;
import utilities.ListUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Searches the index for documents
 * Created by chris on 10/5/15.
 */
public class LuceneDocumentSearcher extends Searcher implements DocumentSearcher {
    private static final Log log = LogFactory.getLog(LuceneDocumentSearcher.class);
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
            search = cache.get(term, () -> searcher.search(parser.parse(term), reader.getReader().numDocs()));
        } catch (ExecutionException e) {
            log.error("Error getting value from cache." + e.getMessage());
            return Collections.EMPTY_LIST;
        }

        return Arrays.asList(search.scoreDocs).stream()
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
