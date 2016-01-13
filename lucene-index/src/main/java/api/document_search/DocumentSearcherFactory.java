package api.document_search;

import internal.analyzers.search.SearchAnalyzer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import api.reader.IndexReader;
import api.exception.LuceneSearchException;

/**
 * Creates instances of DocumentSearchers
 * Created by chris on 12/14/15.
 */
public class DocumentSearcherFactory {
    // Do not allow the class to be instantiated
    private DocumentSearcherFactory() {
    }

    /**
     * Returns a Document Searcher Object, given an index api.reader and a Tokenizer type
     *
     * @param reader The index api.reader that should be used for the document searcher
     * @param type   The type of Tokenizer that the document searcher should use
     * @return A document searcher with the needed parameters. Returns a whitespace tokenizer by default.
     * @throws LuceneSearchException
     */
    public static LuceneDocumentSearcher getDocumentSearcher(IndexReader reader, TokenizerType type) throws LuceneSearchException {
        switch (type) {
            case WHITESPACE_TOKENIZER:
                return new LuceneDocumentSearcher(reader, new SearchAnalyzer(WhitespaceTokenizer.class));
            case KEYWORD_TOKENIZER:
                return new LuceneDocumentSearcher(reader, new SearchAnalyzer(KeywordTokenizer.class));
            default:
                return new LuceneDocumentSearcher(reader, new SearchAnalyzer(WhitespaceTokenizer.class));
        }
    }

    public static LuceneDocumentSearcher defaultDocumentSearcher(IndexReader reader) throws LuceneSearchException{
        return new LuceneDocumentSearcher(reader, new SearchAnalyzer(WhitespaceTokenizer.class));
    }

    public enum TokenizerType{
        WHITESPACE_TOKENIZER,
        KEYWORD_TOKENIZER
    }
}
