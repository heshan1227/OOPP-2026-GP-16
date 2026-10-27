package com.fams.gui;

import com.fams.exception.ValidationException;
import com.fams.service.AuthService;
import com.fams.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblError;
    private final Runnable onLoginSuccess;

    public LoginFrame(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        setTitle("FAMS - Faculty Academic Management System | Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 650);
        setLocationRelativeTo(null);
        setResizable(false);

        initComponents();
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Theme.LIGHT_BG);

        // Header Panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Theme.DARK_BG);
        headerPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JLabel titleLabel = new JLabel("UNIVERSITY OF RUHUNA");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(148, 163, 184));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subTitleLabel = new JLabel("Faculty of Technology");
        subTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        subTitleLabel.setForeground(Color.WHITE);
        subTitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sysName = new JLabel("Academic Management System (FAMS)");
        sysName.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sysName.setForeground(Theme.PRIMARY);
        sysName.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(subTitleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        headerPanel.add(sysName);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formCard = Theme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;

        JLabel loginHeader = new JLabel("Account Login", SwingConstants.CENTER);
        loginHeader.setFont(Theme.FONT_TITLE);
        loginHeader.setForeground(Theme.TEXT_MAIN);
        formCard.add(loginHeader, gbc);

        gbc.gridy++;
        lblError = new JLabel("", SwingConstants.CENTER);
        lblError.setFont(Theme.FONT_SMALL);
        lblError.setForeground(Theme.DANGER);
        formCard.add(lblError, gbc);

        gbc.gridy++;
        formCard.add(new JLabel("Username:"), gbc);

        gbc.gridy++;
        txtUsername = new JTextField(20);
        txtUsername.setFont(Theme.FONT_REGULAR);
        txtUsername.setText("admin");
        formCard.add(txtUsername, gbc);

        gbc.gridy++;
        formCard.add(new JLabel("Password:"), gbc);

        gbc.gridy++;
        txtPassword = new JPasswordField(20);
        txtPassword.setFont(Theme.FONT_REGULAR);
        txtPassword.setText("admin123");
        formCard.add(txtPassword, gbc);

        gbc.gridy++;
        JButton btnLogin = Theme.createPrimaryButton("Login");
        btnLogin.setBackground(Color.BLUE);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.addActionListener(e -> performLogin());
        formCard.add(btnLogin, gbc);

        gbc.gridy++;
        JPanel quickBtnGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        quickBtnGrid.setBackground(Color.WHITE);

        formCard.add(quickBtnGrid, gbc);

        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerWrapper.setBackground(Theme.LIGHT_BG);
        centerWrapper.setBorder(new EmptyBorder(15, 15, 15, 15));
        centerWrapper.add(formCard);

        mainPanel.add(centerWrapper, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void setQuickLogin(String user, String pass) {
        txtUsername.setText(user);
        txtPassword.setText(pass);
        performLogin();
    }

    private void performLogin() {
        lblError.setText("");
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        try {
            User user = AuthService.getInstance().login(username, password);
            if (user != null) {
                dispose();
                onLoginSuccess.run();
            }
        } catch (ValidationException ve) {
            lblError.setText(ve.getMessage());
        }
    }
}
