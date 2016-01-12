package document_search;

import common.data.ScoredDocument;
import exception.SearchException;

import java.util.List;

/**
 * Created by chris on 1/8/16.
 */
public interface DocumentSearcher {
    List<ScoredDocument> searchForTerm(String term) throws SearchException;
    List<ScoredDocument> searchForTerm(String term, int maxDocs) throws SearchException;
}
