package com.noteverse.servlet;

import com.noteverse.dao.SubjectDAO;
import com.noteverse.model.Subject;
import com.noteverse.util.JsonUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Handles GET /subjects?semester=
 * Backs the Subject dropdown on notes-explorer.html (and could back
 * upload.html's subject dropdown too, if that's switched over from its
 * hardcoded JS object later). semester is optional — omit it for all subjects.
 */
@WebServlet("/subjects")
public class SubjectsServlet extends HttpServlet {

    private final SubjectDAO subjectDAO = new SubjectDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String semesterParam = request.getParameter("semester");
        Integer semester = null;
        if (semesterParam != null && !semesterParam.isBlank()) {
            try {
                semester = Integer.parseInt(semesterParam.trim());
            } catch (NumberFormatException ignored) {
                // fall through with semester = null (all subjects)
            }
        }

        List<Subject> subjects = subjectDAO.getSubjectsBySemester(semester);

        try (PrintWriter out = response.getWriter()) {
            out.print(JsonUtil.subjectsToJson(subjects));
        }
    }
}
