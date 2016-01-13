package internal.lucene_intf;

import api.reader.IndexReader;
import api.exception.LuceneSearchException;

/**
 * Generic super class for ensuring that the api.reader is initialized in any class
 * Created by chris on 12/15/15.
 */
public abstract class LuceneReader {
    protected IndexReader reader;

    public LuceneReader(IndexReader reader) throws LuceneSearchException {
        if (!reader.isInitialized()) {
            throw new LuceneSearchException(getClass().getName() + ": IndexReader Not Initialized");
        }
        this.reader = reader;
    }
}
