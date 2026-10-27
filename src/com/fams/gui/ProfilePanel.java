package com.fams.gui;

import com.fams.database.DBConnection;
import com.fams.database.DatabaseInitializer;
import com.fams.model.Role;
import com.fams.model.UndergraduateUser;
import com.fams.model.User;
import com.fams.service.AuthService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Role-aware Profile Screen with Profile Photo Upload and Student Enrolled Course List.
 */
public class ProfilePanel extends JPanel {
    private final User user = AuthService.getInstance().getCurrentUser();
    private final Runnable onProfileUpdated;

    private final JTextField txtName = new JTextField(25);
    private final JTextField txtEmail = new JTextField(25);
    private final JTextField txtPhone = new JTextField(25);
    private final JTextField txtDepartment = new JTextField(25);
    private final JTextField txtPicturePath = new JTextField(25);

    private CircularAvatar avatarPreview;

    public ProfilePanel() {
        this(null);
    }

    public ProfilePanel(Runnable onProfileUpdated) {
        this.onProfileUpdated = onProfileUpdated;

        setLayout(new BorderLayout(15, 15));
        setBackground(Theme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        if (user != null) {
            initUI();
        }
    }

    private void initUI() {
        // --- HEADER ---
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setOpaque(false);

        JLabel lblTitle = Theme.createHeaderLabel("My Profile");
        lblTitle.setFont(Theme.FONT_TITLE);
        topHeader.add(lblTitle, BorderLayout.WEST);

        add(topHeader, BorderLayout.NORTH);

        // Main vertical content
        JPanel contentContainer = new JPanel();
        contentContainer.setLayout(new BoxLayout(contentContainer, BoxLayout.Y_AXIS));
        contentContainer.setOpaque(false);

        // --- PROFILE DETAILS & PHOTO CARD ---
        JPanel profileCard = Theme.createCardPanel();
        profileCard.setLayout(new BorderLayout(20, 15));

        // Left Avatar & Upload Section
        JPanel avatarSection = new JPanel();
        avatarSection.setLayout(new BoxLayout(avatarSection, BoxLayout.Y_AXIS));
        avatarSection.setOpaque(false);
        avatarSection.setBorder(new EmptyBorder(10, 15, 10, 25));

        avatarPreview = new CircularAvatar(100, user.getProfilePictureUrl(), user.getFullName());
        avatarPreview.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatarSection.add(avatarPreview);
        avatarSection.add(Box.createVerticalStrut(12));

        JButton btnUploadPhoto = Theme.createSecondaryButton("Upload Photo");
        btnUploadPhoto.setFont(Theme.FONT_SMALL);
        btnUploadPhoto.setPreferredSize(new Dimension(140, 36));
        btnUploadPhoto.setMaximumSize(new Dimension(140, 36));
        btnUploadPhoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnUploadPhoto.addActionListener(e -> chooseAndUploadPhoto());
        avatarSection.add(btnUploadPhoto);

        avatarSection.add(Box.createVerticalStrut(8));
        JButton btnResetPhoto = new JButton("Reset Default");
        btnResetPhoto.setFont(Theme.FONT_SMALL);
        btnResetPhoto.setFocusPainted(false);
        btnResetPhoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnResetPhoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnResetPhoto.addActionListener(e -> {
            txtPicturePath.setText("default_avatar.png");
            avatarPreview.setImagePath("default_avatar.png");
        });
        avatarSection.add(btnResetPhoto);

        profileCard.add(avatarSection, BorderLayout.WEST);

        // Right Form Section
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;

        addRow(form, c, "Username (cannot change)", new JLabel(user.getUsername()));

        boolean student = user.getRole() == Role.UNDERGRADUATE;

        txtName.setText(user.getFullName());
        txtEmail.setText(nvl(user.getEmail()));
        txtPhone.setText(nvl(user.getPhone()));
        txtDepartment.setText(nvl(user.getDepartment()));
        txtPicturePath.setText(nvl(user.getProfilePictureUrl()));

        if (!student) {
            addRow(form, c, "Full name", txtName);
        } else {
            addRow(form, c, "Full name (read only)", new JLabel(user.getFullName()));
        }

        addRow(form, c, "Email / contact", txtEmail);
        addRow(form, c, "Phone", txtPhone);

        if (!student) {
            addRow(form, c, "Department", txtDepartment);
        } else {
            UndergraduateUser ug = (UndergraduateUser) user;
            addRow(form, c, "Student Reg No", new JLabel(ug.getStudentRegNo()));
            addRow(form, c, "Department", new JLabel(ug.getDepartment()));
            addRow(form, c, "Batch", new JLabel(ug.getBatch()));
        }

        JPanel picInputPanel = new JPanel(new BorderLayout(6, 0));
        picInputPanel.setOpaque(false);
        picInputPanel.add(txtPicturePath, BorderLayout.CENTER);
        JButton btnBrowse = new JButton("...");
        btnBrowse.setToolTipText("Browse image file");
        btnBrowse.addActionListener(e -> chooseAndUploadPhoto());
        picInputPanel.add(btnBrowse, BorderLayout.EAST);

        addRow(form, c, "Profile picture path / URL", picInputPanel);

        // Save Button Row
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 8));
        savePanel.setOpaque(false);
        JButton btnSave = Theme.createPrimaryButton("Save Profile");
        btnSave.setPreferredSize(new Dimension(180, 42));
        btnSave.addActionListener(e -> saveProfile());
        savePanel.add(btnSave);
        form.add(savePanel, c);

        profileCard.add(form, BorderLayout.CENTER);

        // Footer Hint
        JLabel hint = new JLabel(student ?
                "Students may change contact details and profile picture only." :
                "Lecturers and Technical Officers may edit profile details, but not username or password.");
        hint.setFont(Theme.FONT_SMALL);
        hint.setForeground(Theme.TEXT_MUTED);
        hint.setBorder(new EmptyBorder(6, 8, 2, 8));
        profileCard.add(hint, BorderLayout.SOUTH);

        contentContainer.add(profileCard);

        JScrollPane mainScrollPane = new JScrollPane(contentContainer);
        mainScrollPane.setBorder(null);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(12);
        add(mainScrollPane, BorderLayout.CENTER);
    }

    private void addRow(JPanel p, GridBagConstraints c, String label, Component field) {
        c.gridx = 0;
        c.gridwidth = 1;
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.FONT_REGULAR);
        p.add(lbl, c);
        c.gridx = 1;
        p.add(field, c);
        c.gridy++;
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private void chooseAndUploadPhoto() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Profile Photo");
        chooser.setFileFilter(new FileNameExtensionFilter("Image Files (*.jpg, *.jpeg, *.png, *.gif, *.webp)", "jpg", "jpeg", "png", "gif", "webp"));
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            try {
                // Ensure upload directory exists
                File uploadDir = new File("uploads/avatars");
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                // Create destination file with safe filename
                String ext = "png";
                String name = selectedFile.getName();
                int dot = name.lastIndexOf('.');
                if (dot > 0) {
                    ext = name.substring(dot + 1).toLowerCase();
                }
                String destFileName = "avatar_" + user.getUserId() + "_" + System.currentTimeMillis() + "." + ext;
                File destFile = new File(uploadDir, destFileName);

                Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                String relativePath = destFile.getPath();
                txtPicturePath.setText(relativePath);
                avatarPreview.setImagePath(relativePath);

                JOptionPane.showMessageDialog(this, "Photo selected successfully!\nClick 'Save Profile' to apply changes.", "Photo Attached", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                // Fallback: use absolute path directly if file copy fails
                String absPath = selectedFile.getAbsolutePath();
                txtPicturePath.setText(absPath);
                avatarPreview.setImagePath(absPath);
            }
        }
    }

    private void saveProfile() {
        String em = txtEmail.getText().trim();
        String ph = txtPhone.getText().trim();
        String pic = txtPicturePath.getText().trim();

        if (em.isEmpty() || !em.contains("@")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 1. Update in-memory user object
        user.setEmail(em);
        user.setPhone(ph);
        user.setProfilePictureUrl(pic);
        if (user.getRole() != Role.UNDERGRADUATE) {
            String newName = txtName.getText().trim();
            if (!newName.isEmpty()) {
                user.setFullName(newName);
            }
            String newDept = txtDepartment.getText().trim();
            if (!newDept.isEmpty()) {
                user.setDepartment(newDept);
            }
        }

        // Update in-memory map
        DatabaseInitializer.USERS_MAP.put(user.getUsername(), user);

        // 2. Persist to MySQL if available
        boolean dbSaved = false;
        try (Connection con = DBConnection.getConnection()) {
            if (con != null) {
                String sql = user.getRole() == Role.UNDERGRADUATE
                        ? "UPDATE users SET email=?, phone=?, profile_picture_url=? WHERE user_id=?"
                        : "UPDATE users SET full_name=?, email=?, phone=?, department=?, profile_picture_url=? WHERE user_id=?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    int i = 1;
                    if (user.getRole() != Role.UNDERGRADUATE) {
                        ps.setString(i++, user.getFullName());
                    }
                    ps.setString(i++, em);
                    ps.setString(i++, ph);
                    ps.setString(i++, pic);
                    ps.setString(i, user.getUserId());
                    ps.executeUpdate();
                    dbSaved = true;
                }
            }
        } catch (Exception ex) {
            // Keep going, in-memory updated
        }

        // Refresh avatar preview
        avatarPreview.setImagePath(pic);
        avatarPreview.setFallbackInitials(user.getFullName());

        // Notify parent to refresh header/dashboard avatar
        if (onProfileUpdated != null) {
            onProfileUpdated.run();
        }

        String msg = dbSaved ?
                "Profile updated and saved to MySQL successfully!" :
                "Profile updated successfully (Active in session memory store)!";
        JOptionPane.showMessageDialog(this, msg, "Profile Saved", JOptionPane.INFORMATION_MESSAGE);
    }
}
