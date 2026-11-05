package fams.TG_2024_2113;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/** PDF-aligned grade mapping. All final mark and GPA rounding is HALF_UP to 2 decimals. */
public final class GPAManagement {
    private GPAManagement() {}
    public record GradeBand(String grade, BigDecimal minimum, BigDecimal gradePoint) {}
    public static BigDecimal round2(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
    public static GradeResult calculate(double mark) {
        return calculate(mark,defaultBands());
    }
    public static List<GradeBand> defaultBands() {
        return List.of(
            new GradeBand("A+",new BigDecimal("85"),new BigDecimal("4.00")),
            new GradeBand("A",new BigDecimal("75"),new BigDecimal("4.00")),
            new GradeBand("A-",new BigDecimal("70"),new BigDecimal("3.70")),
            new GradeBand("B+",new BigDecimal("65"),new BigDecimal("3.30")),
            new GradeBand("B",new BigDecimal("60"),new BigDecimal("3.00")),
            new GradeBand("B-",new BigDecimal("55"),new BigDecimal("2.70")),
            new GradeBand("C+",new BigDecimal("50"),new BigDecimal("2.30")),
            new GradeBand("C",new BigDecimal("45"),new BigDecimal("2.00")),
            new GradeBand("C-",new BigDecimal("40"),new BigDecimal("1.70")),
            new GradeBand("D",new BigDecimal("35"),new BigDecimal("1.30")),
            new GradeBand("E",BigDecimal.ZERO,BigDecimal.ZERO));
    }
    public static GradeResult calculate(double mark,List<GradeBand> bands) {
        if (!Double.isFinite(mark) || mark < 0 || mark > 100)
            throw new IllegalArgumentException("Mark must be between 0 and 100.");
        BigDecimal m=round2(BigDecimal.valueOf(mark));
        List<GradeBand> ordered=new ArrayList<>(bands);
        ordered.sort(Comparator.comparing(GradeBand::minimum).reversed());
        for(GradeBand b:ordered) if(m.compareTo(b.minimum())>=0)
            return new GradeResult(m.doubleValue(),b.grade(),b.gradePoint().doubleValue());
        throw new IllegalArgumentException("Grade boundaries must include a zero-point E band.");
    }
    public static double sgpa(double[] gradePoints, int[] credits) {
        if (gradePoints.length != credits.length) throw new IllegalArgumentException("Grade points and credits must have the same length.");
        BigDecimal points=BigDecimal.ZERO; int total=0;
        for(int i=0;i<credits.length;i++) {
            if(credits[i] < 0) throw new IllegalArgumentException("Credits cannot be negative.");
            points=points.add(BigDecimal.valueOf(gradePoints[i]).multiply(BigDecimal.valueOf(credits[i])));
            total+=credits[i];
        }
        return total==0 ? 0.0 : round2(points.divide(BigDecimal.valueOf(total), 8, RoundingMode.HALF_UP)).doubleValue();
    }
}
