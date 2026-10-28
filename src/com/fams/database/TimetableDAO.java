package com.fams.database;

import com.fams.model.ComponentType;
import com.fams.model.TimetableEntry;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimetableDAO {
    private void ensureAcademicPeriodColumn(Connection conn) {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("ALTER TABLE timetables ADD COLUMN academic_period VARCHAR(100) NOT NULL DEFAULT 'Semester 1 - 2026'");
        } catch (SQLException ignored) { }
    }

    public void saveTimetable(TimetableEntry entry) {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                ensureAcademicPeriodColumn(conn);
                String sql = "INSERT INTO timetables (course_code,course_name,component_type,day_of_week,start_time,end_time,venue,lecturer_name,department,batch,academic_period) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    setValues(ps, entry, false); ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) entry.setEntryId(rs.getInt(1)); }
                    return;
                }
            }
        } catch (SQLException ignored) { }
        DatabaseInitializer.initializeSampleData();
        entry.setEntryId(nextMemoryId());
        DatabaseInitializer.TIMETABLE_ENTRIES.add(entry);
    }

    public void updateTimetable(TimetableEntry entry) {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                ensureAcademicPeriodColumn(conn);
                String sql = "UPDATE timetables SET course_code=?,course_name=?,component_type=?,day_of_week=?,start_time=?,end_time=?,venue=?,lecturer_name=?,department=?,batch=?,academic_period=? WHERE timetable_id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    setValues(ps, entry, true); ps.executeUpdate(); return;
                }
            }
        } catch (SQLException ignored) { }
        DatabaseInitializer.initializeSampleData();
        for (TimetableEntry t : DatabaseInitializer.TIMETABLE_ENTRIES) {
            if (t.getEntryId() == entry.getEntryId()) { copy(entry, t); return; }
        }
    }

    public void deleteTimetable(int entryId) {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                try (PreparedStatement ps = conn.prepareStatement("DELETE FROM timetables WHERE timetable_id=?")) { ps.setInt(1, entryId); ps.executeUpdate(); return; }
            }
        } catch (SQLException ignored) { }
        DatabaseInitializer.initializeSampleData();
        DatabaseInitializer.TIMETABLE_ENTRIES.removeIf(t -> t.getEntryId() == entryId);
    }

    public List<TimetableEntry> getAllTimetables() {
        List<TimetableEntry> list = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            if (conn != null) {
                ensureAcademicPeriodColumn(conn);
                try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM timetables ORDER BY day_of_week,start_time")) {
                    while (rs.next()) list.add(extractTimetable(rs));
                    return list;
                }
            }
        } catch (SQLException ignored) { }
        DatabaseInitializer.initializeSampleData();
        return new ArrayList<>(DatabaseInitializer.TIMETABLE_ENTRIES);
    }

    public List<TimetableEntry> getTimetablesByDepartment(String dept) {
        List<TimetableEntry> result = new ArrayList<>();
        for (TimetableEntry t : getAllTimetables()) if (t.getDepartment().equalsIgnoreCase(dept)) result.add(t);
        return result;
    }

    private void setValues(PreparedStatement ps, TimetableEntry e, boolean update) throws SQLException {
        ps.setString(1,e.getCourseCode()); ps.setString(2,e.getCourseName()); ps.setString(3,e.getComponentType().name());
        ps.setString(4,e.getDayOfWeek()); ps.setTime(5, java.sql.Time.valueOf(parseTime(e.getStartTime()))); ps.setTime(6, java.sql.Time.valueOf(parseTime(e.getEndTime()))); ps.setString(7,e.getVenue());
        ps.setString(8,e.getLecturerName()); ps.setString(9,e.getDepartment()); ps.setString(10,e.getBatch()); ps.setString(11,e.getAcademicPeriod());
        if (update) ps.setInt(12,e.getEntryId());
    }

    private TimetableEntry extractTimetable(ResultSet rs) throws SQLException {
        String period;
        try { period = rs.getString("academic_period"); } catch (SQLException ex) { period = "Semester 1 - 2026"; }
        return new TimetableEntry(rs.getInt("timetable_id"), rs.getString("course_code"), rs.getString("course_name"),
                ComponentType.valueOf(rs.getString("component_type")), rs.getString("day_of_week"), formatTime(rs.getTime("start_time")),
                formatTime(rs.getTime("end_time")), rs.getString("venue"), rs.getString("lecturer_name"), rs.getString("department"), rs.getString("batch"), period);
    }

    private LocalTime parseTime(String value) {
        try { return LocalTime.parse(value.trim(), DateTimeFormatter.ofPattern("h:mm a", java.util.Locale.ENGLISH)); }
        catch (Exception ignored) { return LocalTime.parse(value.trim()); }
    }
    private String formatTime(Time value) {
        return value == null ? "" : value.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a", java.util.Locale.ENGLISH));
    }
    private int nextMemoryId() { int max=0; for(TimetableEntry t:DatabaseInitializer.TIMETABLE_ENTRIES) max=Math.max(max,t.getEntryId()); return max+1; }
    private void copy(TimetableEntry a, TimetableEntry b) {
        b.setCourseCode(a.getCourseCode()); b.setCourseName(a.getCourseName()); b.setComponentType(a.getComponentType()); b.setDayOfWeek(a.getDayOfWeek());
        b.setStartTime(a.getStartTime()); b.setEndTime(a.getEndTime()); b.setVenue(a.getVenue()); b.setLecturerName(a.getLecturerName());
        b.setDepartment(a.getDepartment()); b.setBatch(a.getBatch()); b.setAcademicPeriod(a.getAcademicPeriod());
    }
}
