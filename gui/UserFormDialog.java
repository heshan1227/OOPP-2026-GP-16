package gui;

import model.Admin;
import model.Undergraduate;
import model.User;

import javax.swing.*;
import java.awt.*;

public class UserFormDialog extends JDialog {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtName;
    private JTextField txtEmail;
    private JComboBox<String> cbRole;
    private boolean saved = false;
    private User user;

    public UserFormDialog(Frame parent, User userToEdit) {
        super(parent, userToEdit == null ? "Add New User" : "Edit User", true);
        this.user = userToEdit;
        initUI();
    }

    private void initUI() {
        setSize(380, 320);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtUsername = new JTextField(15);
        txtPassword = new JPasswordField(15);
        txtName = new JTextField(15);
        txtEmail = new JTextField(15);
        cbRole = new JComboBox<>(new String[]{"ADMIN", "UNDERGRADUATE", "LECTURER", "TECHNICAL_OFFICER"});

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; formPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; formPanel.add(txtPassword, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1; formPanel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; formPanel.add(txtEmail, gbc);

        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1; formPanel.add(cbRole, gbc);

        if (user != null) {
            txtUsername.setText(user.getUsername());
            txtUsername.setEnabled(false);
            txtPassword.setEnabled(false);
            txtName.setText(user.getName());
            txtEmail.setText(user.getEmail());
            cbRole.setSelectedItem(user.getRole());
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSave = new JButton("Save");
        JButton btnCancel = new JButton("Cancel");

        btnSave.addActionListener(e -> onSave());
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);

        add(formPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void onSave() {
        if (txtName.getText().trim().isEmpty() || txtEmail.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String role = (String) cbRole.getSelectedItem();
        if (user == null) {
            String pass = new String(txtPassword.getPassword()).trim();
            if (txtUsername.getText().trim().isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password are required for new users.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if ("ADMIN".equalsIgnoreCase(role)) {
                user = new Admin(0, txtUsername.getText().trim(), pass, txtName.getText().trim(), txtEmail.getText().trim(), 0, "ICT", "");
            } else {
                user = new Undergraduate(0, txtUsername.getText().trim(), pass, txtName.getText().trim(), txtEmail.getText().trim(), 0, txtUsername.getText().trim(), 1, "", "", "");
            }
        } else {
            user.setName(txtName.getText().trim());
            user.setEmail(txtEmail.getText().trim());
            user.setRole(role);
        }

        saved = true;
        dispose();
    }

    public boolean isSaved() {
        return saved;
    }

    public User getUser() {
        return user;
    }
}