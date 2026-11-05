package com.fams.service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AcademicResultEngineTest {
    @Test void gradeBoundaryTests() {
        assertEquals("E",GradeCalculator.calculate(34.99).letterGrade());
        assertEquals("D",GradeCalculator.calculate(35.00).letterGrade());
        assertEquals("D",GradeCalculator.calculate(39.99).letterGrade());
        assertEquals("C-",GradeCalculator.calculate(40.00).letterGrade());
        assertEquals("A",GradeCalculator.calculate(84.99).letterGrade());
        assertEquals("A+",GradeCalculator.calculate(85.00).letterGrade());
    }
    @Test void everyGradeBoundaryMapsToExpectedGrade() {
        double[] marks={85,75,70,65,60,55,50,45,40,35,34.99};
        String[] grades={"A+","A","A-","B+","B","B-","C+","C","C-","D","E"};
        for(int i=0;i<marks.length;i++) assertEquals(grades[i],GradeCalculator.calculate(marks[i]).letterGrade());
    }
    @Test void halfUpRounding() {
        assertEquals(59.90, GradeCalculator.calculate(59.895).mark(),0.0001);
        assertEquals(2.93,GradeCalculator.sgpa(new double[]{2.7,2.7,3.3},new int[]{3,2,3}),0.0001);
    }
    @Test void caAndEsaMinimumsAreBothRequired() {
        var caFail=AcademicResultEngine.calculate(new AcademicResultEngine.ModuleMark(
            new BigDecimal("39.99"),new BigDecimal("90"),new BigDecimal("30"),new BigDecimal("70"),3,true,""));
        assertEquals("E(CA)",caFail.grade());
        var esaFail=AcademicResultEngine.calculate(new AcademicResultEngine.ModuleMark(
            new BigDecimal("60"),new BigDecimal("34.99"),new BigDecimal("30"),new BigDecimal("70"),3,true,""));
        assertEquals("E(ESA)",esaFail.grade());
    }
    @Test void moduleSpecificPassThresholdsAreApplied() {
        var result=AcademicResultEngine.calculate(new AcademicResultEngine.ModuleMark(
            new BigDecimal("44"),new BigDecimal("36"),new BigDecimal("30"),new BigDecimal("70"),3,true,"",
            new BigDecimal("45"),new BigDecimal("35")));
        assertEquals("E(CA)",result.grade());
    }
    @Test void nWithdrawnAndNonGpaAreExcluded() {
        var n=AcademicResultEngine.calculate(new AcademicResultEngine.ModuleMark(null,null,new BigDecimal("30"),new BigDecimal("70"),3,true,"N"));
        var w=AcademicResultEngine.calculate(new AcademicResultEngine.ModuleMark(null,null,new BigDecimal("30"),new BigDecimal("70"),3,true,"W"));
        var english=AcademicResultEngine.calculate(new AcademicResultEngine.ModuleMark(new BigDecimal("80"),new BigDecimal("80"),new BigDecimal("100"),new BigDecimal("0"),3,false,""));
        assertFalse(n.includedInGpa()); assertFalse(w.includedInGpa()); assertFalse(english.includedInGpa());
    }
    @Test void bestGradeAttemptPolicyCanBeApplied() {
        var oldAttempt=GradeCalculator.calculate(59).gradePoint();
        var improved=GradeCalculator.calculate(72).gradePoint();
        assertTrue(improved>oldAttempt);
        // Persistence stores each attempt separately; result selection chooses the highest GP.
        assertEquals(3.7,Math.max(oldAttempt,improved),0.001);
    }
    @Test void bestAttemptSelectsHighestGradeAndRetainsAttemptRows() {
        var points=java.util.Map.of(1,new BigDecimal("2.70"),2,new BigDecimal("3.70"));
        var marks=java.util.Map.of(1,new BigDecimal("59.00"),2,new BigDecimal("72.00"));
        assertEquals(2,AcademicResultEngine.bestAttempt(points,marks));
    }
    @Test void eligibilityThresholdIsInclusive() {
        assertEquals(AcademicResultEngine.Eligibility.ELIGIBLE,AcademicResultEngine.eligibility(new BigDecimal("2.00"),new BigDecimal("2.00")));
        assertEquals(AcademicResultEngine.Eligibility.NOT_ELIGIBLE,AcademicResultEngine.eligibility(new BigDecimal("1.99"),new BigDecimal("2.00")));
    }
    @Test void cgpaUsesUniformSemesterWeightingNotRawCreditLump() {
        BigDecimal s1=new BigDecimal("2.93"); // 8 credits
        BigDecimal s2=new BigDecimal("4.00"); // 12 credits
        assertEquals(new BigDecimal("3.47"),AcademicResultEngine.cgpa(List.of(s1,s2)));
        BigDecimal rawCreditWeighted=s1.multiply(new BigDecimal("8")).add(s2.multiply(new BigDecimal("12"))).divide(new BigDecimal("20"),2,java.math.RoundingMode.HALF_UP);
        assertNotEquals(rawCreditWeighted,AcademicResultEngine.cgpa(List.of(s1,s2)));
    }
    @Test void fifthSemesterGateUsesFirstThreeSemesterCgpaThreshold() {
        assertTrue(AcademicResultEngine.canRegisterSemester5(new BigDecimal("2.00"),new BigDecimal("2.00")));
        assertFalse(AcademicResultEngine.canRegisterSemester5(new BigDecimal("1.99"),new BigDecimal("2.00")));
    }
    @Test void classCutoffsRequireCompletionWithinFourYears() {
        assertEquals("First Class",AcademicResultEngine.classAward(new BigDecimal("3.70"),true));
        assertEquals("No Class",AcademicResultEngine.classAward(new BigDecimal("3.90"),false));
    }
}
