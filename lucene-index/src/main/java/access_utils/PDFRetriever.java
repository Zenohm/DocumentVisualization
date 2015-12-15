package access_utils;

import searcher.Searcher;
import searcher.exception.LuceneSearchException;
import reader.IndexReader;
import common.Constants;

import java.io.File;
import java.io.IOException;

/**
 * Created by Chris on 10/8/2015.
 */
public class PDFRetriever extends LuceneReader {
    public PDFRetriever(IndexReader reader) throws LuceneSearchException {
        super(reader);
    }

    public File getPDFFile(int docId) throws LuceneSearchException {
        try {
            String path = reader.getReader().document(docId).get(Constants.FIELD_PATH);
            return new File(path);
        } catch (IOException e) {
            throw new LuceneSearchException("PDFRetriever: IO Exception while accessing index");
        }
    }
}
