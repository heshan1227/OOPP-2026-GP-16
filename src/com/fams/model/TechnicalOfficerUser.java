package com.fams.model;

public class TechnicalOfficerUser extends User {

    public TechnicalOfficerUser(String userId, String username, String password, String fullName, String email, String phone, String department) {
        super(userId, username, password, fullName, email, phone, department, Role.TECHNICAL_OFFICER);
    }

    @Override
    public String getRoleSpecificDescription() {
        return "Technical Officer - Add & Maintain Undergraduate Attendance & Medical Records";
    }

    @Override
    public boolean canManageAttendance() { return true; }

    @Override
    public boolean canManageMedicals() { return true; }

    @Override
    public boolean canManageNotices() { return false; }

    @Override
    public boolean canManageTimetables() { return false; } // Read-only for TO
}
