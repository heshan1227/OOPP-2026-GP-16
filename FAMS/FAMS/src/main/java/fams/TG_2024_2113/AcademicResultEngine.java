package fams.TG_2024_2113;

import java.math.*;
import java.util.*;

/** Server-side academic rules. UI code must use this class rather than calculate marks itself. */
public final class AcademicResultEngine {
    private AcademicResultEngine() {}
    public enum Eligibility { ELIGIBLE, NOT_ELIGIBLE }
    public enum AttemptStatus { GRADED, E_CA, E_ESA, E_CA_ESA, N, W }
    public record ModuleMark(BigDecimal ca, BigDecimal esa, BigDecimal caWeight,
                             BigDecimal esaWeight, int credits, boolean gpaModule,
                             String completionCode, BigDecimal caPass, BigDecimal esaPass) {
        public ModuleMark(BigDecimal ca,BigDecimal esa,BigDecimal caWeight,BigDecimal esaWeight,
                          int credits,boolean gpaModule,String completionCode) {
            this(ca,esa,caWeight,esaWeight,credits,gpaModule,completionCode,
                 new BigDecimal("40"),new BigDecimal("35"));
        }
    }
    public record ModuleResult(BigDecimal finalMark, String grade, BigDecimal gp,
                               AttemptStatus status, boolean includedInGpa) {}
    public static BigDecimal money(BigDecimal n) { return n.setScale(2, RoundingMode.HALF_UP); }
    public static void validateMark(BigDecimal mark) {
        if (mark == null || mark.compareTo(BigDecimal.ZERO)<0 || mark.compareTo(new BigDecimal("100"))>0)
            throw new IllegalArgumentException("Marks must be between 0.00 and 100.00.");
    }
    public static ModuleResult calculate(ModuleMark m) {
        if (m.credits() <= 0) throw new IllegalArgumentException("Module credits must be greater than zero.");
        String code = m.completionCode()==null ? "" : m.completionCode().trim().toUpperCase(Locale.ROOT);
        if ("N".equals(code) || "W".equals(code))
            return new ModuleResult(null, code, BigDecimal.ZERO, "N".equals(code)?AttemptStatus.N:AttemptStatus.W, false);
        if (!code.isEmpty()) {
            if (!Set.of("E(CA&ESA)","E(CA)","E(ESA)").contains(code))
                throw new IllegalArgumentException("Unsupported completion code: "+code);
            return new ModuleResult(BigDecimal.ZERO, code, BigDecimal.ZERO,
                    "E(CA)".equals(code)?AttemptStatus.E_CA:"E(ESA)".equals(code)?AttemptStatus.E_ESA:AttemptStatus.E_CA_ESA,
                    m.gpaModule());
        }
        validateMark(m.ca()); validateMark(m.esa());
        if (m.ca().compareTo(m.caPass())<0)
            return new ModuleResult(BigDecimal.ZERO,"E(CA)",BigDecimal.ZERO,AttemptStatus.E_CA,m.gpaModule());
        if (m.esa().compareTo(m.esaPass())<0)
            return new ModuleResult(BigDecimal.ZERO,"E(ESA)",BigDecimal.ZERO,AttemptStatus.E_ESA,m.gpaModule());
        if (m.caWeight().add(m.esaWeight()).compareTo(new BigDecimal("100"))!=0)
            throw new IllegalArgumentException("CA and ESA weights must total 100%.");
        BigDecimal finalMark=money(m.ca().multiply(m.caWeight()).add(m.esa().multiply(m.esaWeight())).divide(new BigDecimal("100"),8,RoundingMode.HALF_UP));
        var grade=GPAManagement.calculate(finalMark.doubleValue());
        return new ModuleResult(finalMark,grade.letterGrade(),BigDecimal.valueOf(grade.gradePoint()).setScale(2,RoundingMode.HALF_UP),AttemptStatus.GRADED,m.gpaModule());
    }
    /** SGPA is credit weighted within a semester. N/W and non-GPA modules are excluded. */
    public static BigDecimal sgpa(Collection<ModuleResultWithCredit> modules) {
        BigDecimal points=BigDecimal.ZERO, credits=BigDecimal.ZERO;
        for(var x:modules) if(x.result().includedInGpa() && x.result().status()!=AttemptStatus.N && x.result().status()!=AttemptStatus.W) {
            points=points.add(x.result().gp().multiply(BigDecimal.valueOf(x.credits())));
            credits=credits.add(BigDecimal.valueOf(x.credits()));
        }
        return credits.signum()==0?new BigDecimal("0.00"):money(points.divide(credits,8,RoundingMode.HALF_UP));
    }
    /** PDF uniform semester contribution: calculate each semester SGPA, then average semester GPAs equally. */
    public static BigDecimal cgpa(Collection<BigDecimal> semesterSgpas) {
        if(semesterSgpas.isEmpty()) return new BigDecimal("0.00");
        BigDecimal sum=semesterSgpas.stream().reduce(BigDecimal.ZERO,BigDecimal::add);
        return money(sum.divide(BigDecimal.valueOf(semesterSgpas.size()),8,RoundingMode.HALF_UP));
    }
    public static Eligibility eligibility(BigDecimal cgpa, BigDecimal threshold) {
        return cgpa.compareTo(threshold)>=0?Eligibility.ELIGIBLE:Eligibility.NOT_ELIGIBLE;
    }
    public record ModuleResultWithCredit(ModuleResult result,int credits) {}
    /** Choose the best stored attempt by grade point, then final mark; never discard attempt history. */
    public static int bestAttempt(Map<Integer,BigDecimal> gradePoints,Map<Integer,BigDecimal> finalMarks) {
        return gradePoints.keySet().stream().max((a,b)->{
            int gp=gradePoints.get(a).compareTo(gradePoints.get(b));
            if(gp!=0)return gp;
            return finalMarks.getOrDefault(a,BigDecimal.ZERO).compareTo(finalMarks.getOrDefault(b,BigDecimal.ZERO));
        }).orElseThrow(()->new IllegalArgumentException("No attempts supplied."));
    }
    public static boolean canRegisterSemester5(BigDecimal firstThreeSemesterCgpa,BigDecimal threshold) {
        return firstThreeSemesterCgpa.compareTo(threshold)>=0;
    }
    public static String classAward(BigDecimal cgpa, boolean completedWithinFourYears) {
        if(!completedWithinFourYears) return "No Class";
        if(cgpa.compareTo(new BigDecimal("3.70"))>=0) return "First Class";
        if(cgpa.compareTo(new BigDecimal("3.30"))>=0) return "Second Upper";
        if(cgpa.compareTo(new BigDecimal("3.00"))>=0) return "Second Lower";
        if(cgpa.compareTo(new BigDecimal("2.00"))>=0) return "Pass";
        return "No Class";
    }
}
