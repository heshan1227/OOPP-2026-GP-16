package com.fams.database;

import com.fams.model.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MedicalDAO {

    public void saveMedical(MedicalRecord record) {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                String sql = "INSERT INTO medical_records (student_reg_no, student_name, course_code, component_type, session_number, absent_date, reference_no, reason, submission_date, status, reviewed_by, image_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, record.getStudentRegNo());
                    stmt.setString(2, record.getStudentName());
                    stmt.setString(3, record.getCourseCode());
                    stmt.setString(4, record.getComponentType().name());
                    stmt.setInt(5, record.getSessionNumber());
                    stmt.setDate(6, Date.valueOf(record.getAbsentDate()));
                    stmt.setString(7, record.getReferenceNo());
                    stmt.setString(8, record.getReason());
                    stmt.setDate(9, Date.valueOf(record.getSubmissionDate()));
                    stmt.setString(10, record.getStatus().name());
                    stmt.setString(11, record.getReviewedBy());
                    stmt.setString(12, record.getImagePath());
                    stmt.executeUpdate();

                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (rs.next()) {
                            record.setMedicalId(rs.getInt(1));
                        }
                    }
                    return;
                }
            }
        } catch (SQLException e) {
            // Fallback
        }

        DatabaseInitializer.initializeSampleData();
        record.setMedicalId(DatabaseInitializer.MEDICAL_RECORDS.size() + 1);
        DatabaseInitializer.MEDICAL_RECORDS.add(record);
    }

    public void updateMedicalStatus(int medicalId, MedicalStatus newStatus, String reviewedBy) {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                String sql = "UPDATE medical_records SET status = ?, reviewed_by = ? WHERE medical_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, newStatus.name());
                    stmt.setString(2, reviewedBy);
                    stmt.setInt(3, medicalId);
                    stmt.executeUpdate();
                    return;
                }
            }
        } catch (SQLException e) {
            // Fallback
        }

        DatabaseInitializer.initializeSampleData();
        for (MedicalRecord r : DatabaseInitializer.MEDICAL_RECORDS) {
            if (r.getMedicalId() == medicalId) {
                r.setStatus(newStatus);
                r.setReviewedBy(reviewedBy);
                break;
            }
        }
    }

    public List<MedicalRecord> getAllMedicals() {
        List<MedicalRecord> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                String sql = "SELECT * FROM medical_records ORDER BY submission_date DESC";
                try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                    while (rs.next()) {
                        list.add(extractMedical(rs));
                    }
                    return list;
                }
            }
        } catch (SQLException e) {
            // Fallback
        }

        DatabaseInitializer.initializeSampleData();
        return new ArrayList<>(DatabaseInitializer.MEDICAL_RECORDS);
    }

    public List<MedicalRecord> getMedicalsByStudent(String regNo) {
        List<MedicalRecord> list = new ArrayList<>();
        for (MedicalRecord m : getAllMedicals()) {
            if (m.getStudentRegNo().equalsIgnoreCase(regNo)) {
                list.add(m);
            }
        }
        return list;
    }

    private MedicalRecord extractMedical(ResultSet rs) throws SQLException {
        int id = rs.getInt("medical_id");
        String regNo = rs.getString("student_reg_no");
        String name = rs.getString("student_name");
        String courseCode = rs.getString("course_code");
        ComponentType comp = ComponentType.valueOf(rs.getString("component_type"));
        int sessionNo = rs.getInt("session_number");
        LocalDate absentDate = rs.getDate("absent_date").toLocalDate();
        String ref = rs.getString("reference_no");
        String reason = rs.getString("reason");
        LocalDate subDate = rs.getDate("submission_date").toLocalDate();
        MedicalStatus status = MedicalStatus.valueOf(rs.getString("status"));
        String reviewer = rs.getString("reviewed_by");
        String imgPath = rs.getString("image_path");
        if (imgPath == null || imgPath.isEmpty()) imgPath = "";

        return new MedicalRecord(id, regNo, name, courseCode, comp, sessionNo, absentDate, ref, reason, subDate, status, reviewer, imgPath);
    }
}
