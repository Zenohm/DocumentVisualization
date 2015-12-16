package searcher;

import common.Constants;
import common.data.ScoredDocument;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import reader.IndexReader;
import searcher.exception.LuceneSearchException;
import util.ListUtils;
import util.Searcher;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Searches the index for documents
 * Created by chris on 10/5/15.
 */
public class DocumentSearcher extends Searcher {
    public DocumentSearcher(IndexReader reader, Analyzer analyzer) throws LuceneSearchException {
        super(reader, analyzer);
    }

    /**
     * Returns a list of scored documents given a term
     * @param term Term to search for
     * @return List of scored documents for the search term
     * @throws LuceneSearchException
     */
    public List<ScoredDocument> searchForTerm(String term) throws LuceneSearchException {
        try {
            Query query = parser.parse(term);
            System.out.print("Searching for query: " + query.toString());
            // Searches and returns the max number of documents
            final TopDocs search = searcher.search(query, reader.getReader().numDocs());
            System.out.print(". Found " + search.totalHits + " documents matching your query.\n");

            // Prints the contents of the document
//            for(ScoreDoc doc : search.scoreDocs){
//                System.out.print("Doc ID: ");
//                for(String content : reader.getReader().document(doc.doc).getValues(Constants.FIELD_CONTENTS)){
//                    System.out.print(content + ", ");
//                }
//                System.out.println();
//            }

            return Arrays.asList(search.scoreDocs)
                    .stream()
                    .map(doc -> ScoredDocument.of(doc.doc, (double) doc.score))
                    .collect(Collectors.toList());
        } catch (ParseException e) {
            throw new LuceneSearchException("DocumentSearcher: Parse exception while searching for term: "
                    + e.toString());
        } catch (IOException e) {
            throw new LuceneSearchException("DocumentSearcher: IO Exception " + e.toString());
        }
    }

    /**
     * Searches for a set of terms, only returns the first N
     * @param term The term to search for documents that contain it
     * @param limit Limit for the number of documents
     * @return A list of scored documents of size limit or smaller.
     * @throws LuceneSearchException
     */
    public List<ScoredDocument> searchForTerm(String term, int limit) throws LuceneSearchException{
        return ListUtils.getSublist(searchForTerm(term), limit);
    }


}
