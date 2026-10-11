package dao;

import model.Course;
import java.sql.SQLException;
import java.util.List;

public interface CourseDAO {
    List<Course> searchCourses(String query) throws SQLException;
    boolean saveCourse(Course course) throws SQLException;
    boolean deleteCourse(int courseId) throws SQLException;
    List<String[]> getMaterials(int courseId) throws SQLException;
    boolean saveMaterial(int courseId, int lecturerId, String title, String filePath) throws SQLException;
    boolean updateMaterial(int materialId, String title, String filePath) throws SQLException;
    boolean deleteMaterial(int materialId) throws SQLException;
    List<String[]> getUndergraduates() throws SQLException;
    List<String[]> getEnrollments(int courseId) throws SQLException;
    boolean enroll(int undergraduateId, int courseId) throws SQLException;
    boolean withdraw(int enrollmentId) throws SQLException;
}
