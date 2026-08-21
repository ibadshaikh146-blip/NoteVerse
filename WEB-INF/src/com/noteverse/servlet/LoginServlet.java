package com.noteverse.servlet;

import com.noteverse.dao.UserDAO;
import com.noteverse.model.User;
import com.noteverse.util.JsonUtil;
import com.noteverse.util.PasswordUtil;

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
 * Handles POST /login — checks credentials and starts an HttpSession on success.
 * Reads JSON body (same style as RegisterServlet): {identifier, password, rememberMe}
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    // 30 days in seconds, used when "remember me" is checked
    private static final int REMEMBER_ME_TIMEOUT = 60 * 60 * 24 * 30;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                body.append(line);
            }
        }

        Map<String, String> fields = JsonUtil.parseFlatJson(body.toString());
        String identifier = fields.getOrDefault("identifier", "").trim();
        String password = fields.getOrDefault("password", "");
        boolean rememberMe = "true".equalsIgnoreCase(fields.getOrDefault("rememberMe", "false"));

        if (identifier.isEmpty() || password.isEmpty()) {
            respond(response, 400, false, "Enter both your email/student ID and password.");
            return;
        }

        User user = userDAO.getUserByIdentifier(identifier);

        if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
            // Deliberately vague — never reveal whether the identifier or the
            // password was the wrong part, that leaks which accounts exist.
            respond(response, 401, false, "Incorrect email/student ID or password.");
            return;
        }

        // ---- Success: start a session ----
        HttpSession session = request.getSession(true); // create if not exists
        session.setAttribute("userId", user.getUserId());
        session.setAttribute("fullName", user.getFullName());
        session.setAttribute("email", user.getEmail());
        session.setAttribute("rollNumber", user.getRollNumber());
        session.setAttribute("semester", user.getSemester());
        session.setAttribute("role", user.getRole());

        if (rememberMe) {
            session.setMaxInactiveInterval(REMEMBER_ME_TIMEOUT);
        }
        // else: leave the container default (Tomcat's default is 30 min),
        // so the session naturally expires soon after the browser is idle.

        respond(response, 200, true, "Login successful! Redirecting…");
    }

    private void respond(HttpServletResponse response, int statusCode, boolean success, String message)
            throws IOException {
        response.setStatus(statusCode);
        try (PrintWriter out = response.getWriter()) {
            out.print(JsonUtil.successResponse(success, message));
        }
    }
}
