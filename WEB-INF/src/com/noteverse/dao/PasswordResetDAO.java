package com.noteverse.dao;

import com.noteverse.db.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * All database access for the forgot/reset password flow.
 * Expiry and "recently requested" checks use the database clock (NOW())
 * on purpose, so there are no Java/JDBC timezone surprises.
 */
public class PasswordResetDAO {

    public static class UserRef {
        public final int userId;
        public final String fullName;
        public final String email;

        UserRef(int userId, String fullName, String email) {
            this.userId = userId;
            this.fullName = fullName;
            this.email = email;
        }
    }

    public UserRef findUserByEmail(String email) {
        String sql = "SELECT user_id, full_name, email FROM users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new UserRef(rs.getInt("user_id"), rs.getString("full_name"), rs.getString("email"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Guards against someone hammering the form (and burning the daily email quota)
    public boolean wasRequestedRecently(int userId) {
        String sql = "SELECT 1 FROM password_resets WHERE user_id = ? "
                   + "AND created_at > DATE_SUB(NOW(), INTERVAL 1 MINUTE)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Replaces any earlier reset links for this user with a fresh 30-minute one
    public boolean createResetToken(int userId, String tokenHash) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM password_resets WHERE user_id = ?")) {
                    del.setInt(1, userId);
                    del.executeUpdate();
                }
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO password_resets (user_id, token_hash, expires_at) "
                      + "VALUES (?, ?, DATE_ADD(NOW(), INTERVAL 30 MINUTE))")) {
                    ins.setInt(1, userId);
                    ins.setString(2, tokenHash);
                    ins.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isTokenValid(String tokenHash) {
        String sql = "SELECT 1 FROM password_resets "
                   + "WHERE token_hash = ? AND used = 0 AND expires_at > NOW()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tokenHash);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Validates the token, updates the password, and burns the token - all in one transaction
    public boolean resetPassword(String tokenHash, String newPasswordHash) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int resetId = -1;
                int userId = -1;

                try (PreparedStatement sel = conn.prepareStatement(
                        "SELECT reset_id, user_id FROM password_resets "
                      + "WHERE token_hash = ? AND used = 0 AND expires_at > NOW() FOR UPDATE")) {
                    sel.setString(1, tokenHash);
                    try (ResultSet rs = sel.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        resetId = rs.getInt("reset_id");
                        userId = rs.getInt("user_id");
                    }
                }

                try (PreparedStatement upd = conn.prepareStatement(
                        "UPDATE users SET password_hash = ? WHERE user_id = ?")) {
                    upd.setString(1, newPasswordHash);
                    upd.setInt(2, userId);
                    upd.executeUpdate();
                }

                try (PreparedStatement mark = conn.prepareStatement(
                        "UPDATE password_resets SET used = 1 WHERE reset_id = ?")) {
                    mark.setInt(1, resetId);
                    mark.executeUpdate();
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
