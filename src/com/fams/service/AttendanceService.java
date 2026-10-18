package com.fams.service;

import com.fams.database.AttendanceDAO;
import com.fams.database.MedicalDAO;
import com.fams.database.UserDAO;
import com.fams.exception.UnauthorizedAccessException;
import com.fams.exception.ValidationException;
import com.fams.model.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Attendance Service demonstrating Polymorphism (Method Overloading)
 * and Business logic for Theory, Practical, Combined calculations and 80% Eligibility scenarios.
 */
public class AttendanceService {
    private final AttendanceDAO attendanceDAO;
    private final MedicalDAO medicalDAO;
    private final UserDAO userDAO;

    public AttendanceService() {
        this.attendanceDAO = new AttendanceDAO();
        this.medicalDAO = new MedicalDAO();
        this.userDAO = new UserDAO();
    }

    /**
     * Technical Officer functionality to record/update attendance.
     */
    public void recordAttendance(AttendanceRecord record) throws ValidationException, UnauthorizedAccessException {
        AuthService.getInstance().requireRole(Role.TECHNICAL_OFFICER, Role.ADMIN);

        if (record.getStudentRegNo() == null || record.getStudentRegNo().trim().isEmpty()) {
            throw new ValidationException("Student Registration Number is required.");
        }
        if (record.getCourseCode() == null || record.getCourseCode().trim().isEmpty()) {
            throw new ValidationException("Course code is required.");
        }
        if (record.getSessionNumber() < 1 || record.getSessionNumber() > 15) {
            throw new ValidationException("Session number must be between 1 and 15.");
        }

        attendanceDAO.saveOrUpdateAttendance(record);
    }

    // --- POLYMORPHISM: METHOD OVERLOADING FOR ATTENDANCE CALCULATIONS ---

    /**
     * Overloaded Method 1: Calculate attendance percentage for a SPECIFIC COMPONENT (Theory or Practical)
     */
    public double calculateAttendance(String studentRegNo, String courseCode, ComponentType componentType) {
        List<AttendanceRecord> records = attendanceDAO.getAttendanceByStudent(studentRegNo, courseCode);
        int attended = 0;
        int total = 0;

        for (AttendanceRecord r : records) {
            if (r.getComponentType() == componentType) {
                total++;
                if (r.isPresent()) {
                    attended++;
                }
            }
        }

        if (total == 0) return 0.0;
        return (double) attended / total * 100.0;
    }

    /**
     * Overloaded Method 2: Calculate COMBINED attendance percentage for an Undergraduate across both Theory and Practical
     */
    public double calculateAttendance(String studentRegNo, String courseCode) {
        List<AttendanceRecord> records = attendanceDAO.getAttendanceByStudent(studentRegNo, courseCode);
        int attended = 0;
        int total = records.size();

        for (AttendanceRecord r : records) {
            if (r.isPresent()) {
                attended++;
            }
        }

        if (total == 0) return 0.0;
        return (double) attended / total * 100.0;
    }

    /**
     * Generate detailed AttendanceSummary object for an individual student.
     */
    public AttendanceSummary generateIndividualSummary(String studentRegNo, String courseCode) {
        List<AttendanceRecord> records = attendanceDAO.getAttendanceByStudent(studentRegNo, courseCode);
        List<MedicalRecord> medicals = medicalDAO.getMedicalsByStudent(studentRegNo);

        int theoryAttended = 0;
        int totalTheory = 15; // standard 15 sessions
        int practicalAttended = 0;
        int totalPractical = 15; // standard 15 sessions

        for (AttendanceRecord r : records) {
            if (r.getComponentType() == ComponentType.THEORY && r.isPresent()) {
                theoryAttended++;
            } else if (r.getComponentType() == ComponentType.PRACTICAL && r.isPresent()) {
                practicalAttended++;
            }
        }

        int approvedMeds = 0;
        for (MedicalRecord m : medicals) {
            if (m.getCourseCode().equalsIgnoreCase(courseCode) && m.getStatus() == MedicalStatus.APPROVED) {
                approvedMeds++;
            }
        }

        UndergraduateUser ug = userDAO.findUndergraduateByRegNo(studentRegNo);
        String name = (ug != null) ? ug.getFullName() : studentRegNo;
        String batch = (ug != null) ? ug.getBatch() : "Batch 16";

        double combinedPct = (double) (theoryAttended + practicalAttended) / (totalTheory + totalPractical) * 100.0;

        String scenarioNote;
        if (combinedPct > 80.0 && approvedMeds == 0) {
            scenarioNote = "Scenario 1: > 80% Attendance without medicals";
        } else if (Math.abs(combinedPct - 80.0) < 0.1 && approvedMeds == 0) {
            scenarioNote = "Scenario 2: Exactly 80% Attendance";
        } else if (combinedPct < 80.0 && approvedMeds == 0) {
            scenarioNote = "Scenario 3: < 80% Attendance without medicals [Ineligible]";
        } else if (combinedPct > 80.0 && approvedMeds > 0) {
            scenarioNote = "Scenario 4: > 80% Attendance with medicals";
        } else {
            scenarioNote = "Scenario 5: < 80% Attendance with " + approvedMeds + " Approved Medicals";
        }

        return new AttendanceSummary(studentRegNo, name, courseCode, batch, theoryAttended, totalTheory, practicalAttended, totalPractical, approvedMeds, scenarioNote);
    }

    /**
     * Generate Attendance Summaries for the entire Batch.
     */
    public List<AttendanceSummary> generateBatchSummary(String courseCode) {
        List<User> allUsers = userDAO.getAllUsers();
        List<AttendanceSummary> batchSummaries = new ArrayList<>();

        for (User u : allUsers) {
            if (u instanceof UndergraduateUser) {
                UndergraduateUser ug = (UndergraduateUser) u;
                batchSummaries.add(generateIndividualSummary(ug.getStudentRegNo(), courseCode));
            }
        }
        return batchSummaries;
    }
}
