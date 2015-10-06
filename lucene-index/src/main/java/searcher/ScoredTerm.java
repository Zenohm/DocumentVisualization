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

package searcher;

/**
 * This class represents a term within a document with a score. This is for finding terms that are related within
 * the document corpus.
 */
public class ScoredTerm implements Comparable<ScoredTerm> {
    public final String text;
    public final double score;

    /**
     * Constructor
     *
     * @param text  Text of the term
     * @param score The score for the term
     */
    public ScoredTerm(String text, double score) {
        this.text = text;
        this.score = score;
    }

    /**
     * Compare to method. Compares a scored term to another score term
     * @param o The other scored term.
     * @return -1 if this term is less than the other term
     * 1 if it is greater than the other term
     * 0 if they are equal
     */
    @Override
    public int compareTo(ScoredTerm o) {
        if (this.score == o.score) return 0;
        if (this.score > o.score) {
            return 1;
        }
        return -1;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o instanceof ScoredTerm) {
            ScoredTerm term = (ScoredTerm) o;
            if (term.text.equals(this.text)) return true;
        }
        return false;
    }
}
