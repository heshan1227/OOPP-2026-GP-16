package fams.TG_2024_2113;

import fams.TG_2024_2133.DBConnection;


import java.math.BigDecimal;   // FIX: added — BigDecimal was used but never imported
import java.sql.*;
import java.util.*;

/**
 * JDBC access layer for assessments, marks, eligibility and GPA calculations.
 * All marks are stored out of 100; CA eligibility threshold is 40%; attendance threshold is 80%.
 * Grades follow UGC Commission Circular No. 12-2024.
 */
public class MarksDAO {

    // ─── inner records ────────────────────────────────────────────────────────

    /** Examination eligibility for a single student/course pair. */
    public record Eligibility(
            String studentId, String studentName, String courseCode, String courseName,
            double gpaUsed, double sgpa, double cgpa, String status) { }

    /** CA marks summary (per assessment) for a student/course. */
    public record CaMark(
            String studentId,
            String studentName,
            String courseCode,
            String courseName,
            String assessmentName,
            double mark,
            double weight,
            double weighted) { }

    /** Final mark, grade and GPA contribution for a student/course. */
    public record GradeSummary(
            String studentId,
            String studentName,
            String courseCode,
            String courseName,
            int credits,
            double finalMark,
            String letterGrade,
            double gradePoint,
            boolean published) { }

    /** SGPA / CGPA summary for one student. */
    public record GpaSummary(
            String studentId,
            String studentName,
            double sgpa,
            double cgpa,
            List<GradeSummary> subjects) { }

    /** A course assigned to / available for a lecturer. */
    public record AssignedCourse(String courseCode, String courseName) { }

    /** Module result config (CA/ESA weights and pass thresholds). */
    public record ModuleResultConfig(
            BigDecimal caWeight, BigDecimal esaWeight, BigDecimal caPass, BigDecimal esaPass) { }

    // ─── eligibility ─────────────────────────────────────────────────────────

    /**
     * Returns eligibility rows.
     * @param studentId  user_id or student_reg_no; null/blank = all students.
     * @param courseCode course filter; null/blank = all courses in the student's department.
     */
    public List<Eligibility> getEligibility(String studentId,String courseCode) throws SQLException {
        boolean fs=studentId!=null&&!studentId.isBlank(),fc=courseCode!=null&&!courseCode.isBlank();
        String sql="SELECT u.user_id,u.full_name,c.course_code,c.course_name,es.gpa_used,es.sgpa,es.cgpa,COALESCE(es.status,'NOT_CHECKED') status "+
                "FROM course_enrollments ce JOIN users u ON u.user_id=ce.student_user_id JOIN courses c ON c.course_code=ce.course_code "+
                "LEFT JOIN eligibility_snapshots es ON es.snapshot_id=(SELECT x.snapshot_id FROM eligibility_snapshots x WHERE x.student_user_id=u.user_id ORDER BY x.checked_at DESC,x.snapshot_id DESC LIMIT 1) "+
                "WHERE ce.active=TRUE AND u.role='UNDERGRADUATE' "+(fs?"AND (u.user_id=? OR u.student_reg_no=?) ":"")+(fc?"AND c.course_code=? ":"")+"ORDER BY u.batch,u.full_name,c.course_code";
        List<Eligibility> out=new ArrayList<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            int i=1;if(fs){p.setString(i++,studentId);p.setString(i++,studentId);}if(fc)p.setString(i,courseCode);
            try(ResultSet r=p.executeQuery()){while(r.next())out.add(new Eligibility(r.getString("user_id"),r.getString("full_name"),r.getString("course_code"),r.getString("course_name"),r.getDouble("gpa_used"),r.getDouble("sgpa"),r.getDouble("cgpa"),r.getString("status")));}
        }return out;
    }

    /** Convenience overload: all courses, single or all students. */
    public List<Eligibility> getEligibility(String studentId) throws SQLException {
        return getEligibility(studentId, null);
    }

    // ─── CA marks ─────────────────────────────────────────────────────────────

    /**
     * Returns per-assessment CA mark rows.
     * @param studentId  null/blank = all students.
     * @param courseCode null/blank = all courses.
     */
    public List<CaMark> getCaMarks(String studentId,String courseCode) throws SQLException {
        boolean fs=studentId!=null&&!studentId.isBlank(), fc=courseCode!=null&&!courseCode.isBlank();
        String sql="SELECT u.user_id,u.full_name,c.course_code,c.course_name,a.assessment_name,m.mark,a.weight_percentage,ROUND(m.mark*a.weight_percentage/100,2) weighted "+
                "FROM academic_marks m JOIN academic_assessments a ON a.assessment_id=m.assessment_id JOIN users u ON u.user_id=m.student_user_id JOIN courses c ON c.course_code=a.course_code "+
                "WHERE a.component IN ('QUIZ','ASSIGNMENT','MID_TERM') "+(fs?"AND (u.user_id=? OR u.student_reg_no=?) ":"")+(fc?"AND a.course_code=? ":"")+"ORDER BY c.course_code,u.full_name,a.assessment_name";
        List<CaMark> out=new ArrayList<>();
        try(Connection conn=DBConnection.getConnection();PreparedStatement ps=conn.prepareStatement(sql)){
            int i=1;if(fs){ps.setString(i++,studentId);ps.setString(i++,studentId);}if(fc)ps.setString(i,courseCode);
            try(ResultSet rs=ps.executeQuery()){while(rs.next())out.add(new CaMark(rs.getString("user_id"),rs.getString("full_name"),rs.getString("course_code"),rs.getString("course_name"),rs.getString("assessment_name"),rs.getDouble("mark"),rs.getDouble("weight_percentage"),rs.getDouble("weighted")));}
        }return out;
    }

    // ─── grades & GPA ────────────────────────────────────────────────────────

    /**
     * Returns grade summaries (final mark + grade + GPA contribution) for each student/course.
     * @param studentId  null/blank = all.
     * @param courseCode null/blank = all.
     */
    public List<GradeSummary> getGrades(String studentId,String courseCode) throws SQLException {
        boolean fs=studentId!=null&&!studentId.isBlank(),fc=courseCode!=null&&!courseCode.isBlank();
        String sql="SELECT u.user_id,u.full_name,c.course_code,c.course_name,ar.credits,ar.final_mark,ar.grade_code,ar.grade_point,ar.is_approved "+
                "FROM academic_results ar JOIN users u ON u.user_id=ar.student_user_id JOIN courses c ON c.course_code=ar.course_code "+
                "WHERE ar.is_best=TRUE AND ar.is_approved=TRUE "+(fs?"AND (u.user_id=? OR u.student_reg_no=?) ":"")+(fc?"AND ar.course_code=? ":"")+"ORDER BY ar.course_code,u.full_name";
        List<GradeSummary> out=new ArrayList<>();
        try(Connection conn=DBConnection.getConnection();PreparedStatement ps=conn.prepareStatement(sql)){
            int i=1;if(fs){ps.setString(i++,studentId);ps.setString(i++,studentId);}if(fc)ps.setString(i,courseCode);
            try(ResultSet rs=ps.executeQuery()){while(rs.next())out.add(new GradeSummary(rs.getString("user_id"),rs.getString("full_name"),rs.getString("course_code"),rs.getString("course_name"),rs.getInt("credits"),rs.getDouble("final_mark"),rs.getString("grade_code"),rs.getDouble("grade_point"),rs.getBoolean("is_approved")));}
        }return out;
    }

    public List<GpaSummary> getGpaSummary(String studentId) throws SQLException {
        List<GradeSummary> subjects=getGrades(studentId,null);
        Map<String,List<GradeSummary>> byStudent=new LinkedHashMap<>();
        Map<String,String> names=new LinkedHashMap<>();
        for(GradeSummary g:subjects){byStudent.computeIfAbsent(g.studentId(),k->new ArrayList<>()).add(g);names.put(g.studentId(),g.studentName());}
        List<GpaSummary> result=new ArrayList<>();
        String sql="SELECT ar.student_user_id,s.semester_no,ROUND(SUM(ar.grade_point*ar.credits)/NULLIF(SUM(ar.credits),0),2) sgpa "+
                "FROM academic_results ar JOIN academic_semesters s ON s.semester_id=ar.semester_id "+
                "WHERE ar.is_approved=TRUE AND ar.is_best=TRUE AND ar.gpa_module=TRUE AND ar.grade_code NOT IN ('N','W') "+
                (studentId!=null&&!studentId.isBlank()?"AND ar.student_user_id=? ":"")+
                "GROUP BY ar.student_user_id,s.semester_id,s.semester_no ORDER BY s.semester_no";
        Map<String,List<Double>> semesterValues=new HashMap<>();Map<String,Double> latestSgpa=new HashMap<>();
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(sql)){
            if(studentId!=null&&!studentId.isBlank())p.setString(1,studentId);
            try(ResultSet r=p.executeQuery()){while(r.next()){String id=r.getString(1);double g=r.getDouble(3);semesterValues.computeIfAbsent(id,k->new ArrayList<>()).add(g);latestSgpa.put(id,g);}}
        }
        for(var e:byStudent.entrySet()){
            List<Double> sems=semesterValues.getOrDefault(e.getKey(),List.of());
            double cgpa=sems.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            double sgpa=latestSgpa.getOrDefault(e.getKey(),0.0);
            result.add(new GpaSummary(e.getKey(),names.get(e.getKey()),Math.round(sgpa*100)/100.0,Math.round(cgpa*100)/100.0,e.getValue()));
        }
        return result;
    }

    // ─── module result config ──────────────────────────────────────────────────────────

    /** Returns the module result configuration for a course (CA/ESA weights and pass thresholds). */
    public MarksDAO.ModuleResultConfig getModuleResultConfig(String courseCode) throws SQLException {
        List<MarksDAO.ModuleResultConfig> configs = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT ca_weight,esa_weight,ca_pass,esa_pass FROM module_result_config WHERE course_code=?")) {
            ps.setString(1, courseCode);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    configs.add(new MarksDAO.ModuleResultConfig(
                            rs.getBigDecimal("ca_weight"),
                            rs.getBigDecimal("esa_weight"),
                            rs.getBigDecimal("ca_pass"),
                            rs.getBigDecimal("esa_pass")
                    ));
                }
            }
        }
        // If no course-specific config, return defaults.
        // FIX: previous defaults were (30, 70, 35, 40) -> caPass=35, esaPass=40.
        // The class javadoc and AcademicResultsDAO both use CA pass = 40, ESA pass = 35,
        // so the fallback now matches that policy.
        if (configs.isEmpty()) {
            return new MarksDAO.ModuleResultConfig(
                    new BigDecimal("30"),   // ca_weight
                    new BigDecimal("70"),   // esa_weight
                    new BigDecimal("40"),   // ca_pass   (CA eligibility threshold = 40%)
                    new BigDecimal("35")    // esa_pass
            );
        }
        return configs.get(0);
    }

    /** Returns courses assigned to the given lecturer (or all courses for ADMIN). */
    public List<AssignedCourse> getCoursesForLecturer(String lecturerUserId, boolean isAdmin) throws SQLException {
        List<AssignedCourse> out = new ArrayList<>();
        String sql = isAdmin
                ? "SELECT course_code, course_name FROM courses ORDER BY course_code"
                : "SELECT c.course_code, c.course_name FROM lecturer_course_assignments lca " +
                "JOIN courses c ON c.course_code = lca.course_code " +
                "WHERE lca.lecturer_user_id = ? ORDER BY c.course_code";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (!isAdmin) ps.setString(1, lecturerUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(new AssignedCourse(rs.getString("course_code"), rs.getString("course_name")));
            }
        }
        return out;
    }

    /** Returns all courses available (for student dropdowns). */
    public List<AssignedCourse> getAllCourses() throws SQLException {
        List<AssignedCourse> out = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT course_code, course_name FROM courses ORDER BY course_code")) {
            while (rs.next()) out.add(new AssignedCourse(rs.getString("course_code"), rs.getString("course_name")));
        }
        return out;
    }

    // ─── publish marks ────────────────────────────────────────────────────────

    /**
     * Publishes all marks for a given course (sets is_published = TRUE).
     *
     * NOTE: this method still targets the legacy {@code marks} / {@code assessments}
     * tables. Every other query in this file targets {@code academic_marks} /
     * {@code academic_assessments}. If you have fully migrated to the academic_*
     * tables, this method will throw "Table not found" at runtime. Publishing is
     * now handled by {@link AcademicResultsDAO#reviewPublishRequest}. Kept here
     * for backwards compatibility only.
     */
    public int publishMarks(String courseCode) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "UPDATE marks m JOIN assessments a ON a.assessment_id = m.assessment_id " +
                             "SET m.is_published = TRUE WHERE a.course_code = ?")) {
            ps.setString(1, courseCode);
            return ps.executeUpdate();
        }
    }

    // ─── final marks view ─────────────────────────────────────────────────────

    /**
     * Returns final examination marks (ESA component) for each student/course.
     * @param studentId  null = all.
     * @param courseCode null = all.
     */
    public List<CaMark> getFinalMarks(String studentId,String courseCode) throws SQLException {
        boolean fs=studentId!=null&&!studentId.isBlank(),fc=courseCode!=null&&!courseCode.isBlank();
        String sql="SELECT u.user_id,u.full_name,c.course_code,c.course_name,a.assessment_name,m.mark,a.weight_percentage,m.mark weighted "+
                "FROM academic_marks m JOIN academic_assessments a ON a.assessment_id=m.assessment_id JOIN users u ON u.user_id=m.student_user_id JOIN courses c ON c.course_code=a.course_code "+
                "WHERE a.component='ESA' "+(fs?"AND (u.user_id=? OR u.student_reg_no=?) ":"")+(fc?"AND a.course_code=? ":"")+"ORDER BY c.course_code,u.full_name";
        List<CaMark> out=new ArrayList<>();
        try(Connection conn=DBConnection.getConnection();PreparedStatement ps=conn.prepareStatement(sql)){
            int i=1;if(fs){ps.setString(i++,studentId);ps.setString(i++,studentId);}if(fc)ps.setString(i,courseCode);
            try(ResultSet rs=ps.executeQuery()){while(rs.next())out.add(new CaMark(rs.getString("user_id"),rs.getString("full_name"),rs.getString("course_code"),rs.getString("course_name"),rs.getString("assessment_name"),rs.getDouble("mark"),rs.getDouble("weight_percentage"),rs.getDouble("weighted")));}
        }return out;
    }
}