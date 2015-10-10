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

package servlets;

import indexer.PDFIndexer;
import searcher.exception.LuceneSearchException;
import searcher.reader.LuceneIndexReader;
import util.IndexerConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.util.concurrent.Semaphore;

/**
 * Created by Chris on 8/19/2015.
 */
public class IndexerStartup extends HttpServlet {
    public static Semaphore lock = new Semaphore(0);
    public void init() throws ServletException {
        System.out.println("-----------------");
        System.out.println("Running Initial Indexing Operation...");
        if (System.getenv(IndexerConstants.RESOURCE_FOLDER_VAR) == null) {
            System.err.println("CRITICAL: Indexer: RESOURCE Environment variable was not set.");
            return;
        }

        // Create the indexer
        // indexer output is INDEX_DIRECTORY, local to the application
        // indexer input is RESOURCE_FOLDER_VAR which can be anywhere on the system and is specified by
        // an environment variable
        PDFIndexer indexer = new PDFIndexer(getServletContext().getRealPath(IndexerConstants.INDEX_DIRECTORY),
                System.getenv(IndexerConstants.RESOURCE_FOLDER_VAR));

        // Try to update the index
        // TODO: Configuration option to NOT update the index
        try {
            indexer.updateIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Complete Updating the index
        System.out.println("DONE!");
        System.out.println("-----------------");
        lock.release();
        LuceneIndexReader.getInstance()
                .initializeIndexReader(getServletContext().getRealPath(IndexerConstants.INDEX_DIRECTORY));
    }
}
