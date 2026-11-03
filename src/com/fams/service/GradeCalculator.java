package com.fams.service;

import com.fams.model.GradeResult;

/**
 * Centralized grade conversion. Thresholds are configuration points and MUST be checked
 * against the faculty's approved UGC Circular 12-2024 implementation before official use.
 */
public final class GradeCalculator {
    private GradeCalculator() { }
    public static GradeResult calculate(double mark) {
        if (Double.isNaN(mark) || mark < 0 || mark > 100) throw new IllegalArgumentException("Mark must be between 0 and 100.");
        if (mark >= 90) return new GradeResult(mark,"A+",4.0);
        if (mark >= 80) return new GradeResult(mark,"A",4.0);
        if (mark >= 75) return new GradeResult(mark,"A-",3.7);
        if (mark >= 70) return new GradeResult(mark,"B+",3.3);
        if (mark >= 65) return new GradeResult(mark,"B",3.0);
        if (mark >= 60) return new GradeResult(mark,"B-",2.7);
        if (mark >= 55) return new GradeResult(mark,"C+",2.3);
        if (mark >= 50) return new GradeResult(mark,"C",2.0);
        if (mark >= 45) return new GradeResult(mark,"C-",1.7);
        if (mark >= 40) return new GradeResult(mark,"D+",1.3);
        if (mark >= 35) return new GradeResult(mark,"D",1.0);
        return new GradeResult(mark,"E",0.0);
    }
    public static double sgpa(double[] gradePoints, int[] credits) {
        if (gradePoints.length != credits.length) throw new IllegalArgumentException("Grade points and credits must have the same length.");
        double points=0; int totalCredits=0;
        for(int i=0;i<credits.length;i++) { if(credits[i]<0) throw new IllegalArgumentException("Credits cannot be negative."); points += gradePoints[i]*credits[i]; totalCredits += credits[i]; }
        if(totalCredits==0) return 0.0;
        return Math.round(points/totalCredits*100.0)/100.0;
    }
}
