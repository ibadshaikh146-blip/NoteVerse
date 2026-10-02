package com.noteverse.servlet;

import com.noteverse.dao.AdminDAO;
import com.noteverse.model.Note;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/admin/pending-notes")
public class AdminPendingNotesServlet extends HttpServlet {
    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!isAdmin(request)) {
            response.setStatus(403);
            response.getWriter().print("{\"success\":false,\"message\":\"Admins only.\"}");
            return;
        }

        List<Note> pendingNotes = adminDAO.getPendingNotes();

        // Build manual JSON array to avoid external dependency issues
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < pendingNotes.size(); i++) {
            Note n = pendingNotes.get(i);
            json.append("{")
                .append("\"noteId\":").append(n.getNoteId()).append(",")
                .append("\"title\":\"").append(escapeJson(n.getTitle())).append("\",")
                .append("\"description\":\"").append(escapeJson(n.getDescription())).append("\",")
                .append("\"semester\":").append(n.getSemester()).append(",")
                .append("\"noteType\":\"").append(escapeJson(n.getNoteType())).append("\",")
                .append("\"filePath\":\"").append(escapeJson(n.getFilePath())).append("\"")
                .append("}");
            if (i < pendingNotes.size() - 1) json.append(",");
        }
        json.append("]");

        try (PrintWriter out = response.getWriter()) {
            out.print(json.toString());
            out.flush();
        }
    }

    static boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null
            && session.getAttribute("userId") != null
            && "ADMIN".equals(session.getAttribute("role"));
    }

    private String escapeJson(String val) {
        if (val == null) return "";
        return val.replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}