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
 * Created by chris on 3/16/16.
 */
public class SetDictionary implements Dictionary {
    private static Log log = LogFactory.getLog(SetDictionary.class);
    private Set<String> words;
    private static SetDictionary instance;

    private SetDictionary() throws FileNotFoundException{
        this(System.getenv(Constants.RESOURCE_FOLDER_VAR) + "/" + Constants.DICTIONARY_FILE);
    }

    public SetDictionary(String wordListFile) throws FileNotFoundException {
        Scanner file = new Scanner(new BufferedInputStream(new FileInputStream(new File(wordListFile))));
        words = new HashSet<>();
        while(file.hasNextLine()){
            words.add(file.nextLine().trim());
        }
        file.close();
        log.info("Initialized Dictionary with: " + words.size() + " words.");
    }

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

    public boolean contains(String word){
        return words.contains(word);
    }


}
