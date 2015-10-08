package servlets;

import com.google.gson.GsonBuilder;
import common.DocumentMetadata;
import searcher.MetadataRetriever;
import searcher.exception.LuceneSearchException;
import searcher.reader.LuceneIndexReader;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Created by chris on 10/6/15.
 */
@WebServlet(value = "/metadata", name = "metadataServlet")
public class MetadataServlet extends GenericServlet {
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        int docId = Integer.parseInt(req.getParameter("docId"));
        MetadataRetriever retriever = null;
        try {
            retriever = new MetadataRetriever(LuceneIndexReader.getInstance());
        } catch (LuceneSearchException e) {
            e.printStackTrace();
        }

        try{
            DocumentMetadata metadata = retriever.getMetadata(docId);
            res.getWriter().println((new GsonBuilder()).create().toJson(metadata));
        } catch (LuceneSearchException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.err.println("MetadataServlet: Metadata retriever could not be created");
        }

    }
}
