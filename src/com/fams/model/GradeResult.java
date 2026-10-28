package com.fams.model;

/** Immutable grade result for a final subject mark. */
public record GradeResult(double mark, String letterGrade, double gradePoint) { }
