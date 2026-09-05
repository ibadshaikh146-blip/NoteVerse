package com.noteverse.servlet;

import com.noteverse.dao.NoteDAO;
import com.noteverse.model.Note;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Handles GET /download?id=5 — increments the note's download_count,
 * then redirects the browser to the actual file. Keeps the counting
 * logic server-side instead of trusting the frontend to report downloads.
 */
@WebServlet("/download")
public class DownloadServlet extends HttpServlet {

    private final NoteDAO noteDAO = new NoteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        int noteId;
        try {
            noteId = Integer.parseInt(idParam);
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(400, "Missing or invalid id.");
            return;
        }

        Note note = noteDAO.getNoteById(noteId);
        if (note == null || note.getFilePath() == null || note.getFilePath().isBlank()) {
            response.sendError(404, "File not found.");
            return;
        }

        noteDAO.incrementDownloadCount(noteId);

        String filePath = note.getFilePath();
        // filePath may be an absolute URL or a path relative to the webapp root
        response.sendRedirect(filePath);
    }
}
