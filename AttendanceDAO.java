package dao;

import model.*;
import util.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class AttendanceDAO {
    public void saveOrUpdate(AttendanceRecord r) throws SQLException {
        String find = "SELECT attendance_id FROM attendance_records ar JOIN undergraduates u ON u.undergraduate_id=ar.undergraduate_id JOIN courses c ON c.course_id=ar.course_id WHERE u.reg_number=? AND c.course_code=? AND ar.component_type=? AND ar.session_number=?";
        try (Connection cn=DBConnection.getConnection(); PreparedStatement ps=cn.prepareStatement(find)) {
            ps.setString(1,r.getStudentRegNo());
            ps.setString(2,r.getCourseCode()); 
            ps.setString(3,r.getComponentType().name()); 
            ps.setInt(4,r.getSessionNumber());
            try(ResultSet rs=ps.executeQuery()) {
                if(rs.next()) {
                    try(PreparedStatement up=cn.prepareStatement("UPDATE attendance_records SET session_date=?, is_present=? WHERE attendance_id=?")) {
                        up.setDate(1,java.sql.Date.valueOf(r.getSessionDate())); up.setBoolean(2,r.isPresent()); up.setInt(3,rs.getInt(1)); up.executeUpdate(); return;
                    }
                }
            }
        }
        String sql="INSERT INTO attendance_records (undergraduate_id, course_id, component_type, session_number, session_date, is_present) SELECT u.undergraduate_id,c.course_id,?,?,?,? FROM undergraduates u JOIN courses c ON c.course_code=? WHERE u.reg_number=?";
        try(Connection cn=DBConnection.getConnection(); PreparedStatement ps=cn.prepareStatement(sql)) {
            ps.setString(1,r.getComponentType().name()); ps.setInt(2,r.getSessionNumber()); ps.setDate(3,java.sql.Date.valueOf(r.getSessionDate()));
            ps.setBoolean(4,r.isPresent()); ps.setString(5,r.getCourseCode()); ps.setString(6,r.getStudentRegNo());
            if(ps.executeUpdate()!=1) throw new SQLException("Student registration number or course code was not found.");
        }
    }
    public List<AttendanceRecord> getAll() throws SQLException {
        String sql="SELECT ar.attendance_id,u.reg_number,usr.name,c.course_code,c.course_name,ar.component_type,ar.session_number,ar.session_date,ar.is_present FROM attendance_records ar JOIN undergraduates u ON u.undergraduate_id=ar.undergraduate_id JOIN users usr ON usr.user_id=u.user_id JOIN courses c ON c.course_id=ar.course_id ORDER BY c.course_code,u.reg_number,ar.component_type,ar.session_number";
        List<AttendanceRecord> out=new ArrayList<>();
        try(Connection cn=DBConnection.getConnection(); PreparedStatement ps=cn.prepareStatement(sql); ResultSet rs=ps.executeQuery()){
            while(rs.next()) out.add(new AttendanceRecord(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),ComponentType.valueOf(rs.getString(6)),rs.getInt(7),rs.getDate(8).toLocalDate(),rs.getBoolean(9)));
        } return out;
    }
    public List<AttendanceRecord> getByStudentAndCourse(String reg, String code) throws SQLException {
        List<AttendanceRecord> out=new ArrayList<>();
        String sql="SELECT ar.attendance_id,u.reg_number,usr.name,c.course_code,c.course_name,ar.component_type,ar.session_number,ar.session_date,ar.is_present FROM attendance_records ar JOIN undergraduates u ON u.undergraduate_id=ar.undergraduate_id JOIN users usr ON usr.user_id=u.user_id JOIN courses c ON c.course_id=ar.course_id WHERE u.reg_number=? AND c.course_code=? ORDER BY ar.component_type,ar.session_number";
        try(Connection cn=DBConnection.getConnection(); PreparedStatement ps=cn.prepareStatement(sql)){
            ps.setString(1,reg); ps.setString(2,code); try(ResultSet rs=ps.executeQuery()){while(rs.next())out.add(new AttendanceRecord(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),ComponentType.valueOf(rs.getString(6)),rs.getInt(7),rs.getDate(8).toLocalDate(),rs.getBoolean(9)));}
        } return out;
    }
    public void delete(int id) throws SQLException { try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement("DELETE FROM attendance_records WHERE attendance_id=?")){p.setInt(1,id);p.executeUpdate();} }
}
