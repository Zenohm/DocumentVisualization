package searcher;

import access_utils.LuceneReader;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import searcher.exception.LuceneSearchException;
import reader.IndexReader;
import common.Constants;

/**
 * Created by chris on 11/19/15.
 */
public abstract class Searcher extends LuceneReader {
    protected IndexSearcher searcher;
    protected QueryParser parser;
    protected Analyzer analyzer;

    public Searcher(IndexReader reader, Analyzer analyzer) throws LuceneSearchException {
        super(reader);
        this.searcher = new IndexSearcher(reader.getReader());
        this.analyzer = analyzer;
        this.parser = new QueryParser(Constants.FIELD_CONTENTS, analyzer);
    }
}
