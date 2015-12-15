package access_utils;

import reader.IndexReader;
import searcher.exception.LuceneSearchException;

/**
 * Created by chris on 12/15/15.
 */
public class LuceneReader {
    protected IndexReader reader;
    public LuceneReader(IndexReader reader) throws LuceneSearchException {
        if(!reader.isInitialized()){
            throw new LuceneSearchException(getClass().getName() + ": IndexReader Not Initialized");
        }
        this.reader = reader;
    }
}
