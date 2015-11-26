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

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import mcUtils.ScoredTerm;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adapter to communicate with JAWS (which communicates with WordNet db files.)  Usage requires the setting of VM
 * options with the following:
 *    -Dwordnet.database.dir={Directory containing the 'dict' folder for wordnet files}
 *
 * @author perryc on 10/10/15
 */
public class SynonymAdapter {
    // The location of the dict files for WordNet
    private final static String DICT_PATH = new File("synonyms/resources/WordNet/dict").getAbsolutePath();

    // TODO: Remove this
    // Command line version of the servlet.
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while(true) {
            try {
                System.out.println("Enter a word");
                String word = sc.nextLine();
                Set<String> synonyms = getSynonyms(word);
                List<ScoredTerm> scoredTerms = SynonymScorer.getRankedSynonymsWithScores(word, synonyms);
                if (scoredTerms != null) {
                    if (word.equalsIgnoreCase("wordnet")) {
                        synonyms = getSynonyms("adequate");
                        synonyms.add("adequate");
                        synonyms.add("workable");
                        synonyms.add("bamboozling");
                    }
                    System.out.println(scoredTerms);
                } else {
                    System.out.println("Sorry, WordNet doesn't have any synonyms for " + word);
                }
            } catch (Exception e) {
                System.out.println(e.getLocalizedMessage());
                // Swallow exceptions, keep going.
            }
        }
    }

    /**
     * @param word The word to find synonyms for
     * @return A set of synonyms for the given #word# in no particular order or null if you didn't pass a word, jerk.
     */
    public static Set getSynonyms(String word) {
        if (word == null) {
            return null;
        }

        // Run the necessary initialization stuff
        // setWordNetDir();

        // Initialize the dictionary db
        WordNetDatabase db = WordNetDatabase.getFileInstance();
        Set<String> synonyms = new HashSet();

        // Sanitize the input
        String cleansedWord = word.toLowerCase().trim();

        // Get the synsets, which will each contain a set of synonyms for a given definition of the word.
        Synset[] synsets = db.getSynsets(cleansedWord);
        for (Synset synset : synsets) {
            // Add all of the wordForms (Either a single word or a phrase) from the given synset
            synonyms.addAll(Arrays.asList(synset.getWordForms()));
        }

        // Filter out any versions of #word# that may be capitalized in any form, then remove them from the set.
        Set<String> wordRepeats = synonyms.stream()
                .filter(synonym -> synonym.equalsIgnoreCase(word)).collect(Collectors.toSet());
        wordRepeats.forEach(synonyms::remove);

        // If we found any synonyms, return them
        if (synonyms.size() > 0) {
            return synonyms;
        } else {
            // Otherwise return null
            return null;
        }
    }
}
