package fams.TG_2024_2138;

import fams.TG_2024_2133.DBConnection;
import fams.TG_2024_2133.DatabaseInitializer;
import fams.TG_2024_2133.ValidationException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Course master data (courses table) and course materials. */
public class CourseManagement {

    public record Course(String code, String name, int theoryCredits, int practicalCredits, String department) {}
    public record Material(int id, String title, String filePath, Timestamp uploaded) {}

    private static final String MATERIALS_SQL =
            "SELECT material_id,title,file_path,uploaded_date FROM course_materials WHERE course_code=? ORDER BY uploaded_date DESC";

    /** Falls back to the in-memory demo course list when MySQL is not reachable. */
    public List<Course> listCourses() throws SQLException {
        List<Course> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection()) {
            if (c == null) {
                DatabaseInitializer.COURSES_MAP.forEach((code, name) ->
                        list.add(new Course(code, name, 2, 1, "ICT Department")));
                return list;
            }
            try (Statement s = c.createStatement();
                 ResultSet r = s.executeQuery("SELECT course_code,course_name,theory_credits,practical_credits,department "
                         + "FROM courses ORDER BY course_code")) {
                while (r.next())
                    list.add(new Course(r.getString(1), r.getString(2), r.getInt(3), r.getInt(4), r.getString(5)));
            }
        }
        return list;
    }

    public void saveCourse(Course course, boolean isNew) throws SQLException, ValidationException {
        if (course.code().isBlank() || course.name().isBlank() || course.department().isBlank()
                || course.theoryCredits() < 0 || course.practicalCredits() < 0)
            throw new ValidationException("Enter valid course details and non-negative credits.");
        try (Connection c = DBConnection.getConnection()) {
            if (c == null) {
                DatabaseInitializer.COURSES_MAP.put(course.code(), course.name());
                return;
            }
            String sql = isNew
                    ? "INSERT INTO courses(course_code,course_name,theory_credits,practical_credits,department) VALUES(?,?,?,?,?)"
                    : "UPDATE courses SET course_name=?,theory_credits=?,practical_credits=?,department=? WHERE course_code=?";
            try (PreparedStatement s = c.prepareStatement(sql)) {
                if (isNew) {
                    s.setString(1, course.code());
                    s.setString(2, course.name());
                    s.setInt(3, course.theoryCredits());
                    s.setInt(4, course.practicalCredits());
                    s.setString(5, course.department());
                } else {
                    s.setString(1, course.name());
                    s.setInt(2, course.theoryCredits());
                    s.setInt(3, course.practicalCredits());
                    s.setString(4, course.department());
                    s.setString(5, course.code());
                }
                s.executeUpdate();
            }
        }
    }

    public void deleteCourse(String code) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            if (c == null) {
                DatabaseInitializer.COURSES_MAP.remove(code);
                return;
            }
            try (PreparedStatement s = c.prepareStatement("DELETE FROM courses WHERE course_code=?")) {
                s.setString(1, code);
                s.executeUpdate();
            }
        }
    }

    /** Returns an empty list when MySQL is unavailable; callers decide on any demo fallback. */
    public List<Material> listMaterials(String courseCode) throws SQLException {
        List<Material> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection()) {
            if (c == null) return list;
            try (PreparedStatement q = c.prepareStatement(MATERIALS_SQL)) {
                q.setString(1, courseCode);
                try (ResultSet r = q.executeQuery()) {
                    while (r.next())
                        list.add(new Material(r.getInt(1), r.getString(2), r.getString(3), r.getTimestamp(4)));
                }
            }
        }
        return list;
    }

    public void addMaterial(String courseCode, String lecturerUserId, String title, String filePath)
            throws SQLException, ValidationException {
        if (title == null || title.isBlank() || filePath == null || filePath.isBlank())
            throw new ValidationException("Title and file path are required.");
        try (Connection c = DBConnection.getConnection()) {
            requireDb(c, "Course materials");
            try (PreparedStatement ins = c.prepareStatement(
                    "INSERT INTO course_materials(course_code,lecturer_user_id,title,file_path) VALUES(?,?,?,?)")) {
                ins.setString(1, courseCode);
                ins.setString(2, lecturerUserId);
                ins.setString(3, title.trim());
                ins.setString(4, filePath.trim());
                ins.executeUpdate();
            }
        }
    }

    public void deleteMaterial(int materialId) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            requireDb(c, "Course materials");
            try (PreparedStatement del = c.prepareStatement("DELETE FROM course_materials WHERE material_id=?")) {
                del.setInt(1, materialId);
                del.executeUpdate();
            }
        }
    }

    static void requireDb(Connection c, String feature) throws SQLException {
        if (c == null)
            throw new SQLException(feature + " require MySQL. Start MySQL and import the database script.");
    }
}
