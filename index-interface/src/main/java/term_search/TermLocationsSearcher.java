package term_search;

import common.data.TermLocations;

import java.util.List;

/**
 * Created by chris on 1/8/16.
 */
public interface TermLocationsSearcher {
    List<TermLocations> getTermLocations(String term);
}
