package analyzers.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.*;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Logger;

/**
 * Created by chris on 12/14/15.
 */
public class SearchAnalyzer extends Analyzer{

    private Class<? extends Tokenizer> tokenizerClass;

    public SearchAnalyzer(Class<? extends Tokenizer> tokenizerClass){
        this.tokenizerClass = tokenizerClass;
    }

    /**
     *
     * @param s
     * @return
     */
    @Override
    protected TokenStreamComponents createComponents(String s) {
        StringReader reader = new StringReader(s);
        Tokenizer tokenizer = getTokenizer();
        if(tokenizer == null){
            System.err.println("Reverting to whitespace tokenizer");
            tokenizer = new WhitespaceTokenizer();
        }

        try {
            tokenizer.setReader(reader);
        } catch (IOException e) {
            // TODO: Better error handling
            e.printStackTrace();
        }
        TokenStream filter = new StandardFilter(tokenizer);
        filter = new LowerCaseFilter(filter);
        filter = new StopFilter(filter, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
        filter = new SnowballFilter(filter, new EnglishStemmer()); // Stemmer

        return new TokenStreamComponents(tokenizer, filter);
    }

    /**
     *
     * @return The tokenizer that has been assigned to this class
     */
    private Tokenizer getTokenizer(){
        try {
            return tokenizerClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            System.err.println("There was an error getting the tokenizer");
            e.printStackTrace();
        }
        return null;
    }

}
