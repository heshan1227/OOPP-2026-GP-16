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