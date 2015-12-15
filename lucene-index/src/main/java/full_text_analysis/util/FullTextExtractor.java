package full_text_analysis.util;

import org.apache.lucene.index.IndexReader;
import common.Constants;

import java.io.IOException;

/**
 * Created by chris on 10/13/15.
 */
public class FullTextExtractor {
    public static final String FAILED_TEXT = "FAILEDTOEXTRACT";
    public static String extractFullText(IndexReader reader, int docId) {
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
