package searcher;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import searcher.exception.LuceneSearchException;
import searcher.reader.IndexReaderInterface;
import util.IndexerConstants;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by chris on 10/5/15.
 */
public class DocumentSearcher {
    private IndexReaderInterface reader;
    private IndexSearcher searcher;
    private QueryParser parser;
    private Analyzer analyzer;
    public DocumentSearcher(IndexReaderInterface reader) throws LuceneSearchException {
        if(!reader.isInitialized()) throw new LuceneSearchException("DocumentSearcher: IndexReaderInterface Not Initialized");
        this.reader = reader;
        this.searcher = new IndexSearcher(reader.getReader());
        this.analyzer = new SearchAnalyzer();
        this.parser = new QueryParser(IndexerConstants.FIELD_CONTENTS, analyzer);
    }

    public List<Map.Entry<Double, Integer>> searchForTerm(String term){
        try {
            Query query = parser.parse(term);
            final TopDocs search = searcher.search(query, 50);
            for(ScoreDoc doc : search.scoreDocs) {
                System.out.println(doc.score + "\t" + doc.doc);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
