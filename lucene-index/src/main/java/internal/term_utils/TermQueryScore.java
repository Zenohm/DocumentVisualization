package internal.term_utils;

import api.exception.LuceneSearchException;
import api.reader.IndexReader;
import internal.analyzers.search.SearchAnalyzer;
import internal.lucene_intf.Searcher;
import internal.static_util.QueryUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;

import java.io.IOException;

/**
 * Created by chris on 1/6/16.
 */
public class TermQueryScore extends Searcher {
    private static final Log log = LogFactory.getLog(TermQueryScore.class);
    public TermQueryScore(IndexReader reader) throws LuceneSearchException {
        super(reader, new SearchAnalyzer(WhitespaceTokenizer.class));
    }

    /**
     * Gets the score for a term if all the words inside a term must be present.
     * @param term Term to look for (might have spaces)
     * @param docId The document id to search
     * @return The score for the search
     */
    public double getMultiwordScore(String term, int docId){
        Query myQuery = QueryUtils.mustContainWords(term);
        try {
            return searcher.explain(myQuery, docId).getValue();
        } catch (IOException e) {
            log.error("Could not get score for " + myQuery.toString() + " for document " + docId);
            return 0;
        }
    }

    public double getBasicScore(String term, int docId){
        Query myQuery;
        try {
            myQuery = parser.parse(term);
        } catch (ParseException e) {
            log.error("Query parser could not parse " + term);
            return 0;
        }
        try {
            return searcher.explain(myQuery, docId).getValue();
        } catch (IOException e) {
            log.error("Could not explain " + myQuery.toString() + " for document " + docId);
            return 0;
        }
    }

    public double getScore(String term, int docId, QueryType type){
        switch(type){
            case Basic:
                return getBasicScore(term, docId);
            case Multiword:
                return getMultiwordScore(term, docId);
            default:
                return getBasicScore(term, docId);
        }
    }


    public enum QueryType{
        Basic, Multiword
    }
}
