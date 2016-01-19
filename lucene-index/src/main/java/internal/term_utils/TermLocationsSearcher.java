package internal.term_utils;

import api.exception.LuceneSearchException;
import api.reader.IndexReader;
import common.Constants;
import common.data.TermLocations;
import internal.lucene_intf.LuceneReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocIdSetIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 12/16/15.
 */
public class TermLocationsSearcher extends LuceneReader {
    private static final Log log = LogFactory.getLog(TermLocationsSearcher.class);
    private final LeafReader lReader;

    public TermLocationsSearcher(IndexReader reader) throws LuceneSearchException {
        super(reader);
        try {
            lReader = SlowCompositeReaderWrapper.wrap(reader.getReader());
        } catch (IOException e) {
            throw new LuceneSearchException("Could Not Wrap IndexReader: " + e.getMessage());
        }
    }

    public List<TermLocations> getLocationsOfTerm(String term) throws LuceneSearchException {
        // TODO: Possibly need to stem term??
        Term searchTerm = new Term(Constants.FIELD_CONTENTS, term);

        PostingsEnum postings;
        try {
            postings = lReader.postings(searchTerm, PostingsEnum.ALL);
        } catch (IOException e) {
            throw new LuceneSearchException("There was an error getting the postings: " + e.getMessage());
        }
        if (postings == null) {
            log.error("Could not get postings. Postings were probably not included in indexing. Update your indexer.");
            return new ArrayList<>();
        }

        int docId;
        try {
            docId = postings.nextDoc();
        } catch (IOException e) {
            throw new LuceneSearchException("Next doc threw an exception while getting initial doc: " + e.getMessage());
        }

        // Overall list of all locations
        List<TermLocations> locationsList = new ArrayList<>();
        while (docId != DocIdSetIterator.NO_MORE_DOCS) {
            // The term locations within a single document.
            TermLocations locations = new TermLocations(docId);

            int numHits = 0;
            try {
                numHits = postings.freq();
            } catch (IOException e) {
                log.error("Could not get the frequency of the postings! Were frequencies stored?");
            }

            int i = 0;
            while (i < numHits) {
                try {
                    locations.addTermLocation(postings.nextPosition());
                } catch (IOException e) {
                    log.error("There was an error getting term location for " + docId);
                }
                i++;
            }

            try {
                docId = postings.nextDoc();
            } catch (IOException e) {
                log.error("No more documents!");
                docId = DocIdSetIterator.NO_MORE_DOCS;
            }

            locationsList.add(locations);
        }

        return locationsList;
    }
}
