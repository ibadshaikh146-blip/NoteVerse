package com.noteverse.servlet;

import com.noteverse.dao.NoteDAO;
import com.noteverse.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Handles POST /admin/moderate-note — approves or rejects a pending note.
 * Body: {"noteId": "5", "action": "APPROVE"} or {"action": "REJECT"}
 * Admin-only, enforced server-side via session role check.
 */
@WebServlet("/admin/moderate-note")
public class AdminModerateServlet extends HttpServlet {

    private final NoteDAO noteDAO = new NoteDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || !"ADMIN".equals(session.getAttribute("role"))) {
            respond(response, 403, false, "Admins only.");
            return;
        }

        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }

        Map<String, String> fields = JsonUtil.parseFlatJson(body.toString());
        String noteIdStr = fields.getOrDefault("noteId", "");
        String action = fields.getOrDefault("action", "").toUpperCase();

        int noteId;
        try {
            noteId = Integer.parseInt(noteIdStr);
        } catch (NumberFormatException e) {
            respond(response, 400, false, "Invalid note ID.");
            return;
        }

        String newStatus;
        if ("APPROVE".equals(action)) {
            newStatus = "APPROVED";
        } else if ("REJECT".equals(action)) {
            newStatus = "REJECTED";
        } else {
            respond(response, 400, false, "Action must be APPROVE or REJECT.");
            return;
        }

        boolean updated = noteDAO.updateNoteStatus(noteId, newStatus);

        if (updated) {
            respond(response, 200, true, "Note " + newStatus.toLowerCase() + ".");
        } else {
            respond(response, 500, false, "Could not update that note. It may no longer exist.");
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
