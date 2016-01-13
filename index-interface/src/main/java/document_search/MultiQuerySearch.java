package document_search;

import common.results.MultiQueryResults;
import java.io.IOException;
import java.util.List;

/**
 * Created by chris on 1/12/16.
 */
public interface MultiQuerySearch {
    List<MultiQueryResults> searchForResults(String... queries) throws IOException;
}
