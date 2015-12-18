package full_text_analysis;

import access_utils.FullTextTokenizer;
import access_utils.TermLocationsSearcher;
import access_utils.data.TermLocations;
import analyzers.filters.NumberFilter;
import analyzers.search.SearchAnalyzer;
import common.Constants;
import common.data.ScoredTerm;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.document.Document;
import reader.IndexReader;
import searcher.exception.LuceneSearchException;
import util.Searcher;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

        //TODO: I should really parallelize this, because it is going to be slow.
        Set<String> potentialCompoundTerms = Collections.newSetFromMap(new ConcurrentHashMap<>());
        termLocations.parallelStream().forEach(loc ->{
            Document doc;
            try {
                doc = reader.getReader().document(loc.docId);
            } catch (IOException e) {
                System.err.println("There was an error getting document " + loc.docId + ": " + e.getMessage());
                return;
            }

            String[] contents;
            try {
                String[] values = doc.getValues(Constants.FIELD_CONTENTS);
                String totalContent = "";
                for(String content : values){
                    totalContent += content + " ";
                }
                contents = FullTextTokenizer.tokenizeText(totalContent);
            } catch (IOException e) {
                System.err.println("There was an error while tokenizing the text for " + loc.docId + ": " + e.getMessage());
                return;
            } catch (ArrayIndexOutOfBoundsException e){
                System.err.println("Document #" + loc.docId + " doesn't have contents?");
                return;
            }


            loc.getLocations().parallelStream()
                    .filter(location -> location + 1 < contents.length)
                    .map(location -> new ImmutablePair<>(contents[location].toLowerCase().trim(),
                            contents[location + 1].toLowerCase().trim()))
                    .filter(content -> !stopwords.contains(content.getRight()))
                    .filter(content -> !NumberFilter.isNumeric(content.getRight()))
                    .map(content -> content.getLeft() + " " + content.getRight())
                    .forEach(potentialCompoundTerms::add);

            loc.getLocations().parallelStream()
                    .filter(location -> location - 1 >= 0)
                    .map(location -> new ImmutablePair<>(contents[location].toLowerCase().trim(),
                            contents[location - 1].toLowerCase().trim()))
                    .filter(content -> !stopwords.contains(content.getRight()))
                    .filter(content -> !NumberFilter.isNumeric(content.getRight()))
                    .map(content -> content.getRight() + " " + content.getLeft())
                    .forEach(potentialCompoundTerms::add);
        });

        return TermRelatednessScorer.getRankedTermsWithScores(term, potentialCompoundTerms, 0);
    }
}
