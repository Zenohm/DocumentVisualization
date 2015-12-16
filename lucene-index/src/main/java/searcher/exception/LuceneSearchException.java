package searcher.exception;

/**
 * Exception to be thrown when lucene has an issue doing a search.
 * Created by chris on 10/5/15.
 */
public class LuceneSearchException extends Exception {
    public LuceneSearchException(String message) {
        super(message);
    }
}
