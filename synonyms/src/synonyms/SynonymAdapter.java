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
package synonyms;

import edu.smu.tspell.wordnet.*;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 *
 * @author perryc on 10/10/15
 */
public class SynonymAdapter {
    private final static String DICT_PATH = new File("synonyms/resources/WordNet/dict").getAbsolutePath();

    // Command line version of the servlet.
    public static void main(String[] args) {
        while(true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter a word");
            String word = sc.nextLine();
            System.out.println(getSynonyms(word));
        }
        // TODO: CASE COMPARISON--IMPROVE IT
    }
    /**
     * @param word The word to find synonyms for
     * @return A set of synonyms for the given #word# in no particular order
     */
    public static Set getSynonyms(String word) {
        // Run the necessary initialization stuff
        init();

        // Initialize the dictionary db
        WordNetDatabase db = WordNetDatabase.getFileInstance();
        Set synonyms = new HashSet();

        // Sanitize the input
        String cleansedWord = word.toLowerCase().trim();

        // Get the synsets, which will each contain a set of synonyms for a given definition of the word.
        Synset[] synsets = db.getSynsets(cleansedWord);
        for (Synset synset : synsets) {
            // Add all of the wordForms (Either a single word or a phrase) from the given synset
            synonyms.addAll(Arrays.asList(synset.getWordForms()));
        }

        // The way wordNet processes synsets adds the search term into its synsets--we need to strip that out.
        synonyms.remove(cleansedWord);
        synonyms.remove(capitalize(cleansedWord));

        // If we found any synonyms, return them
        if (synonyms.size() > 0) {
            return synonyms;
        } else {
            // Otherwise return null
            return null;
        }
    }

    private static void init() {
        // Necessary for wordnet-JAWS interaction
        System.setProperty("wordnet.database.dir", DICT_PATH);
    }

    /**
     * Convenience method to capitalize the first character of a term
     * @param term a word or group of words to be capitalized
     * @return term with the first character capitalized
     */
    private static String capitalize(String term) {
        return term.substring(0, 1).toUpperCase() + term.substring(1);
    }

}
