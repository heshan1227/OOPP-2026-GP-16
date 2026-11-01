package com.fams.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** JDBC access for mark summaries and database-derived examination eligibility. */
public class MarksDAO {
    public record Eligibility(String studentId, String studentName, String courseCode, double caPercent, double attendancePercent, boolean eligible) { }

    public List<Eligibility> getEligibility(String studentId) throws SQLException {
        String sql = "SELECT u.user_id,u.full_name,c.course_code, " +
            "COALESCE((SELECT SUM(m.mark*a.weight_percentage/100.0) FROM marks m JOIN assessments a ON a.assessment_id=m.assessment_id WHERE a.course_code=c.course_code AND a.assessment_type='CA' AND m.student_user_id=u.user_id),0) ca_pct, " +
            "COALESCE((SELECT 100.0*SUM(ar.is_present)/COUNT(*) FROM attendance_records ar WHERE ar.student_reg_no=u.student_reg_no AND ar.course_code=c.course_code),0) attendance_pct " +
            "FROM users u JOIN courses c ON c.department=u.department WHERE u.role='UNDERGRADUATE' AND (? IS NULL OR u.user_id=? OR u.student_reg_no=?) ORDER BY u.batch,u.user_id,c.course_code";
        List<Eligibility> out=new ArrayList<>();
        try(Connection conn=DBConnection.getConnection(); PreparedStatement ps=conn.prepareStatement(sql)) {
            ps.setString(1,studentId); ps.setString(2,studentId); ps.setString(3,studentId);
            try(ResultSet rs=ps.executeQuery()) { while(rs.next()) { double ca=rs.getDouble("ca_pct"), att=rs.getDouble("attendance_pct"); out.add(new Eligibility(rs.getString("user_id"),rs.getString("full_name"),rs.getString("course_code"),ca,att,ca>=40.0&&att>=80.0)); } }
        }
        return out;
    }
}
