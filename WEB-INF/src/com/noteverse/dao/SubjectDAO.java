package com.noteverse.dao;

import com.noteverse.db.DBConnection;
import com.noteverse.model.Subject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubjectDAO {

    public List<Subject> getAllSubjects() {
        return getSubjectsBySemester(null);
    }

    // Pass null to get subjects across all semesters
    public List<Subject> getSubjectsBySemester(Integer semester) {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT * FROM subjects" + (semester != null ? " WHERE semester = ?" : "") + " ORDER BY subject_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (semester != null) {
                stmt.setInt(1, semester);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Subject s = new Subject();
                    s.setSubjectId(rs.getInt("subject_id"));
                    s.setSubjectName(rs.getString("subject_name"));
                    s.setSemester(rs.getInt("semester"));
                    subjects.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subjects;
    }
}
