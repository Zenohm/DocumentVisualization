package indexer;

import full_text_analysis.data.TextTokenizer;
import org.apache.lucene.index.IndexReader;
import reader.LuceneIndexReader;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by chris on 1/5/16.
 */
public class TextTokenizerWarmer {
    private TextTokenizerWarmer(){}
    public static void tokenizeAllText(){
        IndexReader reader = LuceneIndexReader.getInstance().getReader();
        TextTokenizer tokenizer =  TextTokenizer.getInstance();
        ThreadPoolExecutor pool = (ThreadPoolExecutor)Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for(int i = 0; i<reader.numDocs(); i++){
            final int docId = i;
            pool.submit(() -> tokenizer.populateCache(docId));
        }
        new Thread(() -> {
            while(reader.numDocs() > pool.getCompletedTaskCount());
            System.out.println("Done warming tokenizer");
        }).start();
    }
}
