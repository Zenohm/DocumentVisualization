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
 * Contains some basic PLoS functions, which are implemented here to eliminate dependence on a library and
 * maintain cross-browser compatibility
 */
var PLoS = {

    /**
     * Add an event listener to a dom element
     * @param evnt - the type of event to listen for (e.g. 'click')
     * @param el - the dom element to listen on
     * @param func - the callback function
     */
    eventListen: function (evnt, el, func) {  // listens for events on specified elements and triggers specified function
        if (!func) return;
        if (el.addEventListener) {  // W3C Model
            el.addEventListener(evnt, func, false);
        } else if (el.attachEvent) { // IE Model
            r = el.attachEvent("on" + evnt, func);
            return r;
        } else {
            _resultsClickListener
            console.log("unable to listen to events");
        }
    },

    /**
     * Add a callback function for when the DOM is loaded
     * @param callback - the function to call on dom load
     */
    domReady: function (callback) {
        var oldonload = window.onload;
        if (typeof window.onload != 'function') {
            window.onload = callback;
        } else {
            window.onload = function () {
                if (oldonload) {
                    oldonload();
                }
                callback();
            }
        }
    },

    /**
     * Clear all content from an element
     * @param el - the dom element to clear
     */
    clearEl: function (el) {
        el.innerHTML = '';
    },

    /**
     * Shows an error notification in the specified div
     * @param div - the id of the dom element to use as container for the mesage
     * @param text - html of the error notification to show
     */
    error: function (div, text) {
        var el = document.getElementById(div);
        var errorDiv = document.createElement('div');
        errorDiv.innerHTML = text;
        PLoS.clearEl(el);
        el.appendChild(errorDiv);
        el.style.display = 'block';
    },

    formatMo: function (value) {
        switch (value) {
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            case 12:
                return "December";
            default:
                return value;
        }
    },

    /**
     * Expand a hidden element and toggle the event listener of the button doing the expanding.
     *
     *
     * @param hidden - Id of div containing hidden content to expand
     * @param toggleId - Id of the dom element that was clicked to expand the content
     */
    expand: function (hidden, toggleId) {
        var el = document.getElementById(toggleId);
        //Toggle event listening
        el.onclick = null;
        el.innerHTML = "[-]";
        PLoS.eventListen('click', el, function () {
            PLoS.hide(hidden, toggleId);
        });
        document.getElementById(hidden).style.display = "block";
        return false;
    },
    /**
     * Hide a hidden element and toggle the event listener of the button doing the hiding.
     * @param shown - Id of the div containing content to hide
     * @param toggleId - Id of the dom element that was clicked to hide the content
     */
    hide: function (shown, toggleId) {
        var el = document.getElementById(toggleId);
        //Toggle event listening
        el.onclick = null;
        el.innerHTML = "[+]";
        PLoS.eventListen('click', el, function () {
            PLoS.expand(shown, toggleId);
        });
        document.getElementById(shown).style.display = "none";
        return false;
    },

    /**
     * Toggle display of an article abstract, relying on element ids
     * @param doi - the doi of the article
     */
    toggleAbstract: function (doi) {
        var button = document.getElementById('toggle-abstract-' + doi);
        var truncated = document.getElementById('truncated-' + doi);
        var abstract = document.getElementById('abstract-' + doi);
        if (button.innerHTML == '[+]') {
            truncated.style.display = 'none';
            abstract.style.display = 'block';
            button.innerHTML = '[-]';
        } else if (button.innerHTML == '[-]') {
            truncated.style.display = 'block';
            abstract.style.display = 'none';
            button.innerHTML = '[+]';
        }
        return false;
    },

    //Used for managing global jsonp callbacks
    _jsonpCallbacks: []
};
