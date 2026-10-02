package com.noteverse.servlet;

import com.noteverse.dao.AdminDAO;
import com.noteverse.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/admin/users")
public class AdminUsersServlet extends HttpServlet {
    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!AdminPendingNotesServlet.isAdmin(request)) {
            response.setStatus(403);
            response.getWriter().print("{\"success\":false,\"message\":\"Admins only.\"}");
            return;
        }

        List<User> users = adminDAO.getAllUsers();

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            json.append("{")
                .append("\"userId\":").append(u.getUserId()).append(",")
                .append("\"fullName\":\"").append(escapeJson(u.getFullName())).append("\",")
                .append("\"email\":\"").append(escapeJson(u.getEmail())).append("\",")
                .append("\"role\":\"").append(escapeJson(u.getRole())).append("\"")
                .append("}");
            if (i < users.size() - 1) json.append(",");
        }
        json.append("]");

        try (PrintWriter out = response.getWriter()) {
            out.print(json.toString());
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (!AdminPendingNotesServlet.isAdmin(request)) {
            response.setStatus(403);
            response.getWriter().print("{\"success\":false,\"message\":\"Admins only.\"}");
            return;
        }

        String userIdStr = request.getParameter("userId");
        String action = request.getParameter("action");

        if (userIdStr == null || action == null) {
            response.setStatus(400);
            response.getWriter().print("{\"success\": false, \"message\": \"Missing parameters.\"}");
            return;
        }

        int userId;
        try {
            userId = Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            response.setStatus(400);
            response.getWriter().print("{\"success\": false, \"message\": \"Invalid user ID.\"}");
            return;
        }

        boolean success = false;

        if ("UPDATE_ROLE".equalsIgnoreCase(action)) {
            String newRole = request.getParameter("role");
            success = adminDAO.updateUserRole(userId, newRole);
        } else if ("DELETE_USER".equalsIgnoreCase(action)) {
            success = adminDAO.deleteUser(userId);
        }

        PrintWriter out = response.getWriter();
        if (success) {
            out.print("{\"success\": true, \"message\": \"User action completed successfully.\"}");
        } else {
            response.setStatus(500);
            out.print("{\"success\": false, \"message\": \"Failed to perform user action.\"}");
        }
        out.flush();
    }

    private String escapeJson(String val) {
        if (val == null) return "";
        return val.replace("\"", "\\\"").replace("\n", " ").replace("\r", "");
    }
}