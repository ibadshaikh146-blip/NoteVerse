package com.noteverse.servlet;

import com.noteverse.dao.AdminDAO;
import com.noteverse.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/admin/moderate")
public class AdminModerateServlet extends HttpServlet {
    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!AdminPendingNotesServlet.isAdmin(request)) {
            respond(response, 403, false, "Admins only.");
            return;
        }

        String noteIdStr = request.getParameter("noteId");
        String action = request.getParameter("action"); // Expects 'APPROVE', 'REJECT', or 'DELETE'

        if (noteIdStr == null || action == null) {
            respond(response, 400, false, "Missing parameters.");
            return;
        }

        int noteId;
        try {
            noteId = Integer.parseInt(noteIdStr);
        } catch (NumberFormatException e) {
            respond(response, 400, false, "Invalid note ID.");
            return;
        }

        boolean success = false;
        if ("APPROVE".equalsIgnoreCase(action)) {
            success = adminDAO.updateNoteStatus(noteId, "APPROVED");
        } else if ("REJECT".equalsIgnoreCase(action)) {
            success = adminDAO.updateNoteStatus(noteId, "REJECTED");
        } else if ("DELETE".equalsIgnoreCase(action)) {
            // Clean up a LOCAL PDF file from server storage, if this note predates
            // the Cloudinary migration. Cloudinary-hosted files (https://...) are
            // left as-is — deleting those would need a separate Cloudinary API call.
            String filePath = adminDAO.getFilePathById(noteId);
            if (filePath != null && filePath.startsWith("uploads/")) {
                File file = new File(getServletContext().getRealPath("") + File.separator + filePath);
                if (file.exists()) {
                    file.delete();
                }
            }
            success = adminDAO.deleteNote(noteId);
        }

        if (success) {
            respond(response, 200, true, "Action executed successfully.");
        } else {
            respond(response, 500, false, "Failed to perform action on note.");
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