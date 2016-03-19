package servlets.endpoint_servlets;

import data_processing.data.D3ConvertibleJson;
import data_processing.related_terms_combiner.CombinedRelatedTerms;
import data_processing.related_terms_combiner.CombinedRelatedTermsConverter;
import data_processing.related_terms_combiner.alt.AlternateRelatedTermsConverter;
import data_processing.related_terms_combiner.alt.data.RootNode;
import data_processing.related_terms_combiner.data.RelatedTermResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import servlets.servlet_util.RequestUtils;
import servlets.servlet_util.ResponseUtils;
import servlets.servlet_util.ServletConstant;

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
    private static final Log log = LogFactory.getLog(SecondTierServlet.class);
    private CombinedRelatedTerms crt;
    public SecondTierServlet() {
        super();
        crt = new CombinedRelatedTerms();
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res){

        // We have the option of using the previous or not based on request
        List<String> queries = RequestUtils.getQueries(req);
        if(queries.size() == 0){
            log.error("Could not get queries.");
            return;
        }

        int docId;
        if(req.getParameterMap().containsKey(ServletConstant.DOC_ID)){
            docId = RequestUtils.getIntegerParameter(req, ServletConstant.DOC_ID);
        }else{
            log.error("Could not get queries");
            return;
        }

        long startTime = System.nanoTime();
        List<RelatedTermResult> results = new ArrayList<>();
        queries.stream()
                .map(q -> RelatedTermResult.createResult(crt, q, docId))
                .forEach(results::add);

        RelatedTermResult[] resArray = new RelatedTermResult[results.size()];

        if(req.getParameterMap().containsKey("exp")){
            RootNode result = AlternateRelatedTermsConverter.convertToD3(results.toArray(resArray));
            ResponseUtils.printJsonResponse(res, result);
        }else{
            D3ConvertibleJson json = CombinedRelatedTermsConverter
                    .convertToLinksAndNodes(results.toArray(resArray));
            ResponseUtils.printPrettyJsonResponse(res, json);
        }
        log.info("Total time to produce second tier results: " + (System.nanoTime() - startTime)/Math.pow(10, 9));
    }
}
