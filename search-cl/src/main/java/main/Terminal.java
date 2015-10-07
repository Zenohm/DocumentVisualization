package main;

import indexer.PDFIndexer;
import searcher.DocumentSearcher;
import searcher.MetadataRetriever;
import searcher.exception.LuceneSearchException;
import searcher.reader.LuceneIndexReader;
import util.IndexerConstants;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by chris on 10/5/15.
 */
public class Terminal {
    public static final String INDEX_OUT = "index";

    public static void main(String[] args) throws Exception {
        // Initialize the index
        PDFIndexer indexer = new PDFIndexer(INDEX_OUT, System.getenv(IndexerConstants.RESOURCE_FOLDER_VAR));
//        indexer.updateIndex();

        if(!LuceneIndexReader.getInstance().initializeIndexReader(INDEX_OUT))
        {
            System.err.println("Initializer Error: Could Not Initialize IndexReader");
        }

        DocumentSearcher searcher = new DocumentSearcher(LuceneIndexReader.getInstance());
        MetadataRetriever retriever = new MetadataRetriever(LuceneIndexReader.getInstance());

        Scanner kb = new Scanner(System.in);
        while(true){
            System.out.print("Enter a search term: ");
            String search = kb.nextLine();
            if (search.equals("") || search.isEmpty()) break;
            List<Map.Entry<Double, Integer>> docs = searcher.searchForTerm(search);
            docs.stream().forEach(doc -> {
                try {
                    System.out.println(doc.getKey() + "\t" + retriever.getTitle(doc.getValue()));
                } catch (LuceneSearchException e) {
                    System.err.println("Error finding document title: " + e.toString());
                }
            });
        }
        kb.close();
    }
}
