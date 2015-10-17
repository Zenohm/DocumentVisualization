package searcher.analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Created by chris on 10/15/15.
 * A flexible Lucene analyzer for when you just want that one feature.
 */
public class FlexAnalyzerBuilder {

    public Tokenizer tokenizer;
    public List<Class<? extends TokenFilter>> filters;

    public static FlexAnalyzerBuilder getBuilder() {
        return new FlexAnalyzerBuilder();
    }

    // TODO: Think this through some more!
    private FlexAnalyzerBuilder() {
        tokenizer = new WhitespaceTokenizer();
        filters = new ArrayList<>();
    }

    public FlexAnalyzerBuilder setTokenizer(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        return this;
    }

    public FlexAnalyzerBuilder addFilter(Class<? extends TokenFilter> filter) {
        filters.add(filter);
        return this;
    }

    public FlexAnalyzer build() {
        FlexAnalyzer analyzer = new FlexAnalyzer(filters, tokenizer);
        return analyzer;
    }

    class FlexAnalyzer extends Analyzer {
        Tokenizer tokenizer;
        List<Class<? extends TokenFilter>> filters;

        public FlexAnalyzer(List<Class<? extends TokenFilter>> filters, Tokenizer tokenizer) {
            filters = new ArrayList<>(filters);
            this.tokenizer = tokenizer;
        }
        @Override
        protected TokenStreamComponents createComponents(String s) {
            StringReader reader = new StringReader(s);
            try{
                tokenizer.setReader(reader);
            }catch (IOException e){
                e.printStackTrace();
            }
            TokenStream filter = new StandardFilter(tokenizer);
            for(Class<? extends TokenFilter> f : filters) {
                try {
                    filter = f.getConstructor().newInstance(filter);
                } catch (InstantiationException |
                         IllegalAccessException |
                         NoSuchMethodException  |
                         InvocationTargetException e) {
                    System.err.println("Could not create an instance of " + f.getName());
                }
            }
            return new TokenStreamComponents(tokenizer, filter);
        }
    }

}
