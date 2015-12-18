package access_utils;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by chris on 12/17/15.
 */
public class FullTextTokenizer {

    // TODO: Add caching to this for speedup.
    /**
     * Returns tokenized version of full text
     * @param text The Full text
     * @return The tokenized text
     * @throws IOException
     */
    public static String[] tokenizeText(String text) throws IOException{
        ArrayList<String> tokens = new ArrayList<>();
        Reader stringReader = new StringReader(text);
        Tokenizer tokenizer = new StandardTokenizer();
        tokenizer.setReader(stringReader);
        tokenizer.reset();
        while(tokenizer.incrementToken()){
            tokens.add(tokenizer.getAttribute(CharTermAttribute.class).toString().trim());
        }

        String[] tokeArray = new String[tokens.size()];
        return tokens.toArray(tokeArray);
    }
}
