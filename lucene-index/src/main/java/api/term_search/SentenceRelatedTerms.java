package api.term_search;

import common.data.ScoredTerm;
import exception.SearchException;
import internal.static_util.TermsAnalyzer;
import api.reader.LuceneIndexReader;
import term_search.DocumentRelatedTermsSearcher;

import java.util.List;

/**
 * Created by chris on 1/8/16.
 */
public class SentenceRelatedTerms implements DocumentRelatedTermsSearcher{
    @Override
    public List<ScoredTerm> getDocumentRelatedTerms(int docId, String term) throws SearchException{
        return TermsAnalyzer.getRelatedTermsInDocument(LuceneIndexReader.getInstance().getReader(), docId, term);
    }
}
