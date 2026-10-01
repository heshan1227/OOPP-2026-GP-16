import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class DivisionCalculator extends JFrame {
    // GUI Components
    private JLabel lblNum1, lblNum2, lblResultHeading, lblResultValue;
    private JTextField txtNum1, txtNum2;
    private JButton btnDivide;

    public DivisionCalculator() {
        // Setup the frame
        setTitle("Division Calculator");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); // Using absolute positioning to match the layout exactly

        // Initialize and position labels
        lblNum1 = new JLabel("Number 1 :");
        lblNum1.setBounds(30, 40, 80, 25);
        add(lblNum1);

        lblNum2 = new JLabel("Number 2 :");
        lblNum2.setBounds(30, 75, 80, 25);
        add(lblNum2);

        lblResultHeading = new JLabel("Result");
        lblResultHeading.setBounds(230, 40, 80, 25);
        add(lblResultHeading);

        lblResultValue = new JLabel("");
        lblResultValue.setBounds(230, 75, 80, 25);
        add(lblResultValue);

        // Initialize and position text fields
        txtNum1 = new JTextField();
        txtNum1.setBounds(110, 40, 60, 25);
        add(txtNum1);

        txtNum2 = new JTextField();
        txtNum2.setBounds(110, 75, 60, 25);
        add(txtNum2);

        // Initialize and position button
        btnDivide = new JButton("Divide");
        btnDivide.setBounds(70, 130, 90, 30);
        add(btnDivide);

        // Action Listener for the Divide button
        btnDivide.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Parse values from text fields
                    double num1 = Double.parseDouble(txtNum1.getText());
                    double num2 = Double.parseDouble(txtNum2.getText());

                    // Check if second number is zero
                    if (num2 == 0) {
                        lblResultValue.setText(""); // Empty the result area (Figure B)
                        JOptionPane.showMessageDialog(
                                DivisionCalculator.this,
                                "You can't enter 0 for second number",
                                "Message",
                                JOptionPane.INFORMATION_MESSAGE
                        ); // Display error message (Figure C)
                    } else {
                        // Perform division and display result (Figure A)
                        double result = num1 / num2;
                        lblResultValue.setText(String.valueOf(result));
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(
                            DivisionCalculator.this,
                            "Please enter valid numeric values",
                            "Input Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });
    }

    public static void main(String[] args) {
        // Run the GUI application
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new DivisionCalculator().setVisible(true);
            }
        });
    }
}

