package full_text_analysis;

import access_utils.TermLocationsSearcher;
import access_utils.data.TermLocations;
import analyzers.search.SearchAnalyzer;
import common.Constants;
import common.data.ScoredTerm;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.document.Document;
import reader.IndexReader;
import searcher.exception.LuceneSearchException;
import util.Searcher;

import java.io.IOException;
import java.util.List;

/**
 * This class implements a compound related terms search.
 * Created by chris on 12/16/15.
 */
public class CompoundRelatedTerms extends Searcher{
    public CompoundRelatedTerms(IndexReader reader) throws LuceneSearchException {
        super(reader, new SearchAnalyzer(KeywordTokenizer.class));
    }

    public List<ScoredTerm> getCompoundRelatedTerms(String term) throws LuceneSearchException{
        TermLocationsSearcher tlSearcher = new TermLocationsSearcher(reader);
        List<TermLocations> termLocations = tlSearcher.getLocationsOfTerm(term);

        for(TermLocations loc : termLocations){
            Document doc;
            try {
                doc = reader.getReader().document(loc.docId);
            } catch (IOException e) {
                System.err.println("There was an error getting document " + loc.docId + ": " + e.getMessage());
                continue; // Go to next set of term locations
            }
            String[] contents = doc.getValues(Constants.FIELD_CONTENTS);

            for(String content : contents){
                // TODO: Handle each term.
            }

        }
        return null;
    }
}
