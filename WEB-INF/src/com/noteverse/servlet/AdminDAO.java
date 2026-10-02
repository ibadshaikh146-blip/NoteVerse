package com.noteverse.dao;

import com.noteverse.model.Note;
import com.noteverse.model.User;
import com.noteverse.db.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminDAO {

    // ==================== NOTE MODERATION METHODS ====================

    // Fetch all notes waiting for admin review
    public List<Note> getPendingNotes() {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT * FROM notes WHERE status = 'PENDING' ORDER BY upload_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Note note = new Note();
                note.setNoteId(rs.getInt("note_id"));
                note.setTitle(rs.getString("title"));
                note.setDescription(rs.getString("description"));
                note.setSemester(rs.getInt("semester"));
                note.setSubjectId(rs.getInt("subject_id"));
                note.setNoteType(rs.getString("note_type"));
                note.setFilePath(rs.getString("file_path"));
                note.setUploaderId(rs.getInt("uploader_id"));
                notes.add(note);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    // Approve or Reject a note (update status to 'APPROVED' or 'REJECTED')
    public boolean updateNoteStatus(int noteId, String status) {
        String sql = "UPDATE notes SET status = ? WHERE note_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, noteId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get file path before deleting so we can clean up local storage if needed
    public String getFilePathById(int noteId) {
        String path = null;
        String sql = "SELECT file_path FROM notes WHERE note_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, noteId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    path = rs.getString("file_path");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return path;
    }

    // Delete note record completely from database
    public boolean deleteNote(int noteId) {
        String sql = "DELETE FROM notes WHERE note_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, noteId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ==================== USER MANAGEMENT METHODS ====================

    // Fetch all registered users for the student directory
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, full_name, email, role, created_at FROM users ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setRole(rs.getString("role"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // Update user role (e.g., promote to ADMIN or demote to STUDENT)
    public boolean updateUserRole(int userId, String role) {
        String sql = "UPDATE users SET role = ? WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role);
            stmt.setInt(2, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Delete/Ban a user account
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}