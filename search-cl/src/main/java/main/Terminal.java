package main;

import indexer.PDFIndexer;
import searcher.DocumentSearcher;
import searcher.reader.LuceneIndexReader;
import util.IndexerConstants;

import java.util.Scanner;

/**
 * Created by chris on 10/5/15.
 */
public class Terminal {
    public static final String INDEX_OUT = "index";

    public static void main(String[] args) throws Exception {
        // Initialize the index
        PDFIndexer indexer = new PDFIndexer(INDEX_OUT, System.getenv(IndexerConstants.RESOURCE_FOLDER_VAR));
        indexer.updateIndex();

        if(!LuceneIndexReader.getInstance().initializeIndexReader(INDEX_OUT))
        {
            System.err.println("Initializer Error: Could Not Initialize IndexReaderInterface");
        }

        DocumentSearcher searcher = new DocumentSearcher(LuceneIndexReader.getInstance());

        while(true){
            System.out.print("Enter a search term: ");
            Scanner kb = new Scanner(System.in);
            String search = kb.next();
            searcher.searchForTerm(search);

        }
    }
}
