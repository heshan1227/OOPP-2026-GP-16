package com.fams.service;

import com.fams.database.TimetableDAO;
import com.fams.exception.UnauthorizedAccessException;
import com.fams.exception.ValidationException;
import com.fams.model.Role;
import com.fams.model.TimetableEntry;
import com.fams.model.User;
import java.util.List;

public class TimetableService {
    private final TimetableDAO timetableDAO = new TimetableDAO();

    private void validate(TimetableEntry e) throws ValidationException {
        if (e == null) throw new ValidationException("Timetable entry is required.");
        if (blank(e.getCourseCode())) throw new ValidationException("Course Code / Module Code is required.");
        if (blank(e.getCourseName())) throw new ValidationException("Course Name / Module Name is required.");
        if (blank(e.getDayOfWeek())) throw new ValidationException("Day is required.");
        if (blank(e.getStartTime()) || blank(e.getEndTime())) throw new ValidationException("Start Time and End Time are required.");
        if (blank(e.getVenue())) throw new ValidationException("Venue is required.");
        if (blank(e.getLecturerName())) throw new ValidationException("Lecturer is required.");
        if (blank(e.getDepartment())) throw new ValidationException("Department is required.");
        if (blank(e.getBatch())) throw new ValidationException("Batch is required.");
        if (blank(e.getAcademicPeriod())) throw new ValidationException("Academic Period is required.");
    }
    private boolean blank(String s){ return s==null || s.trim().isEmpty(); }

    public void addTimetableEntry(TimetableEntry entry) throws ValidationException, UnauthorizedAccessException {
        AuthService.getInstance().requireRole(Role.ADMIN); validate(entry); timetableDAO.saveTimetable(entry);
    }
    public void updateTimetableEntry(TimetableEntry entry) throws ValidationException, UnauthorizedAccessException {
        AuthService.getInstance().requireRole(Role.ADMIN); validate(entry); timetableDAO.updateTimetable(entry);
    }
    public void deleteTimetableEntry(int id) throws UnauthorizedAccessException {
        AuthService.getInstance().requireRole(Role.ADMIN); timetableDAO.deleteTimetable(id);
    }
    public List<TimetableEntry> getTimetablesForCurrentSession() {
        User u=AuthService.getInstance().getCurrentUser(); if(u==null) return List.of();
        if(u.getRole()==Role.TECHNICAL_OFFICER) return timetableDAO.getTimetablesByDepartment(u.getDepartment());
        return timetableDAO.getAllTimetables();
    }
}
