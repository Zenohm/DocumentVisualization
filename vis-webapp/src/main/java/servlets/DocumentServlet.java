package servlets;

import org.apache.commons.lang.ObjectUtils;
import searcher.MetadataRetriever;
import searcher.PDFRetriever;
import searcher.exception.LuceneSearchException;
import searcher.reader.LuceneIndexReader;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;

/**
 * Created by Chris on 10/8/2015.
 */
@WebServlet(value = "/docs", name = "docServlet")
public class DocumentServlet extends GenericServlet {
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        int docId = Integer.parseInt(req.getParameter("docId"));
        PDFRetriever retriever = null;
        try {
            retriever = new PDFRetriever(LuceneIndexReader.getInstance());
        } catch(LuceneSearchException e){
            e.printStackTrace();
        }

        try{
            res.setContentType("application/pdf");
            File document = retriever.getPDFFile(docId);
            FileUtils.copyFile(document, res.getOutputStream());
        } catch (LuceneSearchException |
                 NullPointerException e) {
            e.printStackTrace();
        }
    }
}
