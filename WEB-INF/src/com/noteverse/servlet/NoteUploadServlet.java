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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Handles POST /upload (multipart/form-data) — saves the PDF to disk and
 * inserts a PENDING note record. Requires login (uploaderId comes from the
 * session, never trusted from the form).
 */
@WebServlet("/upload")
@MultipartConfig(
    maxFileSize = 20L * 1024 * 1024,       // 20MB, matches upload.html's own limit
    maxRequestSize = 25L * 1024 * 1024
)
public class NoteUploadServlet extends HttpServlet {

    private final NoteDAO noteDAO = new NoteDAO();

    // Files get saved to <webapp root>/uploads/ — a real folder on disk,
    // separate from WEB-INF, so downloaded files can be served directly.
    private static final String UPLOAD_SUBDIR = "uploads";

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
        String subjectIdStr = request.getParameter("subject"); // now sends a real subjectId
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

        if (resourceType == null || resourceType.isBlank()) {
            respond(response, 400, false, "Please select a resource type.");
            return;
        }

        Part filePart = request.getPart("noteFile");
        if (filePart == null || filePart.getSize() == 0) {
            respond(response, 400, false, "Please choose a PDF to upload.");
            return;
        }

        String originalName = getSubmittedFileName(filePart);
        if (originalName == null || !originalName.toLowerCase().endsWith(".pdf")) {
            respond(response, 400, false, "Only PDF files are accepted.");
            return;
        }

        // ---- Save the file to disk with a unique name (avoid collisions/overwrites) ----
        String storedFileName = UUID.randomUUID() + ".pdf";
        String webappRoot = getServletContext().getRealPath("/");
        Path uploadDir = Paths.get(webappRoot, UPLOAD_SUBDIR);
        Files.createDirectories(uploadDir);
        Path targetPath = uploadDir.resolve(storedFileName);

        try (InputStream input = filePart.getInputStream()) {
            Files.copy(input, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }

        // ---- Insert the note record (status is forced to PENDING inside NoteDAO) ----
        Note note = new Note();
        note.setTitle(title.trim());
        note.setDescription(description != null ? description.trim() : "");
        note.setSubjectId(subjectId);
        note.setSemester(semester);
        note.setNoteType(resourceType);
        note.setFilePath("/" + UPLOAD_SUBDIR + "/" + storedFileName);
        note.setUploaderId(uploaderId);

        boolean saved = noteDAO.addNote(note);

        if (saved) {
            respond(response, 200, true, "Uploaded! Your note is pending admin review before it appears in Browse Notes.");
        } else {
            // Insert failed — clean up the orphaned file so it doesn't just sit there
            Files.deleteIfExists(targetPath);
            respond(response, 500, false, "Something went wrong saving your upload. Please try again.");
        }
    }

    // Part.getSubmittedFileName() is the standard way, but some older
    // browsers only populate the Content-Disposition header manually —
    // this fallback covers that edge case.
    private String getSubmittedFileName(Part part) {
        String fileName = part.getSubmittedFileName();
        if (fileName != null) return fileName;

        String header = part.getHeader("content-disposition");
        if (header != null) {
            for (String token : header.split(";")) {
                token = token.trim();
                if (token.startsWith("filename")) {
                    return token.substring(token.indexOf('=') + 1).trim().replace("\"", "");
                }
            }
        }
        return null;
    }

    private void respond(HttpServletResponse response, int statusCode, boolean success, String message)
            throws IOException {
        response.setStatus(statusCode);
        try (PrintWriter out = response.getWriter()) {
            out.print(JsonUtil.successResponse(success, message));
        }
    }
}
