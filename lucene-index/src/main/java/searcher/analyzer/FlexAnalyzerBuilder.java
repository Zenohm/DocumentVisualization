package searcher.analyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;

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

    public FlexAnalyzer build() {
        FlexAnalyzer analyzer = new FlexAnalyzer(filters, tokenizer);
        return analyzer;
    }



    class FlexAnalyzer extends Analyzer {
        List<Class<? extends TokenFilter>> filters;

        public FlexAnalyzer(List<Class<? extends TokenFilter>> filters, Tokenizer tokenizer) {
            filters = new ArrayList<>(filters);
        }
        @Override
        protected TokenStreamComponents createComponents(String s) {
            throw new UnsupportedOperationException("Not implemented yet");
        }
    }

}
