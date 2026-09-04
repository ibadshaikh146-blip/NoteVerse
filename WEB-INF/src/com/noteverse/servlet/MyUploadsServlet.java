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
 * Handles GET /my-uploads — returns the logged-in user's own notes
 * (any status: PENDING, APPROVED, REJECTED), for my-account.html's
 * "My Uploads" tab. Mirrors MeServlet's session check.
 */
@WebServlet("/my-uploads")
public class MyUploadsServlet extends HttpServlet {

    private final NoteDAO noteDAO = new NoteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        try (PrintWriter out = response.getWriter()) {
            if (session == null || session.getAttribute("userId") == null) {
                response.setStatus(401);
                out.print("{\"loggedIn\":false}");
                return;
            }

            int userId = (int) session.getAttribute("userId");
            List<Note> notes = noteDAO.getNotesByUploader(userId);

            out.print(JsonUtil.myUploadsToJson(notes));
        }
    }
}
