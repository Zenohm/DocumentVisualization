package searcher;

import analyzers.search.SearchAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.Query;
import reader.IndexReader;
import searcher.exception.LuceneSearchException;
import util.Searcher;

import java.io.IOException;

/**
 * Created by chris on 1/6/16.
 */
public class TermQueryScore extends Searcher {
    public TermQueryScore(IndexReader reader) throws LuceneSearchException {
        super(reader, new SearchAnalyzer(WhitespaceTokenizer.class));
    }

    public double getScore(String term, int docId){
        Query myQuery;
        try {
            myQuery = parser.parse(term);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
        try {
            return searcher.explain(myQuery, docId).getValue();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
