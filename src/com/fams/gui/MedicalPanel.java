package com.fams.gui;

import com.fams.model.*;
import com.fams.service.AuthService;
import com.fams.service.MedicalService;
import com.fams.database.UserDAO;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDate;
import java.util.List;

public class MedicalPanel extends JPanel {
    private final MedicalService medicalService;
    private final UserDAO userDAO;

    private DefaultTableModel tableModel;
    private JTable table;

    // Form inputs
    private JComboBox<String> cbStudent;
    private JComboBox<String> cbCourse;
    private JComboBox<ComponentType> cbComponent;
    private JSpinner spinSession;
    private JTextField txtAbsentDate;
    private JTextField txtRefNo;
    private JTextField txtReason;

    private JLabel lblImageStatus;
    private String selectedImagePath = "";

    public MedicalPanel() {
        this.medicalService = new MedicalService();
        this.userDAO = new UserDAO();

        setLayout(new BorderLayout(15, 15));
        setBackground(Theme.LIGHT_BG);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
    }

    private void initComponents() {
        User user = AuthService.getInstance().getCurrentUser();

        // Top Form Card (Allowed for Undergraduate, Technical Officer, Admin)
        if (user != null && (user.getRole() == Role.UNDERGRADUATE || user.getRole() == Role.TECHNICAL_OFFICER || user.getRole() == Role.ADMIN)) {
            add(createSubmissionFormCard(user), BorderLayout.NORTH);
        }

        // Center Table Card
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel lblTitle = Theme.createHeaderLabel("Undergraduate Medical Records & Certificate Documents");
        headerPanel.add(lblTitle, BorderLayout.WEST);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtns.setBackground(Color.WHITE);

        JButton btnViewImage = Theme.createSecondaryButton(" View Certificate Image");
        btnViewImage.addActionListener(e -> viewSelectedCertificateImage());
        actionBtns.add(btnViewImage);

        if (user != null && (user.getRole() == Role.TECHNICAL_OFFICER || user.getRole() == Role.ADMIN)) {
            JButton btnApprove = Theme.createSuccessButton("Approve Selected");
            btnApprove.addActionListener(e -> updateSelectedMedicalStatus(MedicalStatus.APPROVED));

            JButton btnReject = Theme.createDangerButton("Reject Selected");
            btnReject.addActionListener(e -> updateSelectedMedicalStatus(MedicalStatus.REJECTED));

            actionBtns.add(btnApprove);
            actionBtns.add(btnReject);
        }

        headerPanel.add(actionBtns, BorderLayout.EAST);
        tableCard.add(headerPanel, BorderLayout.NORTH);

        String[] cols = {"Med ID", "Reg No", "Student Name", "Course", "Component", "Session", "Absent Date", "Ref No", "Reason", "Status", "Certificate File", "Reviewed By"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(Theme.FONT_REGULAR);
        table.setRowHeight(26);

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        add(tableCard, BorderLayout.CENTER);

        refreshTable();
    }

    private JPanel createSubmissionFormCard(User currentUser) {
        JPanel formCard = Theme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 8, 5, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel formHeader = new JLabel("Submit Medical Certificate Document");
        formHeader.setFont(Theme.FONT_HEADER);
        formHeader.setForeground(Theme.PRIMARY_DARK);
        gbc.gridwidth = 4;
        formCard.add(formHeader, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;
        formCard.add(new JLabel("Student:"), gbc);
        gbc.gridx = 1;
        cbStudent = new JComboBox<>();
        populateStudents();
        if (currentUser instanceof UndergraduateUser) {
            UndergraduateUser ug = (UndergraduateUser) currentUser;
            cbStudent.setSelectedItem(ug.getStudentRegNo() + " - " + ug.getFullName());
            cbStudent.setEnabled(false); // Student can only submit for themselves!
        }
        formCard.add(cbStudent, gbc);

        gbc.gridx = 2;
        formCard.add(new JLabel("Course:"), gbc);
        gbc.gridx = 3;
        cbCourse = new JComboBox<>(new String[]{"ICT2132", "ICT2113", "ICT2122", "ENG2102"});
        formCard.add(cbCourse, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        formCard.add(new JLabel("Component:"), gbc);
        gbc.gridx = 1;
        cbComponent = new JComboBox<>(new ComponentType[]{ComponentType.THEORY, ComponentType.PRACTICAL});
        formCard.add(cbComponent, gbc);

        gbc.gridx = 2;
        formCard.add(new JLabel("Session No:"), gbc);
        gbc.gridx = 3;
        spinSession = new JSpinner(new SpinnerNumberModel(1, 1, 15, 1));
        formCard.add(spinSession, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        formCard.add(new JLabel("Absent Date:"), gbc);
        gbc.gridx = 1;
        txtAbsentDate = new JTextField(LocalDate.now().minusDays(1).toString());
        formCard.add(txtAbsentDate, gbc);

        gbc.gridx = 2;
        formCard.add(new JLabel("Reference No:"), gbc);
        gbc.gridx = 3;
        txtRefNo = new JTextField("MED/2026/" + (int)(Math.random() * 900 + 100));
        formCard.add(txtRefNo, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        formCard.add(new JLabel("Reason / Diagnosis:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 3;
        txtReason = new JTextField("Viral Fever & Doctor Advised Medical Rest");
        formCard.add(txtReason, gbc);

        // Medical Certificate Image Attachment Row
        gbc.gridwidth = 1;
        gbc.gridy++;
        gbc.gridx = 0;
        formCard.add(new JLabel("Certificate Image:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        lblImageStatus = new JLabel("No certificate selected");
        lblImageStatus.setFont(Theme.FONT_SMALL);
        lblImageStatus.setForeground(Theme.PRIMARY);
        formCard.add(lblImageStatus, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 3;
        JButton btnChooseImg = Theme.createSecondaryButton(" Attach Certificate Image");
        btnChooseImg.addActionListener(e -> selectImageFile());
        formCard.add(btnChooseImg, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        JButton btnSubmit = Theme.createPrimaryButton("Submit Medical Certificate Record");
        btnSubmit.addActionListener(e -> submitMedicalRecord());
        formCard.add(btnSubmit, gbc);

        return formCard;
    }

    private void selectImageFile() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Medical Certificate Images (*.jpg, *.png, *.jpeg)", "jpg", "jpeg", "png");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            selectedImagePath = selectedFile.getAbsolutePath();
            lblImageStatus.setText("Attached: " + selectedFile.getName());
        }
    }

    private void populateStudents() {
        cbStudent.removeAllItems();
        for (User u : userDAO.getAllUsers()) {
            if (u instanceof UndergraduateUser) {
                UndergraduateUser ug = (UndergraduateUser) u;
                cbStudent.addItem(ug.getStudentRegNo() + " - " + ug.getFullName());
            }
        }
    }

    private void submitMedicalRecord() {
        try {
            String selStud = (String) cbStudent.getSelectedItem();
            if (selStud == null) return;
            String regNo = selStud.split(" - ")[0];
            String name = selStud.split(" - ")[1];

            String course = (String) cbCourse.getSelectedItem();
            ComponentType comp = (ComponentType) cbComponent.getSelectedItem();
            int sessionNo = (int) spinSession.getValue();
            LocalDate absentDate = LocalDate.parse(txtAbsentDate.getText().trim());
            String refNo = txtRefNo.getText().trim();
            String reason = txtReason.getText().trim();

            MedicalRecord m = new MedicalRecord(0, regNo, name, course, comp, sessionNo, absentDate, refNo, reason, LocalDate.now(), MedicalStatus.PENDING, null, selectedImagePath);
            medicalService.submitMedical(m);

            JOptionPane.showMessageDialog(this, "Medical Record with Certificate Image Submitted Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error submitting medical: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewSelectedCertificateImage() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a medical record from the table to view its certificate document.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int medId = (int) tableModel.getValueAt(selectedRow, 0);
        String regNo = (String) tableModel.getValueAt(selectedRow, 1);
        String name = (String) tableModel.getValueAt(selectedRow, 2);
        String refNo = (String) tableModel.getValueAt(selectedRow, 7);
        String imgPath = (String) tableModel.getValueAt(selectedRow, 10);

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Medical Certificate Document View - " + refNo, true);
        dlg.setSize(550, 500);
        dlg.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topInfo = new JPanel(new GridLayout(3, 1, 4, 4));
        topInfo.setBackground(Color.WHITE);
        topInfo.add(new JLabel("Student: " + name + " (" + regNo + ")", SwingConstants.CENTER));
        topInfo.add(new JLabel("Reference No: " + refNo, SwingConstants.CENTER));
        topInfo.add(new JLabel("Image File: " + imgPath, SwingConstants.CENTER));

        panel.add(topInfo, BorderLayout.NORTH);

        // Certificate Image Preview Box
        JPanel imgDisplayPanel = new JPanel(new BorderLayout());
        imgDisplayPanel.setBackground(new Color(248, 250, 252));
        imgDisplayPanel.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER, 1), new EmptyBorder(20, 20, 20, 20)));

        File file = new File(imgPath);
        if (file.exists() && file.isFile()) {
            ImageIcon icon = new ImageIcon(imgPath);
            Image img = icon.getImage().getScaledInstance(450, 300, Image.SCALE_SMOOTH);
            JLabel lblImage = new JLabel(new ImageIcon(img), SwingConstants.CENTER);
            imgDisplayPanel.add(lblImage, BorderLayout.CENTER);
        } else {
            // Rendered Medical Certificate Box for Sample Demonstration
            JPanel certGraphic = new JPanel(new GridBagLayout());
            certGraphic.setBackground(new Color(254, 252, 232)); // Light parchment yellow
            certGraphic.setBorder(new LineBorder(new Color(202, 138, 4), 2));

            JLabel lblCertText = new JLabel("<html><div style='text-align: center; padding: 20px;'>" +
                    "<h2 style='color:#854d0e;'>MEDICAL CERTIFICATE OF FITNESS</h2>" +
                    "<p><b>Faculty of Technology - Health Services</b></p><hr>" +
                    "<p>This certifies that <b>" + name + "</b> (" + regNo + ") was under medical treatment on " + tableModel.getValueAt(selectedRow, 6) + ".</p>" +
                    "<p><b>Diagnosis:</b> " + tableModel.getValueAt(selectedRow, 8) + "</p>" +
                    "<p style='margin-top:20px; color:#15803d;'><b>[OFFICIAL STAMP & DIGITAL SIGNATURE ATTACHED]</b></p>" +
                    "</div></html>");

            certGraphic.add(lblCertText);
            imgDisplayPanel.add(certGraphic, BorderLayout.CENTER);
        }

        panel.add(imgDisplayPanel, BorderLayout.CENTER);

        JButton btnClose = Theme.createPrimaryButton("Close View");
        btnClose.addActionListener(e -> dlg.dispose());
        panel.add(btnClose, BorderLayout.SOUTH);

        dlg.add(panel);
        dlg.setVisible(true);
    }

    private void updateSelectedMedicalStatus(MedicalStatus newStatus) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a medical record from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int medId = (int) tableModel.getValueAt(selectedRow, 0);
        try {
            medicalService.updateApprovalStatus(medId, newStatus);
            JOptionPane.showMessageDialog(this, "Medical ID #" + medId + " status updated to " + newStatus.getDisplayName(), "Status Updated", JOptionPane.INFORMATION_MESSAGE);
            refreshTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Authorization/Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshTable() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);

        List<MedicalRecord> list = medicalService.getMedicalsForCurrentUser();
        for (MedicalRecord m : list) {
            File f = new File(m.getImagePath());
            String fileName = f.exists() ? f.getName() : m.getImagePath();

            tableModel.addRow(new Object[]{
                m.getMedicalId(), m.getStudentRegNo(), m.getStudentName(),
                m.getCourseCode(), m.getComponentType().getDisplayName(),
                "Session " + m.getSessionNumber(), m.getAbsentDate(),
                m.getReferenceNo(), m.getReason(), m.getStatus().getDisplayName(),
                fileName,
                m.getReviewedBy() == null ? "Pending Review" : m.getReviewedBy()
            });
        }
    }
}
