package com.noteverse.servlet;

import com.noteverse.dao.BookmarkDAO;
import com.noteverse.model.Note;
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
import java.util.List;
import java.util.Map;

/**
 * GET  /bookmarks        — the logged-in user's bookmarked notes (full list)
 * POST /bookmarks        — body: {"noteId": 5}. Toggles the bookmark for
 *                           that note and responds {"bookmarked": true|false}
 */
@WebServlet("/bookmarks")
public class BookmarkServlet extends HttpServlet {

    private final BookmarkDAO bookmarkDAO = new BookmarkDAO();

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
            List<Note> notes = bookmarkDAO.getBookmarkedNotes(userId);
            out.print(JsonUtil.notesToJson(notes));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
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

            StringBuilder body = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    body.append(line);
                }
            }

            Map<String, String> fields = JsonUtil.parseFlatJson(body.toString());
            // parseFlatJson only handles string values, so pull the raw
            // number out with a tiny regex instead of relying on it here.
            int noteId = extractNoteId(body.toString());

            if (noteId <= 0) {
                response.setStatus(400);
                out.print("{\"success\":false,\"message\":\"Missing or invalid noteId.\"}");
                return;
            }

            boolean nowBookmarked = bookmarkDAO.toggleBookmark(userId, noteId);
            out.print("{\"success\":true,\"bookmarked\":" + nowBookmarked + "}");
        }
    }

    private int extractNoteId(String json) {
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("\"noteId\"\\s*:\\s*(\\d+)")
            .matcher(json);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return -1;
    }
}
