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
 * Created by Chris on 4/9/2016.
 * An attempt to consolidate commonly reused functions between pages
 */

/**
 * *******************************************************************
 * Key bindings
 * *******************************************************************
 */

/**
 *  Ready up key bindings for search submissions
 */
$(document).ready(function() {
    $('#limit').keyup(function(e) {
        if (e.keyCode == 13) {
            doSearch();
        }
    });
});

/**
 * *******************************************************************
 * Dynamic Query Manipulation
 * *******************************************************************
 */

/**
 * Adds a box for another query
 */
function addInput() {
    if(queryElements.length >= 9) return;
    var input = document.createElement("input");
    input.type = "text";
    input.className = "query";
    input.id = "query_text" + nextQueryId;
    document.getElementById("queries").appendChild(input);
    $("#"+input.id).keyup(function (event) {
        if (event.keyCode == 13) {
            searchCallback();
        }
    });
    queryElements.push(input);
    nextQueryId++;
}

/**
 * Removes a box for a query
 */
function removeInput(){
    if(queryElements.length <= 2) return;
    var element = queryElements.pop();
    document.getElementById("queries").removeChild(element);
    nextQueryId--;
    localStorage.removeItem("query" + nextQueryId);
}


/**
 * *******************************************************************
 * Doc Limit functions
 * *******************************************************************
 */

/**
 * Update the textbox for the doc_limit when the slider is modified
 * @param limit
 */
function outputUpdate(limit) {
    document.querySelector('#limit').value = limit;
}

/**
 * Update the slider when the textbox for the doc_limit is modified
 * @param limit
 */
function updateSlider(limit) {
    console.log(document.getElementById('doc_limit').value);
    document.querySelector('#doc_limit').value = limit;
}