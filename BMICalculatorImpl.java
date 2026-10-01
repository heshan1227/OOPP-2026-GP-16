public class BMICalculatorImpl implements BMICalculator {

    @Override
    public double calculateBMI(double weight, double height) {

        if (weight <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "Weight and height must be greater than 0."
            );
        }

        return weight / (height * height);
    }

    @Override
    public String getCategory(double bmi) {

        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi < 25) {
            return "Normal";
        } else if (bmi < 30) {
            return "Overweight";
        } else {
            return "Obese";
        }
    }
}