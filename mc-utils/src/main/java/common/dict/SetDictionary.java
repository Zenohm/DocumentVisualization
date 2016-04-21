package common.dict;

import common.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * Dictionary that is backed by a set
 * Created by chris on 3/16/16.
 */
public class SetDictionary implements Dictionary {
    private static Log log = LogFactory.getLog(SetDictionary.class);
    private Set<String> words;
    private static SetDictionary instance;

    /**
     * Initializes a SetDicionary with the default parameters
     * @throws FileNotFoundException
     */
    private SetDictionary() throws FileNotFoundException{
        this(System.getProperty(Constants.RESOURCE_FOLDER_VAR) + "/" + Constants.DICTIONARY_FILE);
    }

    /**
     * Initialize the dictionary with a file
     * @param wordListFile - filename of a newline separated wordlist
     * @throws FileNotFoundException
     */
    public SetDictionary(String wordListFile) throws FileNotFoundException {
        Scanner file = new Scanner(new BufferedInputStream(new FileInputStream(new File(wordListFile))));
        words = new HashSet<>();
        while(file.hasNextLine()){
            words.add(file.nextLine().trim());
        }
        file.close();
        log.info("Initialized Dictionary with: " + words.size() + " words.");
    }

    /**
     * Utilize a singleton for performance reasons to ensure that the wordlist is only instantiated once
     * @return a SetDictionary
     */
    public static SetDictionary getInstance(){
        if(instance == null){
            try {
                instance = new SetDictionary();
            } catch (FileNotFoundException e) {
                log.fatal("Could not find dictionary file", e);
                return null;
            }
        }
        return instance;
    }

    /**
     * Check if the dictionary contains a word
     * @param word the word to check
     * @return true if the dictionary contains the word, false otherwise
     */
    public boolean contains(String word){
        return words.contains(word);
    }


}
