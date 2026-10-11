package gui;

import service.AuthService;
import model.User;

import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private final AuthService authService;

    public LoginFrame(AuthService authService) {
        this.authService = authService;
        initUI();
    }

    private void initUI() {
        setTitle("FAMS - Faculty Academic Management System | Login");
        setSize(420, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblHeader = new JLabel("FAMS Portal Login", SwingConstants.CENTER);
        lblHeader.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblHeader.setForeground(new Color(40, 60, 100));
        mainPanel.add(lblHeader, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username / ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        txtUsername = new JTextField(15);
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
        formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        txtPassword = new JPasswordField(15);
        formPanel.add(txtPassword, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnClear = new JButton("Clear");
        JButton btnLogin = new JButton("Login");

        btnLogin.setBackground(new Color(40, 110, 200));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);

        buttonPanel.add(btnClear);
        buttonPanel.add(btnLogin);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        btnLogin.addActionListener(e -> handleLogin());
        btnClear.addActionListener(e -> clearFields());
        getRootPane().setDefaultButton(btnLogin);
    }

    private void handleLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both username and password.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean authenticated = authService.login(username, password);

        if (authenticated) {
            User user = AuthService.getCurrentUser();
            JOptionPane.showMessageDialog(this,
                    "Login Successful! Welcome " + user.getName(),
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                new AdminDashboardFrame().setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Dashboard for role '" + user.getRole() + "' is managed by group members.",
                        "Information",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Invalid credentials. Please try again.",
                    "Authentication Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearFields() {
        txtUsername.setText("");
        txtPassword.setText("");
        txtUsername.requestFocus();
    }
}