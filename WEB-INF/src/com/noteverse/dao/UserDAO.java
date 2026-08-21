package com.noteverse.dao;

import com.noteverse.db.DBConnection;
import com.noteverse.model.User;

import java.sql.*;

public class UserDAO {

    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (full_name, email, roll_number, semester, password_hash, role) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getRollNumber());
            stmt.setInt(4, user.getSemester());
            stmt.setString(5, user.getPasswordHash());
            stmt.setString(6, user.getRole() == null ? "STUDENT" : user.getRole());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getUserByIdentifier(String identifier) {
        String sql = "SELECT * FROM users WHERE email = ? OR roll_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, identifier);
            stmt.setString(2, identifier.toUpperCase());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean emailExists(String email) {
        return getUserByEmail(email) != null;
    }

    public boolean rollNumberExists(String rollNumber) {
        String sql = "SELECT 1 FROM users WHERE roll_number = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rollNumber);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Used by profile editing — ignores the user's own current row, so
    // saving your profile without changing your email doesn't falsely
    // flag it as "already taken".
    public boolean emailExistsForOtherUser(String email, int excludeUserId) {
        String sql = "SELECT 1 FROM users WHERE email = ? AND user_id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            stmt.setInt(2, excludeUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean rollNumberExistsForOtherUser(String rollNumber, int excludeUserId) {
        String sql = "SELECT 1 FROM users WHERE roll_number = ? AND user_id != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, rollNumber);
            stmt.setInt(2, excludeUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Updates the editable profile fields (not password — that stays a
    // separate, more careful flow if you build it later).
    public boolean updateProfile(int userId, String fullName, String email, String rollNumber, int semester) {
        String sql = "UPDATE users SET full_name = ?, email = ?, roll_number = ?, semester = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fullName);
            stmt.setString(2, email);
            stmt.setString(3, rollNumber);
            stmt.setInt(4, semester);
            stmt.setInt(5, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setRollNumber(rs.getString("roll_number"));
        user.setSemester(rs.getInt("semester"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(rs.getString("role"));
        user.setCreatedAt(rs.getTimestamp("created_at"));
        return user;
    }
}
