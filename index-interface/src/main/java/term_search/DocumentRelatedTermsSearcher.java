package term_search;

import common.data.ScoredTerm;
import exception.SearchException;

import java.util.List;

/**
 * Created by chris on 1/8/16.
 */
public interface DocumentRelatedTermsSearcher {
    List<ScoredTerm> getDocumentRelatedTerms(int docId, String term) throws SearchException;
}
