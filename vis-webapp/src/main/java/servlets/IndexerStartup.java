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

import common.Constants;
import api.indexer.PDFIndexer;
import api.indexer.TextTokenizerWarmer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import api.reader.LuceneIndexReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Semaphore;


/**
 * Starts up the servlet. Initializes logging and completes indexing if needed.
 * Created by Chris on 8/19/2015.
 */
public class IndexerStartup extends HttpServlet {
    private static final Log log = LogFactory.getLog(IndexerStartup.class);
    public static Semaphore lock = new Semaphore(0);

    /**
     * Initialization Service, starts up the server
     *
     * @throws ServletException
     */
    public void init() throws ServletException {
        System.out.println("Starting Up the Logging System");
        // TODO: Better configuration for loggers
        org.apache.log4j.BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.INFO);
        System.out.println("Disabling PDF Box Logging");
        String[] annoyingLoggers = {"org.apache.pdfbox.pdmodel.font.PDType0Font",
                "org.apache.pdfbox.pdmodel.font.PDType1Font",
                "org.apache.pdfbox.pdmodel.font.PDSimpleFont",
                "org.apache.pdfbox.pdmodel.font.PDCIDFontType0",
                "org.apache.pdfbox.io.ScratchFileBuffer",
                "org.apache.pdfbox.pdfparser.COSParser",
                "org.apache.pdfbox.pdmodel.font.PDTrueTypeFont",
                "org.apache.pdfbox.pdfparser.PDFObjectStreamParser",
                "org.apache.pdfbox.pdmodel.font.PDFont",
                "org.apache.pdfbox.pdmodel.font.FileSystemFontProvider",
                "org.apache.fontbox.ttf.CmapSubtable",
                "org.apache.pdfbox.pdmodel.font.FileSystemFontProvider",
                "org.apache.pdfbox.pdmodel.font.encoding.GlyphList",
                "org.apache.pdfbox.text.TextPosition"};
        for (String annoyingLogger : annoyingLoggers) {
            org.apache.log4j.Logger logPdfEngine = org.apache.log4j.Logger.getLogger(annoyingLogger);
            logPdfEngine.setLevel(Level.FATAL);
        }

        log.info("Running Initial Indexing Operation...");
        if (System.getenv(Constants.RESOURCE_FOLDER_VAR) == null) {
            log.fatal("RESOURCE Environment variable was not set.");
            return;
        }

        // Create the indexer
        // indexer output is INDEX_DIRECTORY, local to the application
        // indexer input is RESOURCE_FOLDER_VAR which can be anywhere on the system and is specified by
        // an environment variable
        PDFIndexer indexer = new PDFIndexer(getServletContext().getRealPath(Constants.INDEX_DIRECTORY),
                System.getenv(Constants.RESOURCE_FOLDER_VAR));

        // Try to update the index
        // TODO: Make this configuration less ugly with a class of its own
        boolean updateIndex = true;
        try {

            Properties props = new Properties();
            String propFilename = System.getenv(Constants.RESOURCE_FOLDER_VAR)
                    + "/" + Constants.INDEX_CONFIG_FILE;
            InputStream in = new FileInputStream(propFilename);
            props.load(in);
            in.close();
            if (props.getProperty("INDEX_DOCS").equals("false")) {
                updateIndex = false;
            }
        } catch (IOException e) {
            log.warn("Could not find configuration file. Defaulting to update index.\n" +
                    "Configuration file should be placed in RESOURCE_DIR/config/index-config.cfg");
        }

        if (updateIndex) {
            try {
                indexer.updateIndex();
            } catch (IOException e) {
                log.error("Updating index error", e);
            }
        } else {
            log.info("Skipping Re-indexing.");
        }


        // Complete Updating the index
        log.info("Indexing Complete");
        lock.release();
        if(!LuceneIndexReader.getInstance().isInitialized()){
            log.info("Initializing IndexReader from directory.");
            LuceneIndexReader.getInstance().initializeIndexReader(getServletContext().getRealPath(Constants.INDEX_DIRECTORY));
        }
        log.info("Warming the Text Tokenizer");
        TextTokenizerWarmer.tokenizeAllText();
    }
}
