package api.startup;

import api.reader.LuceneIndexReader;
import internal.static_util.tokenizer.DocumentTokenizer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by chris on 1/5/16.
 */
public class TextTokenizerWarmer {
    private static final Log log  = LogFactory.getLog(TextTokenizerWarmer.class);
    private TextTokenizerWarmer(){}
    public static void tokenizeAllText(){
        IndexReader reader = LuceneIndexReader.getInstance().getReader();
        DocumentTokenizer tokenizer =  DocumentTokenizer.getInstance();
        ThreadPoolExecutor pool = (ThreadPoolExecutor)Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        long startTime = System.nanoTime();
        for(int i = 0; i<reader.numDocs(); i++){
            final int docId = i;
            pool.submit(() -> tokenizer.populateCache(docId));
        }
        Thread numDocsCounter = new Thread(() -> {
            while(reader.numDocs() > pool.getCompletedTaskCount());
            log.info("Tokenizer Warm Time:  " + (System.nanoTime() - startTime)/Math.pow(10, 9));
        });
        numDocsCounter.setName("TokenizerWarmerWaitThread");
        numDocsCounter.start();
    }
}
