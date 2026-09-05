package com.noteverse.dao;

import com.noteverse.db.DBConnection;
import com.noteverse.model.Note;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoteDAO {

    public List<Note> getAllNotes() {
        return searchAndFilter(null, null, null, null);
    }

    public List<Note> searchAndFilter(Integer semester, Integer subjectId, String query, List<String> noteTypes) {
        List<Note> notes = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT n.*, s.subject_name FROM notes n " +
            "JOIN subjects s ON n.subject_id = s.subject_id WHERE n.status = 'APPROVED'"
        );

        if (semester != null) sql.append(" AND n.semester = ?");
        if (subjectId != null) sql.append(" AND n.subject_id = ?");
        if (query != null && !query.isBlank()) sql.append(" AND n.title LIKE ?");
        if (noteTypes != null && !noteTypes.isEmpty()) {
            sql.append(" AND n.note_type IN (");
            for (int i = 0; i < noteTypes.size(); i++) {
                sql.append(i == 0 ? "?" : ",?");
            }
            sql.append(")");
        }
        sql.append(" ORDER BY n.upload_date DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            if (semester != null) stmt.setInt(idx++, semester);
            if (subjectId != null) stmt.setInt(idx++, subjectId);
            if (query != null && !query.isBlank()) stmt.setString(idx++, "%" + query + "%");
            if (noteTypes != null && !noteTypes.isEmpty()) {
                for (String type : noteTypes) {
                    stmt.setString(idx++, type);
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notes.add(mapRowToNote(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    public List<Note> getNotesByUploader(int uploaderId) {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT n.*, s.subject_name FROM notes n " +
                     "JOIN subjects s ON n.subject_id = s.subject_id " +
                     "WHERE n.uploader_id = ? ORDER BY n.upload_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, uploaderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notes.add(mapRowToNote(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    // ---- Admin moderation ----

    // All PENDING notes, with the uploader's name joined in so the admin
    // panel can show who submitted each one.
    public List<Note> getPendingNotesWithUploader() {
        List<Note> notes = new ArrayList<>();
        String sql = "SELECT n.*, s.subject_name, u.full_name AS uploader_name FROM notes n " +
                     "JOIN subjects s ON n.subject_id = s.subject_id " +
                     "JOIN users u ON n.uploader_id = u.user_id " +
                     "WHERE n.status = 'PENDING' ORDER BY n.upload_date ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Note note = mapRowToNote(rs);
                note.setUploaderName(rs.getString("uploader_name"));
                notes.add(note);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notes;
    }

    public boolean updateNoteStatus(int noteId, String status) {
        String sql = "UPDATE notes SET status = ? WHERE note_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, noteId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Note getNoteById(int noteId) {
        String sql = "SELECT n.*, s.subject_name FROM notes n " +
                     "JOIN subjects s ON n.subject_id = s.subject_id WHERE n.note_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, noteId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToNote(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Used by note-detail.html — a single APPROVED note, with the
    // uploader's name joined in for display.
    public Note getNoteDetailById(int noteId) {
        String sql = "SELECT n.*, s.subject_name, u.full_name AS uploader_name FROM notes n " +
                     "JOIN subjects s ON n.subject_id = s.subject_id " +
                     "JOIN users u ON n.uploader_id = u.user_id " +
                     "WHERE n.note_id = ? AND n.status = 'APPROVED'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, noteId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Note note = mapRowToNote(rs);
                    note.setUploaderName(rs.getString("uploader_name"));
                    return note;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addNote(Note note) {
        String sql = "INSERT INTO notes (title, description, subject_id, semester, note_type, status, file_path, uploader_id) " +
                     "VALUES (?, ?, ?, ?, ?, 'PENDING', ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, note.getTitle());
            stmt.setString(2, note.getDescription());
            stmt.setInt(3, note.getSubjectId());
            stmt.setInt(4, note.getSemester());
            stmt.setString(5, note.getNoteType());
            stmt.setString(6, note.getFilePath());
            stmt.setInt(7, note.getUploaderId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteNote(int noteId) {
        String sql = "DELETE FROM notes WHERE note_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, noteId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void incrementDownloadCount(int noteId) {
        String sql = "UPDATE notes SET download_count = download_count + 1 WHERE note_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, noteId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Note mapRowToNote(ResultSet rs) throws SQLException {
        Note note = new Note();
        note.setNoteId(rs.getInt("note_id"));
        note.setTitle(rs.getString("title"));
        note.setDescription(rs.getString("description"));
        note.setSubjectId(rs.getInt("subject_id"));
        note.setSubjectName(rs.getString("subject_name"));
        note.setSemester(rs.getInt("semester"));
        note.setNoteType(rs.getString("note_type"));
        note.setStatus(rs.getString("status"));
        note.setFilePath(rs.getString("file_path"));
        note.setUploaderId(rs.getInt("uploader_id"));
        note.setUploadDate(rs.getTimestamp("upload_date"));
        note.setDownloadCount(rs.getInt("download_count"));
        return note;
    }
}