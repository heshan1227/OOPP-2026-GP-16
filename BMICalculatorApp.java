import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class BMICalculatorApp extends JFrame {

    private JRadioButton englishRadio;
    private JRadioButton metricRadio;

    private JTextField weightField;
    private JTextField heightField;

    private JComboBox<String> weightUnitCombo;
    private JComboBox<String> heightUnitCombo;

    private JLabel bmiValueLabel;
    private JLabel categoryLabel;

    private BMICalculator bmiCalculator;

    public BMICalculatorApp() {

        // ui
        bmiCalculator = new BMICalculatorImpl();

        setTitle("BMI Calculator");
        setSize(600, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        createGUI();
    }

    private void createGUI() {

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        mainPanel.setBackground(Color.WHITE);

        // title
        JLabel titleLabel = new JLabel("BMI Calculator");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 30));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(20));

        // select units
        JPanel unitPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        unitPanel.setBorder(
                BorderFactory.createTitledBorder("Select Unit System")
        );

        englishRadio = new JRadioButton("English System");
        metricRadio = new JRadioButton("Metric System");

        englishRadio.setSelected(true);

        ButtonGroup unitGroup = new ButtonGroup();
        unitGroup.add(englishRadio);
        unitGroup.add(metricRadio);

        unitPanel.add(englishRadio);
        unitPanel.add(metricRadio);

        mainPanel.add(unitPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        // weight
        JPanel weightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        weightPanel.add(new JLabel("Weight:"));
        weightField = new JTextField(12);

        weightUnitCombo = new JComboBox<>(new String[]{"lbs", "pounds"});

        weightPanel.add(weightField);
        weightPanel.add(weightUnitCombo);

        mainPanel.add(weightPanel);

        // height
        JPanel heightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        heightPanel.add(new JLabel("Height:"));
        heightField = new JTextField(12);

        heightUnitCombo = new JComboBox<>(new String[]{"inches", "feet"});

        heightPanel.add(heightField);
        heightPanel.add(heightUnitCombo);

        mainPanel.add(heightPanel);

        // eng or met
        englishRadio.addActionListener(e -> {
            weightUnitCombo.removeAllItems();
            weightUnitCombo.addItem("lbs");
            weightUnitCombo.addItem("pounds");

            heightUnitCombo.removeAllItems();
            heightUnitCombo.addItem("inches");
            heightUnitCombo.addItem("feet");
        });

        metricRadio.addActionListener(e -> {
            weightUnitCombo.removeAllItems();
            weightUnitCombo.addItem("kg");
            weightUnitCombo.addItem("g");

            heightUnitCombo.removeAllItems();
            heightUnitCombo.addItem("m");
            heightUnitCombo.addItem("cm");
        });

        // button
        JButton calculateButton = new JButton("Calculate BMI");
        calculateButton.setFont(new Font("Arial", Font.BOLD, 18));
        calculateButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        calculateButton.addActionListener(e -> calculateBMI());

        mainPanel.add(Box.createVerticalStrut(15));
        mainPanel.add(calculateButton);

        // result
        JPanel resultPanel = new JPanel();
        resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
        resultPanel.setBorder(BorderFactory.createTitledBorder("BMI Result"));

        bmiValueLabel = new JLabel("Your BMI: -");
        categoryLabel = new JLabel("Category: -");

        bmiValueLabel.setFont(new Font("Arial", Font.BOLD, 22));
        categoryLabel.setFont(new Font("Arial", Font.BOLD, 20));

        bmiValueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        categoryLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        resultPanel.add(Box.createVerticalStrut(10));
        resultPanel.add(bmiValueLabel);
        resultPanel.add(Box.createVerticalStrut(10));
        resultPanel.add(categoryLabel);
        resultPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(resultPanel);

        // ref table
        JPanel referencePanel = new JPanel(new BorderLayout());
        referencePanel.setBorder(BorderFactory.createTitledBorder("BMI Reference"));

        String[] columnNames = {"Category", "BMI Range"};
        String[][] data = {
                {"Underweight", "Less than 18.5"},
                {"Normal", "18.5 - 24.9"},
                {"Overweight", "25 - 29.9"},
                {"Obese", "30 or greater"}
        };

        JTable table = new JTable(data, columnNames);
        table.setRowHeight(30);

        referencePanel.add(new JScrollPane(table), BorderLayout.CENTER);

        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(referencePanel);

        // reset button
        JButton resetButton = new JButton("Clear");
        resetButton.setFont(new Font("Arial", Font.BOLD, 16));
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        resetButton.addActionListener(e -> resetForm());

        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(resetButton);

        add(mainPanel);
    }

    // calculation
    private void calculateBMI() {
        try {
            String weightText = weightField.getText().trim();
            String heightText = heightField.getText().trim();

            if (weightText.isEmpty() || heightText.isEmpty()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Please enter both weight and height.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            double inputWeight = Double.parseDouble(weightText);
            double inputHeight = Double.parseDouble(heightText);

            if (inputWeight <= 0 || inputHeight <= 0) {
                JOptionPane.showMessageDialog(
                        null,
                        "Weight and height must be greater than 0.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }

            double weightInKg = 0;
            double heightInMeters = 0;

            String selectedWeightUnit = (String) weightUnitCombo.getSelectedItem();
            String selectedHeightUnit = (String) heightUnitCombo.getSelectedItem();

            if (selectedWeightUnit != null) {
                switch (selectedWeightUnit) {
                    case "kg":
                        weightInKg = inputWeight;
                        break;
                    case "g":
                        weightInKg = inputWeight / 1000.0;
                        break;
                    case "lbs":
                    case "pounds":
                        weightInKg = inputWeight * 0.45359237;
                        break;
                }
            }

            if (selectedHeightUnit != null) {
                switch (selectedHeightUnit) {
                    case "m":
                    case "metres":
                        heightInMeters = inputHeight;
                        break;
                    case "cm":
                        heightInMeters = inputHeight / 100.0;
                        break;
                    case "inches":
                        heightInMeters = inputHeight * 0.0254;
                        break;
                    case "feet":
                        heightInMeters = inputHeight * 0.3048;
                        break;
                }
            }

            double bmi = weightInKg / (heightInMeters * heightInMeters);

            String category = bmiCalculator.getCategory(bmi);

            bmiValueLabel.setText(String.format("Your BMI: %.2f", bmi));
            categoryLabel.setText("Category: " + category);

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                    null,
                    "Please enter valid numbers.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage(),
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // reset
    private void resetForm() {
        weightField.setText("");
        heightField.setText("");
        englishRadio.setSelected(true);

        weightUnitCombo.removeAllItems();
        weightUnitCombo.addItem("lbs");
        weightUnitCombo.addItem("pounds");

        heightUnitCombo.removeAllItems();
        heightUnitCombo.addItem("inches");
        heightUnitCombo.addItem("feet");

        bmiValueLabel.setText("Your BMI: -");
        categoryLabel.setText("Category: -");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BMICalculatorApp app = new BMICalculatorApp();
            app.setVisible(true);
        });
    }
}
