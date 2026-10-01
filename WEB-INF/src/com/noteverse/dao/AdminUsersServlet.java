package com.noteverse.servlet;

import com.noteverse.dao.AdminDAO;
import com.noteverse.model.User;
import com.google.gson.Gson;

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

        List<User> users = adminDAO.getAllUsers();
        String jsonResponse = new Gson().toJson(users);

        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String userIdStr = request.getParameter("userId");
        String action = request.getParameter("action"); // Expects 'UPDATE_ROLE' or 'DELETE_USER'

        if (userIdStr == null || action == null) {
            response.setStatus(400);
            response.getWriter().print("{\"success\": false, \"message\": \"Missing parameters.\"}");
            return;
        }

        int userId = Integer.parseInt(userIdStr);
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
            out.setStatus(500);
            out.print("{\"success\": false, \"message\": \"Failed to perform user action.\"}");
        }
        out.flush();
    }
}