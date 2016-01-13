package document_access;

import exception.SearchException;

import java.io.File;

/**
 * Created by chris on 1/8/16.
 */
public interface DocumentRetriever {
    File getDocument(int docId) throws SearchException;
}
