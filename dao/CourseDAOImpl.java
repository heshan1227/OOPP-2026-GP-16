package dao;

import model.Course;
import util.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** JDBC repository for TG2138 course, material and enrollment operations. */
public class CourseDAOImpl implements CourseDAO {
    @Override public List<Course> searchCourses(String query) throws SQLException {
        String sql = "SELECT course_id, course_code, course_name, credits, component_type, description, period_id " +
                "FROM courses WHERE course_code LIKE ? OR course_name LIKE ? ORDER BY course_code";
        List<Course> results = new ArrayList<>();
        try (Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            String term="%"+(query == null ? "" : query.trim())+"%";
            p.setString(1,term); p.setString(2,term);
            try (ResultSet r=p.executeQuery()) { while(r.next()) results.add(readCourse(r)); }
        }
        return results;
    }
    @Override public boolean saveCourse(Course x) throws SQLException {
        if (x.getCourseId()==0) {
            String sql="INSERT INTO courses(course_code,course_name,credits,component_type,description,period_id) VALUES(?,?,?,?,?,?)";
            try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
                bindCourse(p,x); int n=p.executeUpdate();
                if(n>0) try(ResultSet r=p.getGeneratedKeys()){if(r.next()) x.setCourseId(r.getInt(1));}
                return n>0;
            }
        }
        String sql="UPDATE courses SET course_code=?,course_name=?,credits=?,component_type=?,description=?,period_id=? WHERE course_id=?";
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            bindCourse(p,x); p.setInt(7,x.getCourseId()); return p.executeUpdate()>0;
        }
    }
    private void bindCourse(PreparedStatement p, Course x) throws SQLException {
        p.setString(1,x.getCourseCode().trim()); p.setString(2,x.getCourseName().trim());
        p.setInt(3,x.getCredits()); p.setString(4,x.getComponentType());
        p.setString(5,x.getDescription()==null?"":x.getDescription().trim());
        if(x.getPeriodId()==null) p.setNull(6,Types.INTEGER); else p.setInt(6,x.getPeriodId());
    }
    private Course readCourse(ResultSet r) throws SQLException {
        int period=r.getInt("period_id"); Integer periodId=r.wasNull()?null:period;
        return new Course(r.getInt("course_id"),r.getString("course_code"),r.getString("course_name"),
                r.getInt("credits"),r.getString("component_type"),r.getString("description"),periodId);
    }
    @Override public boolean deleteCourse(int id) throws SQLException {
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement("DELETE FROM courses WHERE course_id=?")) {
            p.setInt(1,id); return p.executeUpdate()>0;
        }
    }
    @Override public List<String[]> getMaterials(int id) throws SQLException {
        List<String[]> rows=new ArrayList<>();
        String sql="SELECT material_id,title,file_path,uploaded_date FROM course_materials WHERE course_id=? ORDER BY uploaded_date DESC";
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,id); try(ResultSet r=p.executeQuery()){while(r.next()) rows.add(new String[]{r.getString("material_id"),r.getString("title"),r.getString("file_path"),String.valueOf(r.getTimestamp("uploaded_date"))});}
        } return rows;
    }
    @Override public boolean saveMaterial(int courseId,int lecturerId,String title,String path) throws SQLException {
        String sql="INSERT INTO course_materials(course_id,lecturer_id,title,file_path) VALUES(?,?,?,?)";
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setInt(1,courseId); if(lecturerId>0) p.setInt(2,lecturerId); else p.setNull(2,Types.INTEGER);
            p.setString(3,title.trim()); p.setString(4,path.trim()); return p.executeUpdate()>0;
        }
    }
    @Override public boolean updateMaterial(int materialId,String title,String path) throws SQLException {
        String sql="UPDATE course_materials SET title=?,file_path=? WHERE material_id=?";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,title.trim()); p.setString(2,path.trim()); p.setInt(3,materialId); return p.executeUpdate()>0;
        }
    }
    @Override public boolean deleteMaterial(int id) throws SQLException {
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement("DELETE FROM course_materials WHERE material_id=?")) {p.setInt(1,id);return p.executeUpdate()>0;}
    }
    @Override public List<String[]> getUndergraduates() throws SQLException {
        List<String[]> rows=new ArrayList<>();
        String sql="SELECT undergraduate_id,reg_number,user_id FROM undergraduates ORDER BY reg_number";
        try(Connection c=DBConnection.getConnection(); PreparedStatement p=c.prepareStatement(sql);ResultSet r=p.executeQuery()) {
            while(r.next()) rows.add(new String[]{r.getString("undergraduate_id"),r.getString("reg_number"),r.getString("user_id")});
        } return rows;
    }
    @Override public List<String[]> getEnrollments(int courseId) throws SQLException {
        List<String[]> rows=new ArrayList<>();
        String sql="SELECT e.enrollment_id,u.undergraduate_id,u.reg_number,e.status,e.enroll_date FROM enrollments e JOIN undergraduates u ON u.undergraduate_id=e.undergraduate_id WHERE e.course_id=? ORDER BY u.reg_number";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,courseId);try(ResultSet r=p.executeQuery()){while(r.next()) rows.add(new String[]{r.getString("enrollment_id"),r.getString("undergraduate_id"),r.getString("reg_number"),r.getString("status"),String.valueOf(r.getDate("enroll_date"))});}} return rows;
    }
    @Override public boolean enroll(int ugId,int courseId) throws SQLException {
        String sql="INSERT INTO enrollments(undergraduate_id,course_id,enroll_date,status) VALUES(?,?,CURRENT_DATE,'Active') ON DUPLICATE KEY UPDATE status='Active',enroll_date=CURRENT_DATE";
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){p.setInt(1,ugId);p.setInt(2,courseId);return p.executeUpdate()>0;}
    }
    @Override public boolean withdraw(int id) throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("UPDATE enrollments SET status='Dropped' WHERE enrollment_id=?")){p.setInt(1,id);return p.executeUpdate()>0;}
    }
}
