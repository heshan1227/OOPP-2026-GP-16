package com.fams.model;

public class LecturerUser extends User {

    public LecturerUser(String userId, String username, String password, String fullName, String email, String phone, String department) {
        super(userId, username, password, fullName, email, phone, department, Role.LECTURER);
    }

    @Override
    public String getRoleSpecificDescription() {
        return "Lecturer - View Student Attendance, Medicals, Notices & Manage Exam Marks";
    }

    @Override
    public boolean canManageAttendance() { return false; } // Read-only for Lecturer

    @Override
    public boolean canManageMedicals() { return false; } // Read-only for Lecturer

    @Override
    public boolean canManageNotices() { return false; }

    @Override
    public boolean canManageTimetables() { return false; }
}
