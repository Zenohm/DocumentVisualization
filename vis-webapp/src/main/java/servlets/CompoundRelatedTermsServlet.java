package servlets;

import com.google.gson.GsonBuilder;
import common.Constants;
import common.data.ScoredTerm;
import full_text_analysis.CompoundRelatedTerms;
import reader.LuceneIndexReader;
import searcher.exception.LuceneSearchException;

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
    private CompoundRelatedTerms termsGenerator;
    public CompoundRelatedTermsServlet(){
        super();
        String resourceDirectory = System.getenv(Constants.RESOURCE_FOLDER_VAR);
        String stopwordsFile = resourceDirectory + "/" + Constants.STOPWORDS_FILE;
        try{
            termsGenerator = new CompoundRelatedTerms(LuceneIndexReader.getInstance(), stopwordsFile);
        }catch (LuceneSearchException e){
            System.err.println("Error instantiating the compound terms generator");
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
            List<ScoredTerm> terms = termsGenerator.getCompoundRelatedTerms(term);
            System.out.println("Took: " + (System.nanoTime() - start)/Math.pow(10, 9) +
                    " seconds to generate related terms");

            res.getWriter().println((new GsonBuilder().setPrettyPrinting()).create().toJson(terms));
        }catch (LuceneSearchException e){
            e.printStackTrace();
            res.getWriter().println("<h1>ERROR</h1>" + e.toString()); // TODO: Better logging and debugging
        }
    }
}
