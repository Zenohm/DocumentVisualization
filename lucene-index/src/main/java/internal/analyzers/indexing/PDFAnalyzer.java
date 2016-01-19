/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Chris Bellis, Chris Perry
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package internal.analyzers.indexing;

import com.google.common.collect.Lists;
import common.StopwordsProvider;
import internal.analyzers.filters.AlphaNumericFilter;
import internal.analyzers.filters.NumberFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.util.CharArraySet;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;


/**
 * Analyzer used for PDF indexing.
 * Created by Chris on 8/20/2015.
 */
public class PDFAnalyzer extends Analyzer {
    private final CharArraySet stop_set; // DO THIS ONCE!

    private static final Log log = LogFactory.getLog(PDFAnalyzer.class);

    /**
     * Instantiate a new PDF analyzer
     *
     * @param stopwordFile The file containing all the stopwords
     */
    public PDFAnalyzer(String stopwordFile) {
        List<String> stop_list =
                Lists.newArrayList(StopwordsProvider.getProvider(stopwordFile).getStopwords());
        stop_set = StopFilter.makeStopSet(stop_list);
    }

    @Override
    protected Analyzer.TokenStreamComponents createComponents(String s) {
        StringReader reader = new StringReader(s);
        Tokenizer tokenizer = new StandardTokenizer();
        try {
            tokenizer.setReader(reader);
        } catch (IOException e) {
            log.error("Could not set reader on tokenizer. Threw IO exception");
        }

        TokenStream filter = new StandardFilter(tokenizer);
        filter = new LowerCaseFilter(filter);
        filter = new StopFilter(filter, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
        filter = new StopFilter(filter, stop_set);
        filter = new NumberFilter(filter);
        filter = new AlphaNumericFilter(filter);
        return new TokenStreamComponents(tokenizer, filter);
    }
}
