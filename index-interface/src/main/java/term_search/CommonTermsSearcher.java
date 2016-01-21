package term_search;

import common.data.ScoredTerm;
import exception.SearchException;

import java.util.List;

/**
 * Created by chris on 1/21/16.
 */
public interface CommonTermsSearcher{
    List<ScoredTerm> getCommonTerms(int docId) throws SearchException;
}
