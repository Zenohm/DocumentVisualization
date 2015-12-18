package full_text_analysis.util;

import analyzers.filters.AlphaNumericFilter;
import analyzers.filters.NumberFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Stemming Term analyzer, very similar to PDF analyzer. Used to stem terms before they are searched for.
 * Created by chris on 10/16/15.
 */
public class StemmingTermAnalyzer extends Analyzer {
    private final List<String> stopwords;

    /**
     * Intialize the StemmingTermAnalyzer with a stopwordFile
     *
     * @param stopwordFile The stopword file
     */
    public StemmingTermAnalyzer(String stopwordFile) {
        stopwords = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(stopwordFile);
            BufferedReader reader = new BufferedReader(fileReader);
            String line;
            while ((line = reader.readLine()) != null) {
                stopwords.add(line);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Stopword File: " + stopwordFile + " could not be found");
        } catch (IOException e) {
            System.err.println("There was an error reading the file.");
        }


    }

    @Override
    protected Analyzer.TokenStreamComponents createComponents(String s) {
        StringReader reader = new StringReader(s);
        Tokenizer tokenizer = new StandardTokenizer();
        try {
            tokenizer.setReader(reader);
        } catch (IOException e) {
            // TODO: Better error handling
            e.printStackTrace();
        }

        TokenStream filter = new StandardFilter(tokenizer);
        filter = new LowerCaseFilter(filter);
        filter = new StopFilter(filter, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
        filter = new StopFilter(filter, StopFilter.makeStopSet(stopwords));
        filter = new NumberFilter(filter);
        filter = new AlphaNumericFilter(filter);
        filter = new SnowballFilter(filter, new EnglishStemmer());
        return new Analyzer.TokenStreamComponents(tokenizer, filter);
    }
}
