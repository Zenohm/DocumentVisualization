package access_utils;

import common.Constants;
import reader.IndexReader;
import searcher.exception.LuceneSearchException;
import util.LuceneReader;

import java.io.File;
import java.io.IOException;

/**
 * Gets a PDF file
 * Created by Chris on 10/8/2015.
 */
public class PDFRetriever extends LuceneReader {
    public PDFRetriever(IndexReader reader) throws LuceneSearchException {
        super(reader);
    }

    /**
     * Returns a file reference to a PDF document
     *
     * @param docId The document id to get the PDF for
     * @return The PDF file
     * @throws LuceneSearchException
     */
    public File getPDFFile(int docId) throws LuceneSearchException {
        try {
            String path = reader.getReader().document(docId).get(Constants.FIELD_PATH);
            return new File(path);
        } catch (IOException e) {
            throw new LuceneSearchException("PDFRetriever: IO Exception while accessing index");
        }
    }
}
