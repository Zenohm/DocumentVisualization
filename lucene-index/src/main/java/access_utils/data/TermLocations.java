package access_utils.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 12/16/15.
 */
public class TermLocations {
    public final int docId;
    public final List<Integer> locations;

    public TermLocations(int docId) {
        this.docId = docId;
        locations = new ArrayList<>();
    }

    public void addTermLocation(int loc) {
        locations.add(loc);
    }

    public List<Integer> getLocations() {
        return new ArrayList<>(locations);
    }
}
