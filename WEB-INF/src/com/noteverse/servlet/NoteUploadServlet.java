package com.noteverse.servlet;

import com.noteverse.dao.NoteDAO;
import com.noteverse.model.Note;
import com.noteverse.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@WebServlet("/upload")
@MultipartConfig(
    maxFileSize = 20L * 1024 * 1024,      // 20MB max file size
    maxRequestSize = 25L * 1024 * 1024
)
public class NoteUploadServlet extends HttpServlet {

    private final NoteDAO noteDAO = new NoteDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            respond(response, 401, false, "You must be logged in to upload a note.");
            return;
        }
        int uploaderId = (int) session.getAttribute("userId");

        String title = request.getParameter("noteTitle");
        String semesterStr = request.getParameter("semester");
        String subjectIdStr = request.getParameter("subject");
        String resourceType = request.getParameter("resourceType");
        String description = request.getParameter("noteDescription");

        if (title == null || title.trim().isEmpty()) {
            respond(response, 400, false, "Please enter a title.");
            return;
        }

        int semester, subjectId;
        try {
            semester = Integer.parseInt(semesterStr);
            subjectId = Integer.parseInt(subjectIdStr);
        } catch (NumberFormatException | NullPointerException e) {
            respond(response, 400, false, "Please select a semester and subject.");
            return;
        }

        Part filePart = request.getPart("noteFile");
        if (filePart == null || filePart.getSize() == 0) {
            respond(response, 400, false, "Please choose a PDF to upload.");
            return;
        }

        String fileUrl;
        try {
            // Define local upload directory inside the web application
            String uploadPath = getServletContext().getRealPath("") + File.separator + "uploads";
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }

            // Generate a unique file name to prevent overwriting
            String originalFileName = filePart.getSubmittedFileName();
            String uniqueFileName = UUID.randomUUID().toString() + "_" + (originalFileName != null ? originalFileName.replaceAll("\\s+", "_") : "note.pdf");
            File filePath = new File(uploadPath + File.separator + uniqueFileName);

            // Save the file locally on the server
            try (InputStream fileContent = filePart.getInputStream()) {
                Files.copy(fileContent, filePath.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // Construct the relative file URL path
            fileUrl = "uploads/" + uniqueFileName;

        } catch (Exception e) {
            e.printStackTrace();
            respond(response, 500, false, "Server storage failed: " + e.getMessage());
            return;
        }

        // Save note record into Aiven MySQL database
        Note note = new Note();
        note.setTitle(title.trim());
        note.setDescription(description != null ? description.trim() : "");
        note.setSubjectId(subjectId);
        note.setSemester(semester);
        note.setNoteType(resourceType);
        note.setFilePath(fileUrl);
        note.setUploaderId(uploaderId);

        boolean saved = noteDAO.addNote(note);

        if (saved) {
            respond(response, 200, true, "Uploaded! Your note is pending admin review.");
        } else {
            respond(response, 500, false, "Something went wrong saving your upload details.");
        }
    }

    private void respond(HttpServletResponse response, int statusCode, boolean success, String message)
            throws IOException {
        response.setStatus(statusCode);
        try (PrintWriter out = response.getWriter()) {
            out.print(JsonUtil.successResponse(success, message));
        }
    }
}