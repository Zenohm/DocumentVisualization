package servlets;

import data_processing.related_terms_combiner.CombinedRelatedTerms;
import data_processing.related_terms_combiner.CombinedRelatedTermsConverter;
import data_processing.related_terms_combiner.data.RelatedTermResult;
import searcher.exception.LuceneSearchException;
import servlets.servlet_util.RequestUtils;
import servlets.servlet_util.ResponseUtils;
import servlets.servlet_util.ServletConstant;
import util.data.D3ConvertibleJson;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chris on 12/31/15.
 */
@WebServlet(value = "/second_tier", name = "SecondTier")
public class SecondTierServlet extends HttpServlet {

    private CombinedRelatedTerms crt;
    public SecondTierServlet(){
        super();
        crt = new CombinedRelatedTerms();
    }

    public static final String USE_PREVIOUS = "prev";

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res){

        // We have the option of using the previous or not based on request
        List<String> queries;
        if(req.getParameterMap().containsKey(USE_PREVIOUS)){
            Object obj = req.getSession().getAttribute("queries");
            if(obj != null){
                queries = (List<String>)obj;
            }else{
                System.err.println("ERROR: Requested to use previous with no previous queries");
                return;
            }
        }else{
            queries = RequestUtils.getQueries(req);
        }
        if(queries.size() == 0){
            System.err.println("ERROR: Could not get queries.");
            return;
        }

        int docId;
        if(req.getParameterMap().containsKey(ServletConstant.DOC_ID)){
            docId = RequestUtils.getIntegerParameter(req, ServletConstant.DOC_ID);
        }else{
            System.err.println("ERROR: Could not get queries");
            return;
        }

        long startTime = System.nanoTime();
        List<RelatedTermResult> results = new ArrayList<>();
        queries.parallelStream()
                .map(q -> RelatedTermResult.createResult(crt, q, docId))
                .forEach(results::add);

        RelatedTermResult[] resArray = new RelatedTermResult[results.size()];
        D3ConvertibleJson json = CombinedRelatedTermsConverter
                .convertToLinksAndNodes(results.toArray(resArray));
        ResponseUtils.printPrettyJsonResponse(res, json);
        System.out.println("Total time to produce combined related term results: " + (System.nanoTime() - startTime)/Math.pow(10, 9));
    }
}
