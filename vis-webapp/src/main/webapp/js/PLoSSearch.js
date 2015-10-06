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

/*
 Adopted by Chris Bellis to fix non-standards compliant code and make the whole thing WAY easier to understand.
 As well as customize for my own needs.
 Modified: July, 17, 2015
 */

if (typeof PLoS == "undefined") {
    PLoS = {};
}

/**
 * Create a search widget around given ids for dom objects. The returned object has a method, activate(), which
 * should be called on DOM load
 *
 *
 * Arguments should take the following form:
 * <pre>
 * {
 *  resultsDiv : id of the div in which to display results (REQUIRED)
 *  input : id of the input box that should be used for the search (REQUIRED)
 *  button : id of the button that should trigger a search (REQUIRED)
 *  field: SOLR field to use in search, e.g. author, title (OPTIONAL)
 *  limit : the number of results to display. (OPTIONAL)
 * }
 * </pre>
 *
 * So a proper call would be like :
 * <pre>
 * var searchWidget = new PLoS.searchWidget({
 *  resultsDiv : 'results-id',
 *  input : 'input-id',
 *  button : 'search-button-id'
 * });
 * var authorSearchWidget = new PLoS.searchWidget({
 *  resultsDiv : 'author-results-id',
 *  input : 'author-input-id',
 *  button : 'author-search-button-id',
 *  field : 'author'
 * });
 *
 * PLoS.domReady(function(){
 *  searchWidget.activate();
 *  authorSearchWidget.activate();
 * });
 * </pre>
 * @param args - an object literal as described above
 * @requires PLoSLib.js
 *
 */
PLoS.searchWidget = function (args) {

    var that = this;
    var BASE_URL = "./";

    var searchUrl = "http://api.plos.org/search";

    this.numResults = args.limit ? args.limit : 30;
    this.hasDisplayedPLoSFormResults = false; //lets us know when the results have been displayed
    /**
     * Activate the widget.  Should be called only after DOM loads
     */
    this.activate = function () {
        that.id = PLoS._jsonpCallbacks.length;
        PLoS._jsonpCallbacks.push(function (data) {
            that._jsonpCallback(data);
        });
        PLoS.eventListen('click', document.getElementById(args.button),
            function (e) {
                that.getResults(e);
            });
        PLoS.eventListen('keyPress', document.getElementById(args.input),
            function (e) {
                if (e.keyCode == 13) {
                    that.getResults(e);
                }
            });
    };

    /**
     * Sends the search string from args.input to solr
     * @param e - the event that triggered the search
     */
    this.getResults = function (e) {
        if (!e) {
            e = window.event;
        }

        if (e.preventDefault) {
            e.preventDefault();
        } //prevents form submit and page refresh
        e.returnValue = false;

        var el = document.getElementById(args.resultsDiv);
        PLoS.clearEl(el);

        //append the 'loading' image
        var loaderDiv = document.createElement('div');
        loaderDiv.id = "plos-loader";
        loaderDiv.className = "loader";
        loaderDiv.innerHTML = '<img src="' + BASE_URL + 'ajax-loader.gif" alt="loading" title="loading" style="width: 25%; height 25%"/>';
        loaderDiv.innerHTML += "Loading";
        el.appendChild(loaderDiv);

        //Format the query string to send to solr
        var query = document.getElementById(args.input).value;
        if (!query || query === '') {
            if (args.field && args.field == 'author') {
                query = 'Michael B Eisen';
            } else {
                query = "*:*"
            }
        }
        var theQuery = 'q=' + (args.field ? args.field + ':"' + query + '"' : query)
        var params = [
            'json.wrf=PLoS._jsonpCallbacks[' + that.id + ']', //name of our callback function
            'wt=json', //return type
            'start=0',
            'rows=' + that.numResults, //number of results to return
            'fl=id,title_display,counter_total_month,author_display,abstract,journal,article_type,publication_date', //fields we want from solr
            'fq=!article_type_facet:"Issue Image" AND doc_type:full', //only return full articles
            'sort=counter_total_month desc', //sort by articles with the most views
            'api_key=hVdohkgLubKLmo4K8oPK' // TODO: We should store the API key in a more secure location...
        ];
        params = params.concat(theQuery);
        var strData = params.join('&');

        //Create the script tag to insert into the dom
        var script = document.createElement('script');
        script.type = "text/javascript";
        if (query) {
            script.src = searchUrl + '?' + strData; // setting the search url and query string
            document.getElementById(args.resultsDiv).appendChild(script); // inserting the results
            window.setTimeout(function () {
                if (!that.hasDisplayedPLoSFormResults) {
                    PLoS.error('<span id="PLoS-Form-Error">Search Timed Out</span>');
                }
            }, 5000);
        }
    };

    /**
     * Function to call on successful server response
     * @param data - the formatted html data from the jsonp callback
     */
    this.serverResponse = function (data) {
        var el = document.getElementById(args.resultsDiv);
        //var resultsDiv = document.createElement("div");
        //resultsDiv.innerHTML = data;
        var resultsDiv = data; // TODO: This is the replacement eventually when we get generating the DOM sorted out
        PLoS.clearEl(el);
        el.appendChild(resultsDiv);
        el.style.display = 'block';
    };

    /**
     * Function to call on server error
     * @param data - formatted html of an error response to display
     */
    this.serverError = function (data) {
        var el = document.getElementById(args.resultsDiv);
        PLoS.clearEl(el);
        PLoS.error(args.resultsDiv, data.toString());
    };

    /**
     * The function to use as callback from our jsonp request.  Generates display content.
     * @param data - the json response from SOLR
     */
        // TODO: I am going to refactor this to take a different approach
    this._jsonpCallback = function (data) {
        that.hasDisplayedPLoSFormResults = true;
        var resultsData = data.response.docs;
        var resultsLength = resultsData.length;
        var results = '';
        var resultsString = ' Result';
        var totalResults = '';
        var theInput = document.getElementById(args.input);
        var resultsDiv = document.createElement('div'); // TODO: Create a div and build the DOM the way that we should build it.
        var theQuery = theInput.value;
        var topString = "Top ";
        var i, h, j;

        if (args.field && args.field === "author" && theQuery === "") {
            theQuery = "Michael B Eisen";
        }

        var queryString = encodeURI((args.field ? args.field + ':"' + theQuery + '"' : theQuery));


        if (resultsLength > 1) {
            resultsString = ' Results';
        }

        if (resultsLength == data.response.numFound) {
            topString = "";
        }

        if (data.response.numFound > resultsLength) {
            totalResults = data.response.numFound;
            resultsString = ' of ' + totalResults + ' Results';
        }

        var plosSearchWidget = document.createElement("div");
        plosSearchWidget.className = "PLoS-Search-Widget";
        resultsDiv.appendChild(plosSearchWidget);

        if (resultsLength > 0) {

            // Append a div for the top level stuff
            var resultsTop = document.createElement("div");
            plosSearchWidget.appendChild(resultsTop);
            var numResults = document.createElement("h3");
            resultsTop.appendChild(numResults);
            numResults.appendChild(document.createTextNode(topString + resultsLength + resultsString));

            var searchResults = document.createElement("div");
            searchResults.className = "PLoSSearchResults";
            searchResults.id = "search-results-" + that.id;

            //iterate through the results documents
            for (i = 0; i < resultsData.length; i++) {

                var result = resultsData[i];
                var author = result.author_display;
                var pubDate = result.publication_date;

                if (author) {

                    var authorLength = author.length;
                    var authors = '';

                    // if there are multiple authors, format author string
                    if (Object.prototype.toString.call(author) === '[object Array]' && authorLength > 1) {
                        for (j = 0; j < authorLength; j++) {
                            authors += author[j];
                            if (j < authorLength - 1) {
                                authors += ', ';
                            }
                        }
                        author = authors;
                    }
                } else {
                    author = "";
                }

                //format the pub date display
                if (pubDate) {
                    pubDate = pubDate.substr(0, pubDate.indexOf("T"));
                    pubDate = pubDate.split("-");
                    var year = pubDate[0];
                    var month = pubDate[1];
                    if (month.indexOf('0') === 0) {
                        month = month.substr(1, 1);
                    }

                    month = PLoS.formatMo(parseInt(month, 10));

                    var day = pubDate[2];

                    pubDate = month + " " + " " + day + " " + year;

                }

                var resultDiv = document.createElement("div");
                resultDiv.className = "box";
                resultDiv.id = result.id;
                resultsTop.appendChild(resultDiv);

                // Do the article link
                var articleLink = document.createElement('a');
                resultDiv.appendChild(articleLink);
                articleLink.setAttribute('class', "articleLink"); // TODO: Create styles for this
                articleLink.setAttribute('title', result.title_display);
                articleLink.setAttribute('href', "http://dx.plos.org/" + result.id);
                articleLink.setAttribute('target', "_blank");
                articleLink.appendChild(document.createTextNode(result.title_display));

                var authorsP = document.createElement('p');
                authorsP.className = "author"; // TODO: Create styles for this
                authorsP.appendChild(document.createTextNode(author));
                resultDiv.appendChild(authorsP);

                var journalInfo = document.createElement('p');
                journalInfo.className = "journal"; // TODO: Create style for this
                journalInfo.appendChild(document.createTextNode(result.journal + " : " + result.article_type + ", published " + pubDate));
                resultDiv.appendChild(journalInfo);
            }

            // TODO: Add a next page option?

            that.serverResponse(resultsDiv);
        } else {
            var noResults = document.createElement("p");
            noResults.appendChild(document.createTextNode("No Results"));
            plosSearchWidget.appendChild(noResults);
            that.serverError(resultsDiv);
        }
    };
};
