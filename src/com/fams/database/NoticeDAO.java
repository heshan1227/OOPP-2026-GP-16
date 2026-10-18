package com.fams.database;

import com.fams.model.Notice;
import com.fams.model.Role;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NoticeDAO {

    public void saveNotice(Notice notice) {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                String sql = "INSERT INTO notices (title, content, published_by, published_date, target_role, is_important) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, notice.getTitle());
                    stmt.setString(2, notice.getContent());
                    stmt.setString(3, notice.getPublishedBy());
                    stmt.setTimestamp(4, Timestamp.valueOf(notice.getPublishedDate()));
                    stmt.setString(5, notice.getTargetRole() == null ? null : notice.getTargetRole().name());
                    stmt.setBoolean(6, notice.isImportant());
                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            notice.setNoticeId(rs.getInt(1));
                        }
                    }
                    return;
                }
            }
        } catch (SQLException e) {
            // Fallback
        }

        DatabaseInitializer.initializeSampleData();
        notice.setNoticeId(DatabaseInitializer.NOTICES.size() + 1);
        DatabaseInitializer.NOTICES.add(0, notice); // prepend
    }

    public void deleteNotice(int noticeId) {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                String sql = "DELETE FROM notices WHERE notice_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, noticeId);
                    stmt.executeUpdate();
                    return;
                }
            }
        } catch (SQLException e) {
            // Fallback
        }

        DatabaseInitializer.initializeSampleData();
        DatabaseInitializer.NOTICES.removeIf(n -> n.getNoticeId() == noticeId);
    }

    public List<Notice> getAllNotices() {
        List<Notice> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                String sql = "SELECT * FROM notices ORDER BY is_important DESC, published_date DESC";
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        list.add(extractNotice(rs));
                    }
                    return list;
                }
            }
        } catch (SQLException e) {
            // Fallback
        }

        DatabaseInitializer.initializeSampleData();
        return new ArrayList<>(DatabaseInitializer.NOTICES);
    }

    public List<Notice> getNoticesForRole(Role role) {
        List<Notice> result = new ArrayList<>();
        for (Notice n : getAllNotices()) {
            if (n.getTargetRole() == null || n.getTargetRole() == role || role == Role.ADMIN) {
                result.add(n);
            }
        }
        return result;
    }

    private Notice extractNotice(ResultSet rs) throws SQLException {
        int id = rs.getInt("notice_id");
        String title = rs.getString("title");
        String content = rs.getString("content");
        String author = rs.getString("published_by");
        LocalDateTime pDate = rs.getTimestamp("published_date").toLocalDateTime();
        String roleStr = rs.getString("target_role");
        Role role = roleStr == null ? null : Role.valueOf(roleStr);
        boolean imp = rs.getBoolean("is_important");

        return new Notice(id, title, content, author, pDate, role, imp);
    }
}
