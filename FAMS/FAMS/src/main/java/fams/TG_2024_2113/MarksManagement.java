package fams.TG_2024_2113;

import fams.TG_2024_2133.UserManagement;

import fams.TG_2024_2133.DBConnection;
import fams.TG_2024_2133.Role;
import fams.TG_2024_2133.User;
import fams.TG_2024_2133.UnauthorizedAccessException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/** Role-enforced facade for marks and results. All entry points apply the same server-side policy. */
public class MarksManagement {
    private final MarksDAO dao;
    public MarksManagement(){this(new MarksDAO());}
    public MarksManagement(MarksDAO dao){this.dao=dao;}
    private User actor() {
        User u=UserManagement.getInstance().getCurrentUser();
        if(u==null) throw new SecurityException("Please log in.");
        if(u.getRole()==Role.TECHNICAL_OFFICER) throw new SecurityException("Technical Officers do not have access to the results module.");
        return u;
    }
    private String scopedStudent(User u,String requested) {
        if(u.getRole()==Role.UNDERGRADUATE) {
            if(requested!=null&&!requested.isBlank()&&!requested.equals(u.getUserId()))
                throw new SecurityException("Students can only query their own records.");
            return u.getUserId();
        }
        return requested;
    }
    private void requireStaff(User u) {
        if(u.getRole()!=Role.ADMIN&&u.getRole()!=Role.LECTURER)
            throw new SecurityException("Only lecturers and administrators can access this results operation.");
    }
    private void checkLecturerCourse(User u,String course) throws SQLException {
        if(u.getRole()==Role.LECTURER) {
            boolean assigned=dao.getCoursesForLecturer(u.getUserId(),false).stream().anyMatch(c->c.courseCode().equals(course));
            if(!assigned) throw new SecurityException("You are not assigned to this course.");
        }
    }
    public List<MarksDAO.Eligibility> checkEligibility(String studentId)throws SQLException {
        User u=actor();requireStaff(u);return dao.getEligibility(studentId,null);
    }
    public List<MarksDAO.Eligibility> checkEligibility(String studentId,String courseCode)throws SQLException {
        User u=actor();requireStaff(u);checkLecturerCourse(u,courseCode);return dao.getEligibility(studentId,courseCode);
    }
    /**
     * Check CA eligibility for a student based on 30-35% CA marks threshold.
     * @param studentId student user ID
     * @param courseCode course code
     * @return eligibility status and CA percentage
     */
    public MarksDAO.Eligibility checkCaEligibility(String studentId,String courseCode)throws SQLException {
        User u=actor();requireStaff(u);checkLecturerCourse(u,courseCode);
        // Get the module result config for CA/ESA weights and pass thresholds
        var config = dao.getModuleResultConfig(courseCode);
        BigDecimal caWeight = config != null ? config.caWeight() : new BigDecimal("30");
        BigDecimal caPass = config != null ? config.caPass() : new BigDecimal("35");
        // Get all CA marks for this student in the course
        List<MarksDAO.CaMark> caMarks = dao.getCaMarks(studentId,courseCode);
        double totalWeightedCa = 0; double totalWeight = 0;
        for (MarksDAO.CaMark m : caMarks) {
            totalWeightedCa += m.weighted();
            totalWeight += m.weight();
        }
        double caPercentage = totalWeight > 0 ? totalWeightedCa / totalWeight : 0;
        String status = caPercentage >= caPass.doubleValue() ? "ELIGIBLE" : "NOT_ELIGIBLE";
        return new MarksDAO.Eligibility(studentId, studentId, courseCode,
            "CA Marks Summary", caPercentage, 0, 0, status);
    }
    /**
     * Get the module result config for a course.
     */
    public Optional<ModuleResultConfig> getModuleResultConfig(String courseCode)throws SQLException {
        // This would need a new record type or we can use the existing DAO method
        // For now, return configuration from the database
        try (Connection c = DBConnection.getConnection();
             PreparedStatement p = c.prepareStatement("SELECT ca_weight,esa_weight,ca_pass,esa_pass FROM module_result_config WHERE course_code=?")) {
            p.setString(1,courseCode);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    return Optional.of(new ModuleResultConfig(
                        r.getBigDecimal("ca_weight"),
                        r.getBigDecimal("esa_weight"),
                        r.getBigDecimal("ca_pass"),
                        r.getBigDecimal("esa_pass")
                    ));
                }
            }
        }
        return Optional.empty();
    }
    /** Data record for module result config. */
    public record ModuleResultConfig(BigDecimal caWeight, BigDecimal esaWeight, BigDecimal caPass, BigDecimal esaPass) {}
    // ... rest of the class unchanged
    public List<MarksDAO.CaMark> getCaMarks(String studentId,String courseCode)throws SQLException {
        User u=actor();requireStaff(u);checkLecturerCourse(u,courseCode);return dao.getCaMarks(studentId,courseCode);
    }
    public List<MarksDAO.CaMark> getFinalMarks(String studentId,String courseCode)throws SQLException {
        User u=actor();requireStaff(u);checkLecturerCourse(u,courseCode);return dao.getFinalMarks(studentId,courseCode);
    }
    public List<MarksDAO.GradeSummary> getGrades(String studentId,String courseCode)throws SQLException {
        User u=actor();String scoped=scopedStudent(u,studentId);
        if(u.getRole()==Role.LECTURER)checkLecturerCourse(u,courseCode);
        return dao.getGrades(scoped,courseCode);
    }
    public List<MarksDAO.GpaSummary> getGpaSummary(String studentId)throws SQLException {
        User u=actor();return dao.getGpaSummary(scopedStudent(u,studentId));
    }
    public List<MarksDAO.AssignedCourse> getCoursesForLecturer(String lecturerUserId,boolean isAdmin)throws SQLException {
        User u=actor();
        if(u.getRole()==Role.LECTURER)return dao.getCoursesForLecturer(u.getUserId(),false);
        requireStaff(u);return dao.getCoursesForLecturer(u.getUserId(),u.getRole()==Role.ADMIN);
    }
    public List<MarksDAO.AssignedCourse> getAllCourses()throws SQLException {
        User u=actor();requireStaff(u);return dao.getAllCourses();
    }
    public int publishMarks(String courseCode)throws SQLException {
        User u=actor();requireStaff(u);checkLecturerCourse(u,courseCode);
        throw new SecurityException("Direct publication is disabled. Submit a publish request and let Admin approve it.");
    }
}
