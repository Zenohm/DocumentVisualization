package api.document_access;

import common.Constants;
import document_access.DocumentRetriever;
import api.reader.IndexReader;
import api.exception.LuceneSearchException;
import internal.lucene_intf.LuceneReader;

import java.io.File;
import java.io.IOException;

/**
 * Gets a PDF file
 * Created by Chris on 10/8/2015.
 */
public class PDFRetriever extends LuceneReader implements DocumentRetriever{
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
    public File getDocument(int docId) throws LuceneSearchException {
        try {
            String path = reader.getReader().document(docId).get(Constants.FIELD_PATH);
            return new File(path);
        } catch (IOException e) {
            throw new LuceneSearchException("PDFRetriever: IO Exception while accessing index");
        }
    }
}
