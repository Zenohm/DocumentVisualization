package searcher;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import searcher.analyzer.SearchAnalyzer;
import searcher.exception.LuceneSearchException;
import searcher.reader.IndexReader;
import util.IndexerConstants;

/**
 * Created by chris on 11/19/15.
 */
public abstract class Searcher {
    protected IndexReader reader;
    protected IndexSearcher searcher;
    protected QueryParser parser;
    protected Analyzer analyzer;
    public Searcher(IndexReader reader) throws LuceneSearchException {
        if(!reader.isInitialized()) throw new LuceneSearchException(getClass().getName() + ": IndexReader Not Initialized");
        this.reader = reader;
        this.searcher = new IndexSearcher(reader.getReader());
        this.analyzer = new SearchAnalyzer();
        this.parser = new QueryParser(IndexerConstants.FIELD_CONTENTS, analyzer);
    }
}
