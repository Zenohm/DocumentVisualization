package servlets.test_servlets;

import api.exception.LuceneSearchException;
import api.reader.LuceneIndexReader;
import api.term_search.CompoundRelatedTerms;
import common.Constants;
import common.data.ScoredTerm;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import servlets.servlet_util.JsonCreator;
import servlets.servlet_util.ResponseUtils;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.List;

/**
 * Created by chris on 12/16/15.
 */
@WebServlet(value = "/compound_related_terms", name = "CompoundRelatedTermsServlet")
public class CompoundRelatedTermsServlet extends GenericServlet{
    private static final Log log = LogFactory.getLog(CommonTermsServlet.class);
    private CompoundRelatedTerms termsGenerator;
    public CompoundRelatedTermsServlet(){
        super();
        String resourceDirectory = System.getProperty(Constants.RESOURCE_FOLDER_VAR);
        String stopwordsFile = resourceDirectory + "/" + Constants.STOPWORDS_FILE;
        try{
            termsGenerator = new CompoundRelatedTerms(LuceneIndexReader.getInstance(), stopwordsFile);
        }catch (LuceneSearchException e){
            log.error("Could not instantiate compound related terms generator.");
        }

    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        if(termsGenerator == null){
            throw new ServletException("CompoundTermsGenerator not instantiated");
        }
        try{
            long start = System.nanoTime();
            String term = req.getParameter("term");
            List<ScoredTerm> terms = termsGenerator.getRelatedTerms(term);
            log.info("Took: " + (System.nanoTime() - start)/Math.pow(10, 9) +
                    " seconds to generate related terms");

            String response = JsonCreator.getPrettyJson(terms);
            ResponseUtils.printResponse(res, response);

        }catch (LuceneSearchException e){
            log.error("Exception thrown while getting compound terms", e);
            res.getWriter().println("<h1>ERROR</h1>" + e.toString()); // TODO: Better logging and debugging
        }
    }
}
