package fams.TG_2024_2138;

import fams.TG_2024_2133.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Academic registration: which undergraduates are enrolled in which courses. */
public class AcademicManagement {

    /** Enrols (or re-activates the enrolment of) an undergraduate in a course. */
    public void enrollStudent(String studentUserId, String courseCode) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            if (c == null)
                throw new SQLException("Enrollment requires MySQL. Start MySQL and import the database script.");
            try (PreparedStatement s = c.prepareStatement(
                    "INSERT INTO course_enrollments(student_user_id,course_code) VALUES(?,?) "
                            + "ON DUPLICATE KEY UPDATE active=TRUE")) {
                s.setString(1, studentUserId);
                s.setString(2, courseCode);
                s.executeUpdate();
            }
        }
    }

    /** Active enrolments of one student; empty when none exist or MySQL is unavailable. */
    public List<CourseManagement.Course> listEnrolledCourses(String studentUserId) throws SQLException {
        List<CourseManagement.Course> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection()) {
            if (c == null) return list;
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT c.course_code, c.course_name, c.theory_credits, c.practical_credits, c.department "
                            + "FROM course_enrollments e JOIN courses c ON c.course_code = e.course_code "
                            + "WHERE e.student_user_id = ? AND e.active = TRUE ORDER BY c.course_code")) {
                ps.setString(1, studentUserId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        list.add(new CourseManagement.Course(rs.getString(1), rs.getString(2),
                                rs.getInt(3), rs.getInt(4), rs.getString(5)));
                }
            }
        }
        return list;
    }
}
