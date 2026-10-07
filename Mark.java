package model;

import util.InvalidMarkException;

public class Mark {
    private int markId;
    private int assessmentId;
    private int undergraduateId;
    private double markValue;
    private String enterDate;

    public Mark() {}

    public Mark(int markId, int assessmentId, int undergraduateId, double markValue, String enterDate) throws InvalidMarkException {
        this.markId = markId;
        this.assessmentId = assessmentId;
        this.undergraduateId = undergraduateId;
        setMarkValue(markValue); // Use setter for validation
        this.enterDate = enterDate;
    }

    public int getMarkId() { return markId; }
    public void setMarkId(int markId) { this.markId = markId; }

    public int getAssessmentId() { return assessmentId; }
    public void setAssessmentId(int assessmentId) { this.assessmentId = assessmentId; }

    public int getUndergraduateId() { return undergraduateId; }
    public void setUndergraduateId(int undergraduateId) { this.undergraduateId = undergraduateId; }

    public double getMarkValue() { return markValue; }
    
    // Encapsulation & Exception Handling: Validate mark range
    public void setMarkValue(double markValue) throws InvalidMarkException {
        if (markValue < 0 || markValue > 100) {
            throw new InvalidMarkException("Mark must be between 0 and 100. Provided: " + markValue);
        }
        this.markValue = markValue;
    }

    public String getEnterDate() { return enterDate; }
    public void setEnterDate(String enterDate) { this.enterDate = enterDate; }
}
