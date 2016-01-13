package internal.analyzers.search;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;

/**
 * Analyzer that is used for searching. Can have a variable tokenizer
 * Created by chris on 12/14/15.
 */
public class SearchAnalyzer extends Analyzer {

    private Class<? extends Tokenizer> tokenizerClass;
    private static final Log log = LogFactory.getLog(SearchAnalyzer.class);

    /**
     * Constructor that allows for picking a tokenizer that will be used
     *
     * @param tokenizerClass The class to use for a tokenizer
     */
    public SearchAnalyzer(Class<? extends Tokenizer> tokenizerClass) {
        this.tokenizerClass = tokenizerClass;
    }

    @Override
    protected TokenStreamComponents createComponents(String s) {
        StringReader reader = new StringReader(s);
        Tokenizer tokenizer = getTokenizer();
        if (tokenizer == null) {
            log.error("Reverting to whitespace tokenizer because tokenizer is not set");
            tokenizer = new WhitespaceTokenizer();
        }

        try {
            tokenizer.setReader(reader);
        } catch (IOException e) {
            log.warn("Could not set reader on tokenizer");
        }
        TokenStream filter = new StandardFilter(tokenizer);
        filter = new LowerCaseFilter(filter);
        filter = new StopFilter(filter, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
        filter = new SnowballFilter(filter, new EnglishStemmer()); // Stemmer

        return new TokenStreamComponents(tokenizer, filter);
    }

    /**
     * @return The tokenizer that has been assigned to this class
     */
    private Tokenizer getTokenizer() {
        try {
            return tokenizerClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Tokenizer could not be obtained or constructed");
        }
        return null;
    }

}
