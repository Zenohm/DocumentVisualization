package exception;

/**
 * Class used to make the search exception generic
 * Created by chris on 1/8/16.
 */
public abstract class SearchException extends Exception{
    public SearchException(String message){
        super(message);
    }
}
