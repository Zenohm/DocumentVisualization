package document_access;

import common.data.DocumentMetadata;
import exception.SearchException;

/**
 * Created by chris on 1/8/16.
 */
public interface DocumentInfoRetriever {
    DocumentMetadata getMetadata(int docId) throws SearchException;
}
