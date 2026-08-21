package com.noteverse.servlet;

import com.noteverse.dao.UserDAO;
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
import java.util.regex.Pattern;

/**
 * Handles POST /update-profile — edits the CURRENTLY LOGGED-IN user's own
 * profile only. The user ID always comes from the session, never from the
 * request body, so there's no way to edit someone else's account by
 * tampering with the request.
 */
@WebServlet("/update-profile")
public class UpdateProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern ROLL_PATTERN =
        Pattern.compile("^\\d{12}$");

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            respond(response, 401, false, "You must be logged in to edit your profile.");
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
        String fullName = fields.getOrDefault("fullName", "").trim();
        String email = fields.getOrDefault("email", "").trim();
        String rollNumber = fields.getOrDefault("rollNumber", "").trim().toUpperCase();
        String semesterStr = fields.getOrDefault("semester", "").trim();

        if (fullName.length() < 2) {
            respond(response, 400, false, "Please enter your full name.");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            respond(response, 400, false, "Enter a valid email address.");
            return;
        }
        if (!ROLL_PATTERN.matcher(rollNumber).matches()) {
            respond(response, 400, false, "Roll number must be exactly 12 digits.");
            return;
        }
        int semester;
        try {
            semester = Integer.parseInt(semesterStr);
            if (semester < 1 || semester > 6) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            respond(response, 400, false, "Please select a valid semester.");
            return;
        }

        if (userDAO.emailExistsForOtherUser(email, userId)) {
            respond(response, 409, false, "Another account already uses this email.");
            return;
        }
        if (userDAO.rollNumberExistsForOtherUser(rollNumber, userId)) {
            respond(response, 409, false, "Another account already uses this roll number.");
            return;
        }

        boolean updated = userDAO.updateProfile(userId, fullName, email, rollNumber, semester);

        if (updated) {
            // Keep the session's cached values in sync with what's now in the DB,
            // so /me reflects the change immediately without needing to log back in.
            session.setAttribute("fullName", fullName);
            session.setAttribute("email", email);
            session.setAttribute("rollNumber", rollNumber);
            session.setAttribute("semester", semester);

            respond(response, 200, true, "Profile updated successfully.");
        } else {
            respond(response, 500, false, "Something went wrong while saving. Please try again.");
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
