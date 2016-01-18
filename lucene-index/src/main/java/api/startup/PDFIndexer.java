/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Chris Bellis, Chris Perry
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package api.startup;

import api.reader.LuceneIndexReader;
import common.Constants;
import common.data.DocumentMetadata;
import internal.analyzers.indexing.PDFAnalyzer;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import pdfs.PDFTextExtractor;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Chris on 8/19/2015.
 */
public class PDFIndexer{
    private static final Log log = LogFactory.getLog(PDFIndexer.class);
    public final String indexDirectory;
    public final String resourceDirectory;

    public PDFIndexer(String indexDirectory, String resourceDirectory) {
        this.indexDirectory = indexDirectory;
        this.resourceDirectory = resourceDirectory;
    }

    static void indexDocs(final IndexWriter writer, String indexConfiguration) throws IOException {
        Reader in = new FileReader(indexConfiguration);
        CSVParser parser = CSVFormat.RFC4180.withHeader().parse(in);
        List<Callable<Object>> tasks = new ArrayList<>();
        int threadPoolSize = Runtime.getRuntime().availableProcessors();
        log.info("Indexing with " + threadPoolSize + " processors");
        ExecutorService pool = Executors.newFixedThreadPool(threadPoolSize);
        for (CSVRecord record : parser) {
            DocumentMetadata meta = new DocumentMetadata(record);
            tasks.add(() -> {
                indexDoc(writer, meta);
                return null;
            });
        }

        try {
            pool.invokeAll(tasks);
        } catch (InterruptedException e) {
            log.error("Indexing was interrupted " + e.getMessage());
        }

    }

    static void indexDoc(IndexWriter writer, DocumentMetadata metadata) throws IOException {
        Path file = Paths.get(metadata.getFilename());
        try {
            Document doc = new Document();

            Field pathField = new StringField(Constants.FIELD_PATH, file.toString(), Field.Store.YES);
            doc.add(pathField);

            // Add Document metadata //
            doc.add(new StringField(Constants.FIELD_AUTHOR, metadata.getAuthor(), Field.Store.YES));
            doc.add(new StringField(Constants.FIELD_TITLE, metadata.getTitle(), Field.Store.YES));
            doc.add(new StringField(Constants.FIELD_CONFERENCE, metadata.getConference(), Field.Store.YES));
            // End of Document Metadata //

            Field modified = new LongField(Constants.FIELD_MODIFIED,
                    Files.getLastModifiedTime(file).toMillis(), Field.Store.YES);
            doc.add(modified);


            PDFTextExtractor extractor = new PDFTextExtractor();
            // Get the string contents
            String textContents = extractor.extractText(file.toString());

            // Store the string contents
            FieldType contentsType = new FieldType();
            contentsType.setStored(true);
            contentsType.setTokenized(true);
            contentsType.setStoreTermVectors(true);
            contentsType.setStoreTermVectorPositions(true);
            contentsType.setStoreTermVectorPayloads(true);
            contentsType.setStoreTermVectorOffsets(true);
            contentsType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
            Field contents = new Field(Constants.FIELD_CONTENTS, textContents, contentsType);
            doc.add(contents);

            if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE) {
                // New index, so we just add the document (no old document can be there):
                log.info("adding "  + file + " to index");
                writer.addDocument(doc);
            } else {
                // Existing index (an old copy of this document may have been indexed) so
                // we use updateDocument instead to replace the old one matching the exact
                // path, if present:
                log.info("updating " + file + " in index");
                writer.updateDocument(new Term(Constants.FIELD_PATH, file.toString()), doc);
            }
        } catch (IOException e) {
            log.error("Failed to read file " + metadata.getFilename());
        }

    }

    /**
     * Updates the index
     *
     * @throws IOException
     */
    public void updateIndex() throws IOException {
        try {
            long startTime = System.nanoTime();
            // Get the index directory
            Directory dir = FSDirectory.open(Paths.get(indexDirectory));
            // Get the directory for resources
            String resourcesDir = resourceDirectory + "/" + Constants.CSV_LOCATION;
            // Get PDF Analyzer
            Analyzer pdf_analyzer = new PDFAnalyzer(resourceDirectory + "/" + Constants.STOPWORDS_FILE);
            // Create an index writer config with the analyzer
            IndexWriterConfig iwc = new IndexWriterConfig(pdf_analyzer);
            // Set the open mode to create or append
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // Set index to be created
            // Create an index writer
            IndexWriter writer = new IndexWriter(dir, iwc);
            // Index the documents
            indexDocs(writer, resourcesDir);
            writer.close();
            long endTime = System.nanoTime();
            LuceneIndexReader.getInstance().initializeIndexReader(writer);
            log.info("Took: " + (endTime - startTime) / Math.pow(10, 6) + " milliseconds to generate the index.");
        } catch (IOException e) {
            log.error("IO Exception Thrown while updating index " + e.getMessage() + "\n");
        }

    }

}
