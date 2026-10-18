package com.fams.database;

import com.fams.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User authenticate(String username, String password) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            if (conn != null) {
                String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, username);
                    pstmt.setString(2, password);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            return extractUserFromResultSet(rs);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            // Log & Fallback
        }

        // Memory Fallback
        DatabaseInitializer.initializeSampleData();
        User user = DatabaseInitializer.USERS_MAP.get(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                String sql = "SELECT * FROM users";
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        list.add(extractUserFromResultSet(rs));
                    }
                    return list;
                }
            }
        } catch (SQLException e) {
            // Fallback
        }

        DatabaseInitializer.initializeSampleData();
        return new ArrayList<>(DatabaseInitializer.USERS_MAP.values());
    }

    public UndergraduateUser findUndergraduateByRegNo(String regNo) {
        for (User u : getAllUsers()) {
            if (u instanceof UndergraduateUser) {
                UndergraduateUser ug = (UndergraduateUser) u;
                if (ug.getStudentRegNo().equalsIgnoreCase(regNo) || ug.getUserId().equalsIgnoreCase(regNo)) {
                    return ug;
                }
            }
        }
        return null;
    }

    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        String userId = rs.getString("user_id");
        String username = rs.getString("username");
        String pass = rs.getString("password");
        String name = rs.getString("full_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String dept = rs.getString("department");
        String roleStr = rs.getString("role");

        Role role = Role.valueOf(roleStr);

        switch (role) {
            case ADMIN:
                return new AdminUser(userId, username, pass, name, email, phone, dept);
            case LECTURER:
                return new LecturerUser(userId, username, pass, name, email, phone, dept);
            case TECHNICAL_OFFICER:
                return new TechnicalOfficerUser(userId, username, pass, name, email, phone, dept);
            case UNDERGRADUATE:
                String regNo = rs.getString("student_reg_no");
                String batch = rs.getString("batch");
                boolean isRepeat = rs.getBoolean("is_repeat");
                boolean isBatchMissed = rs.getBoolean("is_batch_missed");
                return new UndergraduateUser(userId, username, pass, name, email, phone, dept, regNo, batch, isRepeat, isBatchMissed);
            default:
                throw new IllegalArgumentException("Unknown Role: " + roleStr);
        }
    }
}
