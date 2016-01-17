package internal.static_util;

import common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;

import java.io.IOException;

/**
 * Extracts the full text from the index. Removes newlines
 * Created by chris on 10/13/15.
 */
public class FullTextExtractor {
    public static final String FAILED_TEXT = "FAILEDTOEXTRACT";

    private static final Log log = LogFactory.getLog(FullTextExtractor.class);

    /**
     * Extracts the full text given the document ID
     *
     * @param reader Index api.reader to use. in order to find the full text
     * @param docId  The document ID to get the full text for
     * @return The full text as a string. Removes all newlines.
     */
    public static String extractText(IndexReader reader, int docId) {
        // Remove all the newlines
        try {
            return reader.document(docId).get(Constants.FIELD_CONTENTS)
                    .replaceAll("\\r\\n|\\r|\\n", " ").toLowerCase();
        } catch (IOException e) {
            log.error("Could not get document with id: " + docId);
        }
        return FAILED_TEXT;
    }

    public static String extractText(api.reader.IndexReader reader, int docId){
        IndexReader iReader = reader.getReader();
        if(iReader == null){
            return FAILED_TEXT;
        }
        return extractText(iReader, docId);
    }

}
