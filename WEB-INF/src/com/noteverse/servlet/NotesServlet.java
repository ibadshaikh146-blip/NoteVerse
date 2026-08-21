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
import java.util.Arrays;
import java.util.List;

/**
 * Handles GET /notes?semester=&subjectId=&query=&noteType=
 * Backs notes-explorer.html's live filtering. Every param is optional.
 * noteType may be a comma-separated list (checkbox filters send several).
 */
@WebServlet("/notes")
public class NotesServlet extends HttpServlet {

    private final NoteDAO noteDAO = new NoteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Integer semester = parseIntOrNull(request.getParameter("semester"));
        Integer subjectId = parseIntOrNull(request.getParameter("subjectId"));
        String query = request.getParameter("query");

        String noteTypeParam = request.getParameter("noteType");
        List<String> noteTypes = null;
        if (noteTypeParam != null && !noteTypeParam.isBlank()) {
            noteTypes = Arrays.asList(noteTypeParam.split(","));
        }

        List<Note> notes = noteDAO.searchAndFilter(semester, subjectId, query, noteTypes);

        try (PrintWriter out = response.getWriter()) {
            out.print(JsonUtil.notesToJson(notes));
        }
    }

    private Integer parseIntOrNull(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
