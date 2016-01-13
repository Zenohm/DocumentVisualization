package api.document_access;

import common.Constants;
import common.data.DocumentMetadata;
import document_access.DocumentInfoRetriever;
import org.apache.lucene.document.Document;
import api.reader.IndexReader;
import api.exception.LuceneSearchException;
import internal.lucene_intf.LuceneReader;

import java.io.IOException;

/**
 * Class for getting metadata from the document database
 * Created by chris on 10/6/15.
 */
public class MetadataRetriever extends LuceneReader implements DocumentInfoRetriever {
    public MetadataRetriever(IndexReader reader) throws LuceneSearchException {
        super(reader);
    }

    /**
     * Gets metadata for the document id
     *
     * @param documentId The document id to get metadata for
     * @return Metadata for the document
     * @throws LuceneSearchException
     */
    public DocumentMetadata getMetadata(int documentId) throws LuceneSearchException {
        Document document;
        try {
            document = reader.getReader().document(documentId);
        } catch (IOException e) {
            throw new LuceneSearchException("MetadataRetriever: Could not get a document for the document ID.");
        }
        if (document == null)
            throw new LuceneSearchException("MetadataRetriever: Could not get a document for the document ID.");

        DocumentMetadata metadata = new DocumentMetadata(document.get(Constants.FIELD_PATH),         // Filename
                document.get(Constants.FIELD_TITLE),        // Title
                document.get(Constants.FIELD_AUTHOR),       // Author
                document.get(Constants.FIELD_CONFERENCE));  // Conference

        return metadata;
    }

    /**
     * Gets the title for a given document
     *
     * @param documentId The document id
     * @return The title of the document
     * @throws LuceneSearchException
     */
    public String getTitle(int documentId) throws LuceneSearchException {
        return getMetadata(documentId).getTitle();
    }

    /**
     * Gets the author for a given document
     *
     * @param documentId The document id
     * @return The author of the document
     * @throws LuceneSearchException
     */
    public String getAuthor(int documentId) throws LuceneSearchException {
        return getMetadata(documentId).getAuthor();
    }

    /**
     * Gets the conference for a given document
     *
     * @param documentId The document id
     * @return The conference that the document was published in
     * @throws LuceneSearchException
     */
    public String getConference(int documentId) throws LuceneSearchException {
        return getMetadata(documentId).getConference();
    }
}
