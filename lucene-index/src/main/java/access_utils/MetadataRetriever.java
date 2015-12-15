package access_utils;

import common.data.DocumentMetadata;
import org.apache.lucene.document.Document;
import searcher.Searcher;
import searcher.exception.LuceneSearchException;
import reader.IndexReader;
import common.Constants;

import java.io.IOException;

/**
 * Created by chris on 10/6/15.
 */
public class MetadataRetriever extends LuceneReader {
    public MetadataRetriever(IndexReader reader) throws LuceneSearchException {
        super(reader);
    }

    public DocumentMetadata getMetadata(int documentId) throws LuceneSearchException {
        Document document = null;
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

    public String getTitle(int documentId) throws LuceneSearchException {
        return getMetadata(documentId).getTitle();
    }

    public String getAuthor(int documentId) throws LuceneSearchException {
        return getMetadata(documentId).getAuthor();
    }

    public String getConference(int documentId) throws LuceneSearchException {
        return getMetadata(documentId).getConference();
    }
}
