package servlets;

import access_utils.MetadataRetriever;
import com.google.gson.GsonBuilder;
import common.data.DocumentMetadata;
import reader.LuceneIndexReader;
import searcher.exception.LuceneSearchException;
import util.JsonCreator;

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
        int docId = Integer.parseInt(req.getParameter("docId"));
        MetadataRetriever retriever = null;
        try {
            retriever = new MetadataRetriever(LuceneIndexReader.getInstance());
        } catch (LuceneSearchException e) {
            e.printStackTrace();
        }

        try {
            DocumentMetadata metadata = retriever.getMetadata(docId);
            res.getWriter().println(JsonCreator.toJson(metadata));
        } catch (LuceneSearchException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("MetadataServlet: Metadata retriever could not be created");
        }

    }
}
