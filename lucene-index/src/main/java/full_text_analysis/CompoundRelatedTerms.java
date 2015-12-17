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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class implements a compound related terms search.
 * Created by chris on 12/16/15.
 */
public class CompoundRelatedTerms extends Searcher{
    private final Set<String> stopwords;
    public CompoundRelatedTerms(IndexReader reader, String stopwordFile) throws LuceneSearchException {
        super(reader, new SearchAnalyzer(KeywordTokenizer.class));
        stopwords = new HashSet<>();
        try{
            BufferedReader fileReader = new BufferedReader(new FileReader(stopwordFile));
            String line;
            while((line = fileReader.readLine()) != null){
                stopwords.add(line);
            }
        }catch(FileNotFoundException e){
            System.err.println("Stopword File: " + stopwordFile + " could not be found.");
        }catch (IOException e){
            System.err.println("There was an error reading the file.");
        }
    }

    public List<ScoredTerm> getCompoundRelatedTerms(String term) throws LuceneSearchException{
        // TODO: Potentially stem the term that we are looking for?
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

            for(int location : loc.getLocations()){
                String baseTerm = contents[location];

                // Check the plus one location
                if(location + 1 < contents.length){
                    String addTerm = contents[location + 1];
                    if(!stopwords.contains(addTerm)){
                        // Do the relatedness search
                        String compoundTerm = baseTerm + " " + addTerm;


                    }

                }

                // Check the minus one location
                if(location - 1 > 0){

                }



            }

        }
        return null;
    }
}
