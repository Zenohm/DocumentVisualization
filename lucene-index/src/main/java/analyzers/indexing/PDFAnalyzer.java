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

package analyzers.indexing;

import analyzers.filters.AlphaNumericFilter;
import analyzers.filters.NumberFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Analyzer used for PDF indexing.
 * Created by Chris on 8/20/2015.
 */
public class PDFAnalyzer extends Analyzer {
    private final List<String> stopwords;

    /**
     * Instantiate a new PDF analyzer
     *
     * @param stopwordFile The file containing all the stopwords
     */
    public PDFAnalyzer(String stopwordFile) {
        stopwords = new ArrayList<>();
        try {
            // Read file and add all the stopwords to our list
            BufferedReader reader = new BufferedReader(new FileReader(stopwordFile));
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
        return new TokenStreamComponents(tokenizer, filter);
    }
}
