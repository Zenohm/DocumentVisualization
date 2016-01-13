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

import common.data.ScoredTerm;
import org.apache.commons.lang.StringUtils;
import servlets.servlet_util.ResponseUtils;
import synonyms.SynonymAdapter;
import servlets.servlet_util.JsonCreator;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Servlet that processes a 'term' and returns a set of synonyms.
 */
@WebServlet(value = "/synonyms", name = "SynonymsServlet")
public class SynonymsServlet extends GenericServlet {
    /**
     * Service for the Synonyms servlet
     *
     * @param req Required Parameters:
     *            term: The term to find synonyms for
     *            Optional Parameters:
     *            scored: The terms will have scores attached
     *            related: Terms will be minimally related (zeroes will be filtered)
     * @param res Response will contain a list of synonyms or scored synonyms
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {

        String term = StringUtils.trim(req.getParameter("term"));
        Map<String, String[]> params = req.getParameterMap();

        String response;
        if (!params.containsKey("scored")) {
            // If you just want unscored synonyms.
            Set synonyms = SynonymAdapter.getSynonyms(term);
            response = JsonCreator.toJson(synonyms);
        } else {
            // If you want minimally related terms
            if (params.containsKey("related")) {
                List<ScoredTerm> synonyms = SynonymAdapter.getScoredSynonymsWithMinimalRelation(term);
                response = JsonCreator.toJson(synonyms);
            } else {
                // If you want to include everything with the scores
                List<ScoredTerm> synonyms = SynonymAdapter.getScoredSynonymsWithUnrelatedIncluded(term);
                response = JsonCreator.toJson(synonyms);
            }
        }

        ResponseUtils.printResponse(res, response);
    }

}