package fams.TG_2024_2133;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Administrator operations: user and role maintenance (CRUD on the users table).
 * Every write operation is guarded by {@link UserManagement#requireRole(Role...)} so the
 * rule is enforced here and not only by disabled buttons in the GUI.
 */
public class AdminManagement {

    /** Flat view of one row of the users table. */
    public record UserRow(String userId, String username, String fullName, String email,
                          String department, String role, String studentRegNo, String batch) {}

    private static final String NEEDS_MYSQL = "User management requires MySQL. Start MySQL and import the database script.";

    public List<UserRow> listUsers() throws SQLException {
        List<UserRow> rows = new ArrayList<>();
        try (Connection c = DBConnection.getConnection()) {
            if (c == null) throw new SQLException(NEEDS_MYSQL);
            try (Statement s = c.createStatement();
                 ResultSet r = s.executeQuery("SELECT user_id,username,full_name,email,department,role,student_reg_no,batch "
                         + "FROM users ORDER BY role,full_name")) {
                while (r.next()) {
                    rows.add(new UserRow(r.getString(1), r.getString(2), r.getString(3), r.getString(4),
                            r.getString(5), r.getString(6), r.getString(7), r.getString(8)));
                }
            }
        }
        return rows;
    }

    public void createUser(UserRow u, String password)
            throws SQLException, UnauthorizedAccessException, ValidationException {
        UserManagement.getInstance().requireRole(Role.ADMIN);
        validate(u);
        if (password == null || password.isEmpty())
            throw new ValidationException("Password is required for a new user.");
        try (Connection c = DBConnection.getConnection()) {
            if (c == null) throw new SQLException(NEEDS_MYSQL);
            try (PreparedStatement s = c.prepareStatement(
                    "INSERT INTO users(user_id,username,password,full_name,email,phone,department,role,student_reg_no,batch) "
                            + "VALUES(?,?,?,?,?,'',?,?,?,?)")) {
                s.setString(1, u.userId());
                s.setString(2, u.username());
                s.setString(3, password);
                s.setString(4, u.fullName());
                s.setString(5, u.email());
                s.setString(6, u.department());
                s.setString(7, u.role());
                s.setString(8, u.studentRegNo());
                s.setString(9, u.batch());
                s.executeUpdate();
            }
        }
    }

    /** Updates profile/role fields; the password is changed only when {@code newPassword} is non-empty. */
    public void updateUser(UserRow u, String newPassword)
            throws SQLException, UnauthorizedAccessException, ValidationException {
        UserManagement.getInstance().requireRole(Role.ADMIN);
        validate(u);
        boolean changePassword = newPassword != null && !newPassword.isEmpty();
        String sql = changePassword
                ? "UPDATE users SET full_name=?,email=?,department=?,role=?,student_reg_no=?,batch=?,password=? WHERE user_id=?"
                : "UPDATE users SET full_name=?,email=?,department=?,role=?,student_reg_no=?,batch=? WHERE user_id=?";
        try (Connection c = DBConnection.getConnection()) {
            if (c == null) throw new SQLException(NEEDS_MYSQL);
            try (PreparedStatement s = c.prepareStatement(sql)) {
                s.setString(1, u.fullName());
                s.setString(2, u.email());
                s.setString(3, u.department());
                s.setString(4, u.role());
                s.setString(5, u.studentRegNo());
                s.setString(6, u.batch());
                if (changePassword) {
                    s.setString(7, newPassword);
                    s.setString(8, u.userId());
                } else {
                    s.setString(7, u.userId());
                }
                s.executeUpdate();
            }
        }
    }

    public void deleteUser(String userId) throws SQLException, UnauthorizedAccessException {
        UserManagement.getInstance().requireRole(Role.ADMIN);
        try (Connection c = DBConnection.getConnection()) {
            if (c == null) throw new SQLException(NEEDS_MYSQL);
            try (PreparedStatement s = c.prepareStatement("DELETE FROM users WHERE user_id=?")) {
                s.setString(1, userId);
                s.executeUpdate();
            }
        }
    }

    private void validate(UserRow u) throws ValidationException {
        if (isBlank(u.userId()) || isBlank(u.username()) || isBlank(u.fullName()) || isBlank(u.department()))
            throw new ValidationException("User ID, username, name and department are required.");
        try {
            Role.valueOf(u.role());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ValidationException("Unknown role: " + u.role());
        }
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
