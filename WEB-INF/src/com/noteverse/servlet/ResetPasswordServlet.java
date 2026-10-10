package com.noteverse.servlet;

import com.noteverse.dao.PasswordResetDAO;
import com.noteverse.util.JsonUtil;
import com.noteverse.util.PasswordUtil;
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

/**
 * GET  /reset-password?token=...  -> {"valid": true|false}   (lets the page
 *                                    show "link expired" before asking for a password)
 * POST /reset-password            -> body: {"token": "...", "password": "..."}
 */
@WebServlet("/reset-password")
public class ResetPasswordServlet extends HttpServlet {

    private final PasswordResetDAO resetDAO = new PasswordResetDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String token = request.getParameter("token");
        boolean valid = TokenUtil.looksLikeToken(token)
                     && resetDAO.isTokenValid(TokenUtil.sha256Hex(token));

        try (PrintWriter out = response.getWriter()) {
            out.print("{\"valid\":" + valid + "}");
        }
    }

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
        String token = fields.getOrDefault("token", "");
        String password = fields.getOrDefault("password", "");

        if (password.length() < 8) {
            respond(response, 400, false, "Password must be at least 8 characters.");
            return;
        }

        if (!TokenUtil.looksLikeToken(token)) {
            respond(response, 400, false, "This reset link is invalid or has expired.");
            return;
        }

        boolean updated = resetDAO.resetPassword(
            TokenUtil.sha256Hex(token),
            PasswordUtil.hash(password)
        );

        if (updated) {
            respond(response, 200, true, "Password updated! Redirecting to login…");
        } else {
            respond(response, 400, false, "This reset link is invalid or has expired.");
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
