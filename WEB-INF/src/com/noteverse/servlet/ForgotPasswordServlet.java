package com.noteverse.servlet;

import com.noteverse.dao.PasswordResetDAO;
import com.noteverse.util.EmailUtil;
import com.noteverse.util.JsonUtil;
import com.noteverse.util.TokenUtil;

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
 * Handles POST /forgot-password — body: {"email": "..."}.
 *
 * Always answers with the same generic success message whether or not the
 * email belongs to an account, so the form can't be used to find out
 * who has registered.
 */
@WebServlet("/forgot-password")
public class ForgotPasswordServlet extends HttpServlet {

    private final PasswordResetDAO resetDAO = new PasswordResetDAO();

    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private static final String GENERIC_MESSAGE =
        "If an account exists for that email, a reset link is on its way. "
      + "Check your inbox (and your spam folder).";

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
        String email = fields.getOrDefault("email", "").trim();

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            respond(response, 400, false, "Enter a valid email address.");
            return;
        }

        // Checked before looking the user up, so this error appears for every
        // email equally and doesn't reveal which emails are registered.
        if (!EmailUtil.isConfigured()) {
            respond(response, 500, false, "Password reset email isn't set up on the server yet.");
            return;
        }

        PasswordResetDAO.UserRef user = resetDAO.findUserByEmail(email);

        if (user != null && !resetDAO.wasRequestedRecently(user.userId)) {
            String token = TokenUtil.generateToken();

            if (resetDAO.createResetToken(user.userId, TokenUtil.sha256Hex(token))) {
                String link = buildBaseUrl(request) + "/reset-password.html?token=" + token;
                try {
                    EmailUtil.sendEmail(
                        user.email,
                        user.fullName,
                        "Reset your NoteVerse password",
                        buildEmailHtml(user.fullName, link)
                    );
                } catch (Exception e) {
                    // Logged for the developer (see Render logs); the visitor still
                    // sees the generic message.
                    System.err.println("[ForgotPassword] Could not send reset email: " + e.getMessage());
                }
            }
        }

        respond(response, 200, true, GENERIC_MESSAGE);
    }

    // APP_BASE_URL should be set in production (e.g. https://noteverse-727m.onrender.com).
    // Building the link from request headers is only a fallback for local development:
    // a forged Host header could otherwise point the emailed link at someone else's site.
    private String buildBaseUrl(HttpServletRequest request) {
        String configured = System.getenv("APP_BASE_URL");
        if (configured != null && !configured.trim().isEmpty()) {
            return configured.trim().replaceAll("/+$", "");
        }

        String proto = request.getHeader("X-Forwarded-Proto");
        if (proto == null || proto.trim().isEmpty()) {
            proto = request.getScheme();
        }
        proto = proto.split(",")[0].trim();

        String host = request.getHeader("Host");
        if (host == null || host.trim().isEmpty()) {
            host = request.getServerName() + ":" + request.getServerPort();
        }
        return proto + "://" + host;
    }

    private String buildEmailHtml(String name, String link) {
        return "<div style=\"font-family:Arial,sans-serif;max-width:480px;margin:auto;padding:24px;color:#3A2E1F;\">"
             + "<h2 style=\"margin:0 0 16px;\">Reset your NoteVerse password</h2>"
             + "<p>Hi " + htmlEscape(name) + ",</p>"
             + "<p>We received a request to reset your password. Click the button below to choose a new one. "
             + "This link expires in 30 minutes and can only be used once.</p>"
             + "<p style=\"margin:24px 0;\"><a href=\"" + link + "\" "
             + "style=\"background:#3F7D58;color:#ffffff;padding:12px 22px;border-radius:6px;"
             + "text-decoration:none;font-weight:bold;\">Reset password</a></p>"
             + "<p style=\"font-size:13px;color:#6B5A44;\">Or paste this link into your browser:<br>" + link + "</p>"
             + "<p style=\"font-size:13px;color:#6B5A44;\">If you didn't request this, you can safely ignore "
             + "this email — your password won't change.</p>"
             + "</div>";
    }

    private String htmlEscape(String value) {
        if (value == null) return "";
        return value.replace("&", "&amp;").replace("<", "&lt;")
                    .replace(">", "&gt;").replace("\"", "&quot;");
    }

    private void respond(HttpServletResponse response, int statusCode, boolean success, String message)
            throws IOException {
        response.setStatus(statusCode);
        try (PrintWriter out = response.getWriter()) {
            out.print(JsonUtil.successResponse(success, message));
        }
    }
}
