package term_search;

import common.data.ScoredTerm;
import exception.SearchException;

import java.util.List;

/**
 * Created by chris on 1/8/16.
 */
public interface RelatedTermsSearcher {
    List<ScoredTerm> getRelatedTerms(String term) throws SearchException;
}
