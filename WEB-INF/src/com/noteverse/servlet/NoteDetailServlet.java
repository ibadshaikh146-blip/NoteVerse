package com.noteverse.servlet;

import com.noteverse.dao.NoteDAO;
import com.noteverse.model.Note;
import com.noteverse.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Handles GET /note-detail?id=5 — backs note-detail.html's real content.
 * Only ever returns APPROVED notes (see NoteDAO.getNoteDetailById).
 */
@WebServlet("/note-detail")
public class NoteDetailServlet extends HttpServlet {

    private final NoteDAO noteDAO = new NoteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String idParam = request.getParameter("id");
        int noteId;
        try {
            noteId = Integer.parseInt(idParam);
        } catch (NumberFormatException | NullPointerException e) {
            response.setStatus(400);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Missing or invalid id.\"}");
            }
            return;
        }

        Note note = noteDAO.getNoteDetailById(noteId);

        try (PrintWriter out = response.getWriter()) {
            if (note == null) {
                response.setStatus(404);
                out.print("{\"error\":\"Note not found.\"}");
                return;
            }
            out.print(JsonUtil.noteDetailToJson(note));
        }
    }
}
