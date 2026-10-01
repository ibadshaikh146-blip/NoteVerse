package com.noteverse.servlet;

import com.noteverse.dao.AdminDAO;
import com.noteverse.model.Note;
import com.google.gson.Gson; // Assuming you use Gson for JSON formatting, or use your project's JSON utility

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/admin/pending-notes")
public class AdminPendingNotesServlet extends HttpServlet {
    private final AdminDAO adminDAO = new AdminDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Fetch the list of pending notes from the database
        List<Note> pendingNotes = adminDAO.getPendingNotes();

        // Convert the list to JSON and send it back to the frontend
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(pendingNotes);

        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
            out.flush();
        }
    }
}