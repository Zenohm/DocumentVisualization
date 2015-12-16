package full_text_analysis.util;

import common.Constants;
import org.apache.lucene.index.IndexReader;

import java.io.IOException;

/**
 * Extracts the full text from the index. Removes newlines
 * Created by chris on 10/13/15.
 */
public class FullTextExtractor {
    public static final String FAILED_TEXT = "FAILEDTOEXTRACT";

    /**
     * Extracts the full text given the document ID
     *
     * @param reader Index reader to use. in order to find the full text
     * @param docId  The document ID to get the full text for
     * @return The full text as a string. Removes all newlines.
     */
    public static String extractText(IndexReader reader, int docId) {
        // Remove all the newlines
        try {
            return reader.document(docId).get(Constants.FIELD_CONTENTS)
                    .replaceAll("\\r\\n|\\r|\\n", " ").toLowerCase();
        } catch (IOException e) {
            System.err.println("FullTextExtractor: Failed to obtain the document with id "
                    + docId + e.toString());
        }
        return FAILED_TEXT;
    }
}
