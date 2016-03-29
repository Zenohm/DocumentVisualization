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

/**
 * Created by Chris on 3/24/2016.
 * This is an attempt at a complete rewrite of the code from the bottom up, so that it is more reusable and works
 * well with more dynamic data.
 */

/**
 * Prepares the local storage for a search.
 */
function loadFieldsIntoLocalStorage() {
    var i = 1;
    while(null != document.getElementById("query_text"+i)){
        localStorage.setItem("query"+i, document.getElementById("query_text"+i).value);
        i++;
    }
}

/**
 * Pulls the required fields out of storage and into a string that is returned.
 */
function getQueryString() {
    var query_string = "multi_term_search?vis";
    var i = 1;
    while (null != localStorage.getItem("query" + i)) {
        var query = localStorage.getItem("query" + i);
        query_string += "&query" + i + "=" + query;
        i++;
    }
    return query_string;
}