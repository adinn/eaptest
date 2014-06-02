package org.my.eaptest;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringEscapeUtils;

import org.apache.log4j.Logger;

import javax.inject.Inject;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;


@WebServlet(name = "UploadServlet", urlPatterns = {"/upload"})
@MultipartConfig
public class UploadServlet extends HttpServlet {
    private final int MAX_FILE_SIZE = 5000 * 1024;

    @Inject
    private Logger log;

    private static String fromStream(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html;charset=UTF-8");
        ServletOutputStream out = resp.getOutputStream();

        // Check that we have a file upload request.
        if (!ServletFileUpload.isMultipartContent(req)) {
            out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet upload</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<p>No file uploaded!</p>");
            out.println("</body>");
            out.println("</html>");

            return;
        }

        // Create a factory for disk-based file items.
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // Configure a repository (to ensure a secure temp location is used).
        ServletContext servletContext = this.getServletConfig().getServletContext();
        File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
        factory.setRepository(repository);
        // Maximum size that will be stored in memory.
        factory.setSizeThreshold(MAX_FILE_SIZE);
        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        // Parse the request to get file items.
        try {
            List<FileItem> items = upload.parseRequest(req);

            out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet upload</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<div style=\"text-align: center;\">");
            Iterator<FileItem> iter = items.iterator();
            while (iter.hasNext()) {
                FileItem item = iter.next();
                String contentType = item.getContentType();
                String name = item.getName();
                String fieldName = item.getFieldName();
                long size = item.getSize();
                out.println("<p><pre>Name : " + name + "</pre><br/>");
                out.println("<p><pre>Field Name : " + fieldName + "</pre><br/>");
                out.println("<p><pre>Content Type : " + contentType + "</pre><br/>");
                if (!item.isFormField()) {
                    // Transform the input.
                    String input = item.toString();
                    String output = input;

                    // Show the input jPDL.
                    out.println("<p><h2>Input :</h2></p>");
                    out.println("<textarea rows=\"20\" cols=\"150\">");
                    out.println("<pre>" + StringEscapeUtils.escapeHtml(input) + "</pre>");
                    out.println("</textarea>");

                    // Show the final output BPMN2.
                    out.println("<p><h2>Output:</h2></p>");
                    out.println("<textarea rows=\"20\" cols=\"150\">");
                    out.println("<pre>" + StringEscapeUtils.escapeHtml(output) + "</pre>");
                    out.println("</textarea>");
                }
            }
            out.println("</body>");
            out.println("</html>");
        } catch (FileUploadException fuEx) {
            log.error("Problem with process file upload:", fuEx);
        }
    }
}