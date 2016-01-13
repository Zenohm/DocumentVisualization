package api.indexer;

import internal.static_util.tokenizer.DocumentTokenizer;
import org.apache.lucene.index.IndexReader;
import api.reader.LuceneIndexReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by chris on 1/5/16.
 */
public class TextTokenizerWarmer {
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
        new Thread(() -> {
            while(reader.numDocs() > pool.getCompletedTaskCount());
            System.out.println("Tokenizer Warm Time:  " + (System.nanoTime() - startTime)/Math.pow(10, 9));
        }).start();
    }
}
