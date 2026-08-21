package com.noteverse.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Handles GET /me — tells the frontend whether someone is currently logged
 * in, and if so, who. Used by my-account.html (and could be used by any
 * page that needs to show/hide "Login" vs the user's name).
 */
@WebServlet("/me")
public class MeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false); // don't create one just to check

        try (PrintWriter out = response.getWriter()) {
            if (session == null || session.getAttribute("userId") == null) {
                out.print("{\"loggedIn\":false}");
                return;
            }

            int userId = (int) session.getAttribute("userId");
            String fullName = (String) session.getAttribute("fullName");
            String email = (String) session.getAttribute("email");
            String rollNumber = (String) session.getAttribute("rollNumber");
            int semester = (int) session.getAttribute("semester");
            String role = (String) session.getAttribute("role");

            out.print("{"
                + "\"loggedIn\":true,"
                + "\"userId\":" + userId + ","
                + "\"fullName\":\"" + escape(fullName) + "\","
                + "\"email\":\"" + escape(email) + "\","
                + "\"rollNumber\":\"" + escape(rollNumber) + "\","
                + "\"semester\":" + semester + ","
                + "\"role\":\"" + escape(role) + "\""
                + "}");
        }
    }

    private String escape(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
