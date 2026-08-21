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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Handles POST /register — reads the JSON body sent by register.html's
 * fetch() call, validates it server-side (never trust client-side JS
 * validation alone), and inserts the new user via UserDAO.
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

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

        // Read the raw JSON body
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
        String password = fields.getOrDefault("password", "");
        String rollNumber = fields.getOrDefault("rollNumber", "").trim();
        String semesterStr = fields.getOrDefault("semester", "").trim();

        // ---- Server-side validation (mirrors the client-side checks,
        //      but this is the copy that actually matters) ----
        if (fullName.length() < 2) {
            respond(response, 400, false, "Please enter your full name.");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            respond(response, 400, false, "Enter a valid email address.");
            return;
        }
        if (password.length() < 8) {
            respond(response, 400, false, "Password must be at least 8 characters.");
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

        // ---- Duplicate checks ----
        if (userDAO.emailExists(email)) {
            respond(response, 409, false, "An account with this email already exists.");
            return;
        }
        if (userDAO.rollNumberExists(rollNumber)) {
            respond(response, 409, false, "An account with this roll number already exists.");
            return;
        }

        // ---- Create the user ----
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setRollNumber(rollNumber.toUpperCase());
        user.setSemester(semester);
        user.setPasswordHash(PasswordUtil.hash(password));
        user.setRole("STUDENT");

        boolean created = userDAO.registerUser(user);

        if (created) {
            respond(response, 200, true, "Account created! Redirecting to login…");
        } else {
            respond(response, 500, false, "Something went wrong while creating your account. Please try again.");
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