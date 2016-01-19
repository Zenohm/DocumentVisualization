package internal.static_util.tokenizer;

import api.exception.LuceneSearchException;
import api.reader.LuceneIndexReader;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import common.Constants;
import internal.lucene_intf.LuceneReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by chris on 1/5/16.
 */
public class DocumentTokenizer extends LuceneReader{
    private static final Log log = LogFactory.getLog(DocumentTokenizer.class);
    private static Cache<Integer, String[]> tokenCache = CacheBuilder.newBuilder().maximumSize(Constants.MAX_CACHE_SIZE).build();
    private static DocumentTokenizer instance;
    private DocumentTokenizer() throws LuceneSearchException{
        super(LuceneIndexReader.getInstance());
    }
    public static DocumentTokenizer getInstance(){
        if(instance == null){
            try {
                instance = new DocumentTokenizer();
            } catch (LuceneSearchException e) {
                log.error("Could not create text tokenizer");
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
            log.error("There was an error getting document " + docId + ": " + e.getMessage());
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
            log.error("Could not tokenize text for " + docId + ": " + e.getMessage());
            return null;
        }

        return contents;
    }

}
