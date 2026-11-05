package com.fams.database;

import com.fams.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceDAO {

    public void saveOrUpdateAttendance(AttendanceRecord record) {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                String checkSql = "SELECT record_id FROM attendance_records WHERE student_reg_no = ? AND course_code = ? AND component_type = ? AND session_number = ?";
                try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                    checkStmt.setString(1, record.getStudentRegNo());
                    checkStmt.setString(2, record.getCourseCode());
                    checkStmt.setString(3, record.getComponentType().name());
                    checkStmt.setInt(4, record.getSessionNumber());

                    try (ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            // Update
                            String updateSql = "UPDATE attendance_records SET is_present = ?, session_date = ? WHERE record_id = ?";
                            try (PreparedStatement uStmt = conn.prepareStatement(updateSql)) {
                                uStmt.setBoolean(1, record.isPresent());
                                uStmt.setDate(2, Date.valueOf(record.getSessionDate()));
                                uStmt.setInt(3, rs.getInt("record_id"));
                                uStmt.executeUpdate();
                                return;
                            }
                        }
                    }
                }

                // Insert
                String insertSql = "INSERT INTO attendance_records (student_reg_no, student_name, course_code, component_type, session_number, session_date, is_present) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement iStmt = conn.prepareStatement(insertSql)) {
                    iStmt.setString(1, record.getStudentRegNo());
                    iStmt.setString(2, record.getStudentName());
                    iStmt.setString(3, record.getCourseCode());
                    iStmt.setString(4, record.getComponentType().name());
                    iStmt.setInt(5, record.getSessionNumber());
                    iStmt.setDate(6, Date.valueOf(record.getSessionDate()));
                    iStmt.setBoolean(7, record.isPresent());
                    iStmt.executeUpdate();
                    return;
                }
            }
        } catch (SQLException e) {
            // Fallback
        }

        // Memory Fallback
        DatabaseInitializer.initializeSampleData();
        boolean found = false;
        for (int i = 0; i < DatabaseInitializer.ATTENDANCE_RECORDS.size(); i++) {
            AttendanceRecord r = DatabaseInitializer.ATTENDANCE_RECORDS.get(i);
            if (r.getStudentRegNo().equalsIgnoreCase(record.getStudentRegNo()) &&
                r.getCourseCode().equalsIgnoreCase(record.getCourseCode()) &&
                r.getComponentType() == record.getComponentType() &&
                r.getSessionNumber() == record.getSessionNumber()) {
                
                r.setPresent(record.isPresent());
                r.setSessionDate(record.getSessionDate());
                found = true;
                break;
            }
        }

        if (!found) {
            record.setRecordId(DatabaseInitializer.ATTENDANCE_RECORDS.size() + 1);
            DatabaseInitializer.ATTENDANCE_RECORDS.add(record);
        }
    }


    /** Delete one attendance entry using its natural session key. */
    public boolean deleteAttendance(String regNo, String courseCode, ComponentType component, int sessionNumber) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                String sql = "DELETE FROM attendance_records WHERE student_reg_no=? AND course_code=? AND component_type=? AND session_number=?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, regNo); stmt.setString(2, courseCode);
                    stmt.setString(3, component.name()); stmt.setInt(4, sessionNumber);
                    return stmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) { /* use memory fallback */ }
        DatabaseInitializer.initializeSampleData();
        return DatabaseInitializer.ATTENDANCE_RECORDS.removeIf(r ->
                r.getStudentRegNo().equalsIgnoreCase(regNo) && r.getCourseCode().equalsIgnoreCase(courseCode)
                && r.getComponentType() == component && r.getSessionNumber() == sessionNumber);
    }

    public List<AttendanceRecord> getAttendanceByStudent(String regNo, String courseCode) {
        List<AttendanceRecord> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                String sql = "SELECT * FROM attendance_records WHERE student_reg_no = ? AND course_code = ? ORDER BY component_type, session_number";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, regNo);
                    stmt.setString(2, courseCode);
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            list.add(extractAttendance(rs));
                        }
                        return list;
                    }
                }
            }
        } catch (SQLException e) {
            // Fallback
        }

        DatabaseInitializer.initializeSampleData();
        for (AttendanceRecord r : DatabaseInitializer.ATTENDANCE_RECORDS) {
            if (r.getStudentRegNo().equalsIgnoreCase(regNo) && r.getCourseCode().equalsIgnoreCase(courseCode)) {
                list.add(r);
            }
        }
        return list;
    }

    public List<AttendanceRecord> getAllAttendanceRecords() {
        List<AttendanceRecord> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                String sql = "SELECT * FROM attendance_records ORDER BY course_code, student_reg_no, component_type, session_number";
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        list.add(extractAttendance(rs));
                    }
                    return list;
                }
            }
        } catch (SQLException e) {
            // Fallback
        }

        DatabaseInitializer.initializeSampleData();
        return new ArrayList<>(DatabaseInitializer.ATTENDANCE_RECORDS);
    }

    /** Returns only absent sessions for a student within an inclusive date range. */
    public List<AttendanceRecord> getAbsentAttendanceByStudent(String regNo, LocalDate from, LocalDate to) {
        List<AttendanceRecord> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                String sql = "SELECT * FROM attendance_records WHERE student_reg_no = ? AND is_present = FALSE AND session_date BETWEEN ? AND ? ORDER BY session_date, course_code";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, regNo);
                    stmt.setDate(2, Date.valueOf(from));
                    stmt.setDate(3, Date.valueOf(to));
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) list.add(extractAttendance(rs));
                        return list;
                    }
                }
            }
        } catch (SQLException e) { /* use in-memory sample data */ }

        DatabaseInitializer.initializeSampleData();
        for (AttendanceRecord r : DatabaseInitializer.ATTENDANCE_RECORDS) {
            if (r.getStudentRegNo().equalsIgnoreCase(regNo) && !r.isPresent()
                    && !r.getSessionDate().isBefore(from) && !r.getSessionDate().isAfter(to)) list.add(r);
        }
        list.sort((a, b) -> a.getSessionDate().compareTo(b.getSessionDate()));
        return list;
    }

    /**
     * Absent sessions of a student (inclusive date range) that can still be covered by a NEW medical.
     * Sessions that already have a pending or approved medical are hidden so the same absence
     * cannot be submitted twice.
     */
    public List<AttendanceRecord> getAbsencesAvailableForMedical(String regNo, LocalDate from, LocalDate to) {
        List<AttendanceRecord> result = new ArrayList<>();
        for (AttendanceRecord r : getAbsentAttendanceByStudent(regNo, from, to)) {
            if (!r.isHasMedicalSubmitted()) result.add(r);
        }
        return result;
    }

    /**
     * Keeps attendance in step with the medical workflow.
     * PENDING  -> submitted = true,  approved = false
     * APPROVED -> submitted = true,  approved = true
     * REJECTED -> submitted = false, approved = false (the absence goes back to a plain absence)
     */
    public void updateMedicalFlags(String regNo, String courseCode, ComponentType component,
                                   int sessionNumber, boolean submitted, boolean approved) {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                String sql = "UPDATE attendance_records SET has_medical_submitted = ?, is_medical_approved = ? " +
                        "WHERE student_reg_no = ? AND course_code = ? AND component_type = ? AND session_number = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setBoolean(1, submitted);
                    stmt.setBoolean(2, approved);
                    stmt.setString(3, regNo);
                    stmt.setString(4, courseCode);
                    stmt.setString(5, component.name());
                    stmt.setInt(6, sessionNumber);
                    stmt.executeUpdate();
                    return;
                }
            }
        } catch (SQLException e) {
            // Fallback to memory store below
        }

        DatabaseInitializer.initializeSampleData();
        for (AttendanceRecord r : DatabaseInitializer.ATTENDANCE_RECORDS) {
            if (r.getStudentRegNo().equalsIgnoreCase(regNo)
                    && r.getCourseCode().equalsIgnoreCase(courseCode)
                    && r.getComponentType() == component
                    && r.getSessionNumber() == sessionNumber) {
                r.setHasMedicalSubmitted(submitted);
                r.setMedicalApproved(approved);
                break;
            }
        }
    }

    private AttendanceRecord extractAttendance(ResultSet rs) throws SQLException {
        int id = rs.getInt("record_id");
        String regNo = rs.getString("student_reg_no");
        String name = rs.getString("student_name");
        String courseCode = rs.getString("course_code");
        ComponentType comp = ComponentType.valueOf(rs.getString("component_type"));
        int sessionNo = rs.getInt("session_number");
        LocalDate date = rs.getDate("session_date").toLocalDate();
        boolean isPresent = rs.getBoolean("is_present");
        boolean hasMed = rs.getBoolean("has_medical_submitted");
        boolean isMedApproved = rs.getBoolean("is_medical_approved");

        return new AttendanceRecord(id, regNo, name, courseCode, "Course " + courseCode, comp, sessionNo, date, isPresent, hasMed, isMedApproved);
    }
}
