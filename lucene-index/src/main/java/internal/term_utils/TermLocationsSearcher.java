package internal.term_utils;

import common.data.TermLocations;
import common.Constants;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.DocIdSetIterator;
import api.reader.IndexReader;
import api.exception.LuceneSearchException;
import internal.lucene_intf.LuceneReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 12/16/15.
 */
public class TermLocationsSearcher extends LuceneReader {
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
        // TODO: Possibly need to stem term
        Term searchTerm = new Term(Constants.FIELD_CONTENTS, term);

        PostingsEnum postings;
        try {
            postings = lReader.postings(searchTerm, PostingsEnum.ALL);
        } catch (IOException e) {
            throw new LuceneSearchException("There was an error getting the postings: " + e.getMessage());
        }
        if (postings == null) {
            System.err.println("No Postings Found!");
            return new ArrayList<>();
        }

        int docId;
        try {
            docId = postings.nextDoc();
        } catch (IOException e) {
            throw new LuceneSearchException("Next doc threw an exception while getting initial doc: " + e.getMessage());
        }

        List<TermLocations> locationsList = new ArrayList<>();

        while (docId != DocIdSetIterator.NO_MORE_DOCS) {
            TermLocations locations = new TermLocations(docId);

            int numHits = 0;
            try {
                numHits = postings.freq();
            } catch (IOException e) {
                System.err.println("Num Hits threw an exception: " + e.getMessage());
            }

            int i = 0;
            while (i < numHits) {
                try {
                    locations.addTermLocation(postings.nextPosition());
                } catch (IOException e) {
                    System.err.println("There was an error getting a term location for " + docId +
                            "Message: " + e.getMessage());
                }
                i++;
            }

            try {
                docId = postings.nextDoc();
            } catch (IOException e) {
                System.err.println("No more documents were found.");
                docId = DocIdSetIterator.NO_MORE_DOCS;
            }

            locationsList.add(locations);
        }

        return locationsList;
    }
}
