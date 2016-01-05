package full_text_analysis.data;

import access_utils.FullTextTokenizer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import common.Constants;
import org.apache.lucene.document.Document;
import reader.LuceneIndexReader;
import searcher.exception.LuceneSearchException;
import util.LuceneReader;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by chris on 1/5/16.
 */
public class TextTokenizer extends LuceneReader{
    private static Cache<Integer, String[]> tokenCache = CacheBuilder.newBuilder().maximumSize(Constants.MAX_CACHE_SIZE).build();
    private static TextTokenizer instance;
    private TextTokenizer() throws LuceneSearchException{
        super(LuceneIndexReader.getInstance());
        instance = null;
    }
    public static TextTokenizer getInstance(){
        if(instance == null){
            try {
                instance = new TextTokenizer();
            } catch (LuceneSearchException e) {
                System.err.println("ERROR: Could not create text tokenizer");
                e.printStackTrace();
            }
        }
        return instance;
    }

    public String[] getTokenizedText(int docId){
        try {
            return tokenCache.get(docId, () -> tokenizeText(docId));
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    public void populateCache(int docId){
        if(!tokenCache.asMap().containsKey(docId)) {
            tokenCache.put(docId, tokenizeText(docId));
        }
    }

    private String[] tokenizeText(int docId){
        Document doc;
        try {
            doc = reader.getReader().document(docId);
        } catch (IOException e) {
            System.err.println("There was an error getting document " + docId + ": " + e.getMessage());
            return null;
        }

        String[] contents;
        try {
            String[] values = doc.getValues(Constants.FIELD_CONTENTS);
            String totalContent = "";
            for(String content : values){
                totalContent += content + " ";
            }
            contents = FullTextTokenizer.tokenizeText(totalContent);
        } catch (IOException e) {
            System.err.println("There was an error while tokenizing the text for " + docId + ": " + e.getMessage());
            return null;
        }

        return contents;
    }

}
