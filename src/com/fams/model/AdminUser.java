package com.fams.model;

public class AdminUser extends User {

    public AdminUser(String userId, String username, String password, String fullName, String email, String phone, String department) {
        super(userId, username, password, fullName, email, phone, department, Role.ADMIN);
    }

    @Override
    public String getRoleSpecificDescription() {
        return "System Administrator - Full Access to User Profiles, Courses, Notices & Timetables";
    }

    @Override
    public boolean canManageAttendance() { return false; }

    @Override
    public boolean canManageMedicals() { return false; }

    @Override
    public boolean canManageNotices() { return true; }

    @Override
    public boolean canManageTimetables() { return true; }
}
