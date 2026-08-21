package com.noteverse.servlet;

import com.noteverse.dao.NoteDAO;
import com.noteverse.model.Note;
import com.noteverse.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Handles GET /admin/pending-notes — returns all PENDING notes for review.
 * Admin-only: checks session role == 'ADMIN' server-side (the real
 * enforcement point; hiding the link in the UI is not security on its own).
 */
@WebServlet("/admin/pending-notes")
public class AdminPendingNotesServlet extends HttpServlet {

    private final NoteDAO noteDAO = new NoteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
            response.setStatus(403);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"error\":\"Admins only.\"}");
            }
            return;
        }

        List<Note> pending = noteDAO.getPendingNotesWithUploader();

        try (PrintWriter out = response.getWriter()) {
            out.print(JsonUtil.adminNotesToJson(pending));
        }
    }
}
