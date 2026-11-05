package fams.TG_2024_2133;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User authenticate(String username, String password) {
        if (username == null || password == null) return null;

        String loginId = username.trim();
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                // Students can sign in with either their username (student1) or registration number.
                String sql = "SELECT * FROM users WHERE (LOWER(username) = LOWER(?) "
                           + "OR UPPER(student_reg_no) = UPPER(?)) AND password = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, loginId);
                    pstmt.setString(2, loginId);
                    pstmt.setString(3, password);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            return extractUserFromResultSet(rs);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            // Continue to the sample-data fallback when the database is unavailable
            // or its schema has not been initialized.
        }

        // Also allow registration-number login in memory fallback mode.
        DatabaseInitializer.initializeSampleData();
        User user = DatabaseInitializer.USERS_MAP.get(loginId);
        if (user == null) {
            for (User candidate : DatabaseInitializer.USERS_MAP.values()) {
                if (candidate instanceof UndergraduateUser) {
                    UndergraduateUser student = (UndergraduateUser) candidate;
                    if (student.getStudentRegNo() != null
                            && student.getStudentRegNo().equalsIgnoreCase(loginId)) {
                        user = candidate;
                        break;
                    }
                }
            }
        }
        return user != null && user.getPassword() != null && user.getPassword().equals(password) ? user : null;
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
                if ((ug.getStudentRegNo() != null && ug.getStudentRegNo().equalsIgnoreCase(regNo))
                        || (ug.getUserId() != null && ug.getUserId().equalsIgnoreCase(regNo))) {
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

        Role role = Role.valueOf(roleStr == null ? "" : roleStr.trim().toUpperCase(java.util.Locale.ROOT));

        User userObj;
        switch (role) {
            case ADMIN:
                userObj = new AdminUser(userId, username, pass, name, email, phone, dept);
                break;
            case LECTURER:
                userObj = new LecturerUser(userId, username, pass, name, email, phone, dept);
                break;
            case TECHNICAL_OFFICER:
                userObj = new TechnicalOfficerUser(userId, username, pass, name, email, phone, dept);
                break;
            case UNDERGRADUATE:
                String regNo = rs.getString("student_reg_no");
                String batch = rs.getString("batch");
                boolean isRepeat = rs.getBoolean("is_repeat");
                boolean isBatchMissed = rs.getBoolean("is_batch_missed");
                userObj = new UndergraduateUser(userId, username, pass, name, email, phone, dept, regNo, batch, isRepeat, isBatchMissed);
                break;
            default:
                throw new IllegalArgumentException("Unknown Role: " + roleStr);
        }

        try {
            String pic = rs.getString("profile_picture_url");
            if (pic != null && !pic.trim().isEmpty()) {
                userObj.setProfilePictureUrl(pic);
            }
        } catch (SQLException ignored) {
        }

        return userObj;
    }
}
