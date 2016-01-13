package servlets.endpoint_servlets;

import api.document_access.MetadataRetriever;
import common.data.DocumentMetadata;
import api.reader.LuceneIndexReader;
import api.exception.LuceneSearchException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import servlets.servlet_util.RequestUtils;
import servlets.servlet_util.ResponseUtils;
import servlets.servlet_util.ServletConstant;
import servlets.servlet_util.JsonCreator;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

/**
 * Obtains the metadata for a specific document.
 * Created by chris on 10/6/15.
 */
@WebServlet(value = "/metadata", name = "metadataServlet")
public class MetadataServlet extends GenericServlet {
    public static final Log log = LogFactory.getLog(MetadataServlet.class);
    /**
     * Servlet Service for getting Metadata
     *
     * @param req Required parameters:
     *            docId: The document ID to get the metadata for
     * @param res Response contains JSON representation of a DocumentMetadata object.
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        int docId = RequestUtils.getIntegerParameter(req, ServletConstant.DOC_ID);
        MetadataRetriever retriever;
        try {
            retriever = new MetadataRetriever(LuceneIndexReader.getInstance());
        } catch (LuceneSearchException e) {
            log.error("Metadata retriever could not be created");
            return;
        }

        try {
            DocumentMetadata metadata = retriever.getMetadata(docId);
            String response = JsonCreator.toJson(metadata);
            ResponseUtils.printResponse(res, response);
        } catch (LuceneSearchException e) {
            log.error("Error searching");
        }
    }
}
