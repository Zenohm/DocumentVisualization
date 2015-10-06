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

package common;

import org.apache.commons.csv.CSVRecord;

/**
 * Created by chris on 9/18/15.
 */
public class DocumentMetadata {
    private String filename;
    private String title;
    private String author;
    private String conference;

    public DocumentMetadata(CSVRecord record) {
        filename = record.get("file");
        title = record.get("title");
        author = record.get("author");
        conference = record.get("conference");
    }

    public DocumentMetadata(String filename,
                            String title,
                            String author,
                            String conference) {
        this.filename = filename;
        this.title = title;
        this.author = author;
        this.conference = conference;
    }

    @Override
    public String toString() {
        String temp = "";
        temp += "Title: " + title;
        temp += "Author: " + author;
        temp += "Conference: " + conference;
        temp += "File Location: " + filename;
        return temp;
    }

    public String getFilename() {
        return filename;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getConference() {
        return conference;
    }
}
