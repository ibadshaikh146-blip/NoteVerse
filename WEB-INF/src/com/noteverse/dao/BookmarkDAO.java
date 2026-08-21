package com.noteverse.dao;

import com.noteverse.db.DBConnection;
import com.noteverse.model.Note;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookmarkDAO {

    // Toggle bookmark: if it exists, remove it; if not, add it.
    // Returns true if the note is now bookmarked, false if it was just removed.
    public boolean toggleBookmark(int userId, int noteId) {
        if (isBookmarked(userId, noteId)) {
            removeBookmark(userId, noteId);
            return false;
        } else {
            addBookmark(userId, noteId);
            return true;
        }
    }

    public boolean addBookmark(int userId, int noteId) {
        String sql = "INSERT IGNORE INTO bookmarks (user_id, note_id) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, noteId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeBookmark(int userId, int noteId) {
        String sql = "DELETE FROM bookmarks WHERE user_id = ? AND note_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, noteId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isBookmarked(int userId, int noteId) {
        String sql = "SELECT 1 FROM bookmarks WHERE user_id = ? AND note_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, noteId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all notes a user has bookmarked — powers their "Saved Notes" page
    public List<Note> getBookmarkedNotes(int userId) {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT n.*, s.subject_name FROM bookmarks b " +
                     "JOIN notes n ON b.note_id = n.note_id " +
                     "JOIN subjects s ON n.subject_id = s.subject_id " +
                     "WHERE b.user_id = ? ORDER BY b.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Note note = new Note();
                    note.setNoteId(rs.getInt("note_id"));
                    note.setTitle(rs.getString("title"));
                    note.setDescription(rs.getString("description"));
                    note.setSubjectId(rs.getInt("subject_id"));
                    note.setSubjectName(rs.getString("subject_name"));
                    note.setSemester(rs.getInt("semester"));
                    note.setNoteType(rs.getString("note_type"));
                    note.setFilePath(rs.getString("file_path"));
                    note.setUploaderId(rs.getInt("uploader_id"));
                    note.setUploadDate(rs.getTimestamp("upload_date"));
                    note.setDownloadCount(rs.getInt("download_count"));
                    notes.add(note);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }
}
