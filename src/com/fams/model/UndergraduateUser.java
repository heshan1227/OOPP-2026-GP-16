package com.fams.model;

public class UndergraduateUser extends User {
    private String studentRegNo;
    private String batch; // e.g. "2023/2024" or "Batch 16"
    private boolean isRepeat;
    private boolean isBatchMissed;

    public UndergraduateUser(String userId, String username, String password, String fullName, String email, String phone, String department, String studentRegNo, String batch, boolean isRepeat, boolean isBatchMissed) {
        super(userId, username, password, fullName, email, phone, department, Role.UNDERGRADUATE);
        this.studentRegNo = studentRegNo;
        this.batch = batch;
        this.isRepeat = isRepeat;
        this.isBatchMissed = isBatchMissed;
    }

    @Override
    public String getRoleSpecificDescription() {
        return "Undergraduate Student - Reg: " + studentRegNo + " | Batch: " + batch + (isRepeat ? " [Repeat]" : "") + (isBatchMissed ? " [Batch Missed]" : "");
    }

    @Override
    public boolean canManageAttendance() { return false; }

    @Override
    public boolean canManageMedicals() { return false; }

    @Override
    public boolean canManageNotices() { return false; }

    @Override
    public boolean canManageTimetables() { return false; }

    // Specific Getters & Setters
    public String getStudentRegNo() { return studentRegNo; }
    public void setStudentRegNo(String studentRegNo) { this.studentRegNo = studentRegNo; }

    public String getBatch() { return batch; }
    public void setBatch(String batch) { this.batch = batch; }

    public boolean isRepeat() { return isRepeat; }
    public void setRepeat(boolean repeat) { isRepeat = repeat; }

    public boolean isBatchMissed() { return isBatchMissed; }
    public void setBatchMissed(boolean batchMissed) { isBatchMissed = batchMissed; }
}
