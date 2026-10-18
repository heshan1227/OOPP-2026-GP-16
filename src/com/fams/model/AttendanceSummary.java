package com.fams.model;

public class AttendanceSummary {
    private String studentRegNo;
    private String studentName;
    private String courseCode;
    private String batch;

    private int theoryAttended;
    private int totalTheorySessions; // default 15

    private int practicalAttended;
    private int totalPracticalSessions; // default 15

    private int approvedMedicalsCount;
    private String scenarioDescription;

    public AttendanceSummary(String studentRegNo, String studentName, String courseCode, String batch, int theoryAttended, int totalTheorySessions, int practicalAttended, int totalPracticalSessions, int approvedMedicalsCount, String scenarioDescription) {
        this.studentRegNo = studentRegNo;
        this.studentName = studentName;
        this.courseCode = courseCode;
        this.batch = batch;
        this.theoryAttended = theoryAttended;
        this.totalTheorySessions = totalTheorySessions;
        this.practicalAttended = practicalAttended;
        this.totalPracticalSessions = totalPracticalSessions;
        this.approvedMedicalsCount = approvedMedicalsCount;
        this.scenarioDescription = scenarioDescription;
    }

    public String getStudentRegNo() { return studentRegNo; }
    public String getStudentName() { return studentName; }
    public String getCourseCode() { return courseCode; }
    public String getBatch() { return batch; }

    public int getTheoryAttended() { return theoryAttended; }
    public int getTotalTheorySessions() { return totalTheorySessions; }

    public int getPracticalAttended() { return practicalAttended; }
    public int getTotalPracticalSessions() { return totalPracticalSessions; }

    public int getApprovedMedicalsCount() { return approvedMedicalsCount; }
    public String getScenarioDescription() { return scenarioDescription; }

    public double getTheoryPercentage() {
        if (totalTheorySessions == 0) return 0.0;
        return (double) theoryAttended / totalTheorySessions * 100.0;
    }

    public double getPracticalPercentage() {
        if (totalPracticalSessions == 0) return 0.0;
        return (double) practicalAttended / totalPracticalSessions * 100.0;
    }

    public int getTotalAttended() {
        return theoryAttended + practicalAttended;
    }

    public int getTotalSessions() {
        return totalTheorySessions + totalPracticalSessions;
    }

    public double getCombinedPercentage() {
        int total = getTotalSessions();
        if (total == 0) return 0.0;
        return (double) getTotalAttended() / total * 100.0;
    }

    public boolean is80PercentEligible() {
        return getCombinedPercentage() >= 80.0;
    }

    public String getEligibilityStatus() {
        double pct = getCombinedPercentage();
        if (pct >= 80.0) {
            return "Eligible (" + String.format("%.1f", pct) + "%)";
        } else if (approvedMedicalsCount > 0) {
            return "Medical Pending Review (" + String.format("%.1f", pct) + "% + " + approvedMedicalsCount + " Meds)";
        } else {
            return "Not Eligible (" + String.format("%.1f", pct) + "%)";
        }
    }
}
