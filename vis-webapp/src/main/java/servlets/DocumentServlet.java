package servlets;

import api.document_access.PDFRetriever;
import document_access.DocumentRetriever;
import exception.SearchException;
import org.apache.commons.io.FileUtils;
import api.reader.LuceneIndexReader;
import api.exception.LuceneSearchException;
import servlets.servlet_util.RequestUtils;
import servlets.servlet_util.ServletConstant;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import java.io.File;
import java.io.IOException;

/**
 * Returns PDF documents from the filesystem.
 * Created by Chris on 10/8/2015.
 */
@WebServlet(value = "/docs", name = "docServlet")
public class DocumentServlet extends GenericServlet {
    /**
     * Servlet Service for getting Documents
     *
     * @param req Required parameters:
     *            docId: The document ID to get the most common terms for
     * @param res Response contains a PDF file that can be displayed in the browser
     * @throws ServletException
     * @throws IOException
     */
    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        int docId = RequestUtils.getIntegerParameter(req, ServletConstant.DOC_ID);
        DocumentRetriever retriever;
        try {
            retriever = new PDFRetriever(LuceneIndexReader.getInstance());
        } catch (LuceneSearchException e) {
            e.printStackTrace();
            return;
        }

        try {
            res.setContentType("application/pdf");
            File document = retriever.getDocument(docId);
            FileUtils.copyFile(document, res.getOutputStream());
        } catch (SearchException |
                NullPointerException e) {
            e.printStackTrace();
        }
    }
}
