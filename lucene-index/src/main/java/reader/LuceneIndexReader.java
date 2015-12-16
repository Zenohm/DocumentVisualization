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

package reader;

import common.Constants;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.FileSystems;

/**
 * Specific implementation of index reader for Lucene.
 * SINGLETON that stores the version of the reader from Lucene
 * TODO: Abstract the methods and things that are specific to lucene so we aren't locked into it.
 * Created by Chris on 9/24/2015.
 */
public class LuceneIndexReader implements IndexReader {
    private static LuceneIndexReader ourInstance = new LuceneIndexReader();
    private org.apache.lucene.index.IndexReader READER;

    private LuceneIndexReader() {
        READER = null;
    }

    public static LuceneIndexReader getInstance() {
        return ourInstance;
    }

    /**
     * @return True if the index is initialized
     */
    @Override
    public boolean isInitialized() {
        return READER != null;
    }

    /**
     * This is called as a part of the startup for the indexer.
     * When running an application that is on the web application, the reader is started by the startup script.
     * @param filename The index directory
     * @return if the index initialization succeeds return true.
     */
    @Override
    public boolean initializeIndexReader(String filename) {
        try {
            READER = DirectoryReader.open(FSDirectory.open(FileSystems.getDefault().getPath(filename)));
        } catch (IOException e) {
            e.printStackTrace(); // TODO: Remove stacktrace print from here
            READER = null;
            System.err.println("Failed to instantiate the index reader with directory: " + filename);
        }
        return isInitialized();
    }

    /**
     * Default initializer for the index reader. Initializes the index to the default index directory.
     * @return if the initialization is successful, returns true
     */
    public boolean initializeIndexReader() {
        return initializeIndexReader(Constants.INDEX_DIRECTORY);
    }

    @Override
    public org.apache.lucene.index.IndexReader getReader() {
        if (READER == null)
            System.err.println("Error, Indexer Not Initialized! Results will be invalid."); // TODO: Throw an error here
        return READER;
    }
}
