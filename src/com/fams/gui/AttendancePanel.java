package com.fams.gui;

import com.fams.exception.FAMSException;
import com.fams.model.*;
import com.fams.service.AttendanceService;
import com.fams.service.AuthService;
import com.fams.database.UserDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class AttendancePanel extends JPanel {
    private final AttendanceService attendanceService;
    private final UserDAO userDAO;

    private JTabbedPane tabbedPane;

    // Tab 1 Components (Record Entry)
    private JComboBox<String> cbCourseCode;
    private JComboBox<ComponentType> cbComponentType;
    private JSpinner spinSessionNo;
    private JTextField txtSessionDate;
    private JComboBox<String> cbStudents;
    private JCheckBox chkIsPresent;
    private DefaultTableModel recordTableModel;

    // Tab 2 Components (Individual View)
    private JComboBox<String> cbIndividualStudent;
    private JLabel lblTheoryVal, lblPracticalVal, lblCombinedVal, lblMedsVal, lblScenarioVal, lblEligibleVal;

    // Tab 3 Components (Batch View)
    private JComboBox<String> cbBatchFilterComponent;
    private DefaultTableModel batchTableModel;

    public AttendancePanel() {
        this.attendanceService = new AttendanceService();
        this.userDAO = new UserDAO();

        setLayout(new BorderLayout());
        setBackground(Theme.LIGHT_BG);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
    }

    private void initComponents() {
        User user = AuthService.getInstance().getCurrentUser();
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(Theme.FONT_SUBHEADER);

        // Tab 1: Technical Officer Attendance Entry Form (If authorized)
        if (user != null && (user.getRole() == Role.TECHNICAL_OFFICER || user.getRole() == Role.ADMIN)) {
            tabbedPane.addTab("Mark & Maintain Attendance (TO)", createRecordEntryPanel());
        }

        // Tab 2: Individual Attendance Summary & Scenarios
        tabbedPane.addTab("Individual Attendance & Scenarios", createIndividualSummaryPanel());

        // Tab 3: Whole Batch Attendance Summary
        tabbedPane.addTab("Batch Attendance Summary", createBatchSummaryPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // --- TAB 1: RECORD ENTRY PANEL ---
    private JPanel createRecordEntryPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Theme.LIGHT_BG);

        // Form Card
        JPanel formCard = Theme.createCardPanel();
        formCard.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        formCard.add(new JLabel("Course Code:"), gbc);
        gbc.gridx = 1;
        cbCourseCode = new JComboBox<>(new String[]{"ICT2132", "ICT2113", "ICT2122", "ENG2102"});
        formCard.add(cbCourseCode, gbc);

        gbc.gridx = 2;
        formCard.add(new JLabel("Component:"), gbc);
        gbc.gridx = 3;
        cbComponentType = new JComboBox<>(new ComponentType[]{ComponentType.THEORY, ComponentType.PRACTICAL});
        formCard.add(cbComponentType, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        formCard.add(new JLabel("Session No (1-15):"), gbc);
        gbc.gridx = 1;
        spinSessionNo = new JSpinner(new SpinnerNumberModel(1, 1, 15, 1));
        formCard.add(spinSessionNo, gbc);

        gbc.gridx = 2;
        formCard.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 3;
        txtSessionDate = new JTextField(LocalDate.now().toString());
        formCard.add(txtSessionDate, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        formCard.add(new JLabel("Student Registration:"), gbc);
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        cbStudents = new JComboBox<>();
        populateStudentCombo();
        formCard.add(cbStudents, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 3;
        chkIsPresent = new JCheckBox("Mark Present", true);
        chkIsPresent.setFont(Theme.FONT_SUBHEADER);
        formCard.add(chkIsPresent, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        JButton btnSave = Theme.createPrimaryButton("Save / Update Attendance Record");
        btnSave.addActionListener(e -> saveAttendance());
        formCard.add(btnSave, gbc);

        panel.add(formCard, BorderLayout.NORTH);

        // Table Card
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout());

        JLabel tableHeader = Theme.createHeaderLabel("Attendance Log");
        tableCard.add(tableHeader, BorderLayout.NORTH);

        String[] cols = {"Reg No", "Student Name", "Course", "Component", "Session", "Date", "Status"};
        recordTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable table = new JTable(recordTableModel);
        table.setFont(Theme.FONT_REGULAR);
        table.setRowHeight(24);

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(tableCard, BorderLayout.CENTER);

        refreshRecordTable();
        return panel;
    }

    private void populateStudentCombo() {
        cbStudents.removeAllItems();
        for (User u : userDAO.getAllUsers()) {
            if (u instanceof UndergraduateUser) {
                UndergraduateUser ug = (UndergraduateUser) u;
                cbStudents.addItem(ug.getStudentRegNo() + " - " + ug.getFullName());
            }
        }
    }

    private void saveAttendance() {
        try {
            String course = (String) cbCourseCode.getSelectedItem();
            ComponentType comp = (ComponentType) cbComponentType.getSelectedItem();
            int sessionNo = (int) spinSessionNo.getValue();
            LocalDate date = LocalDate.parse(txtSessionDate.getText().trim());

            String selectedStudent = (String) cbStudents.getSelectedItem();
            if (selectedStudent == null) return;
            String regNo = selectedStudent.split(" - ")[0];
            String name = selectedStudent.split(" - ")[1];

            boolean present = chkIsPresent.isSelected();

            AttendanceRecord rec = new AttendanceRecord(0, regNo, name, course, "Course " + course, comp, sessionNo, date, present, false, false);
            attendanceService.recordAttendance(rec);

            JOptionPane.showMessageDialog(this, "Attendance recorded successfully for " + name, "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshRecordTable();
            refreshIndividualSummary();
            refreshBatchSummary();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error saving attendance: " + ex.getMessage(), "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshRecordTable() {
        if (recordTableModel == null) return;
        recordTableModel.setRowCount(0);
        String course = cbCourseCode != null ? (String) cbCourseCode.getSelectedItem() : "ICT2132";
        String selectedStudent = cbStudents != null ? (String) cbStudents.getSelectedItem() : null;
        if (selectedStudent == null) return;
        String regNo = selectedStudent.split(" - ")[0];

        List<AttendanceRecord> list = new com.fams.database.AttendanceDAO().getAttendanceByStudent(regNo, course);
        for (AttendanceRecord r : list) {
            recordTableModel.addRow(new Object[]{
                r.getStudentRegNo(), r.getStudentName(), r.getCourseCode(),
                r.getComponentType().getDisplayName(), "Session " + r.getSessionNumber(),
                r.getSessionDate(), r.getStatusText()
            });
        }
    }

    // --- TAB 2: INDIVIDUAL SUMMARY PANEL ---
    private JPanel createIndividualSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Theme.LIGHT_BG);

        // Selector Bar
        JPanel selectPanel = Theme.createCardPanel();
        selectPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));

        selectPanel.add(new JLabel("Select Undergraduate:"));
        cbIndividualStudent = new JComboBox<>();
        populateStudentComboToIndividual();
        cbIndividualStudent.addActionListener(e -> refreshIndividualSummary());
        selectPanel.add(cbIndividualStudent);

        JButton btnRefresh = Theme.createSecondaryButton("Recalculate Summary");
        btnRefresh.addActionListener(e -> refreshIndividualSummary());
        selectPanel.add(btnRefresh);

        panel.add(selectPanel, BorderLayout.NORTH);

        // Summary Details Card
        JPanel card = Theme.createCardPanel();
        card.setLayout(new GridLayout(6, 2, 15, 15));

        card.add(new JLabel("Theory Attendance (15 Sessions):"));
        lblTheoryVal = new JLabel("-");
        lblTheoryVal.setFont(Theme.FONT_HEADER);
        card.add(lblTheoryVal);

        card.add(new JLabel("Practical Attendance (15 Sessions):"));
        lblPracticalVal = new JLabel("-");
        lblPracticalVal.setFont(Theme.FONT_HEADER);
        card.add(lblPracticalVal);

        card.add(new JLabel("Combined Overall Percentage:"));
        lblCombinedVal = new JLabel("-");
        lblCombinedVal.setFont(Theme.FONT_TITLE);
        lblCombinedVal.setForeground(Theme.PRIMARY_DARK);
        card.add(lblCombinedVal);

        card.add(new JLabel("Approved Medical Records:"));
        lblMedsVal = new JLabel("-");
        lblMedsVal.setFont(Theme.FONT_HEADER);
        card.add(lblMedsVal);

        card.add(new JLabel("80% Examination Eligibility:"));
        lblEligibleVal = new JLabel("-");
        lblEligibleVal.setFont(Theme.FONT_TITLE);
        card.add(lblEligibleVal);

        card.add(new JLabel("Demonstration Scenario Note:"));
        lblScenarioVal = new JLabel("-");
        lblScenarioVal.setFont(Theme.FONT_SUBHEADER);
        lblScenarioVal.setForeground(Theme.SECONDARY);
        card.add(lblScenarioVal);

        panel.add(card, BorderLayout.CENTER);

        User currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser instanceof UndergraduateUser) {
            UndergraduateUser ug = (UndergraduateUser) currentUser;
            cbIndividualStudent.setSelectedItem(ug.getStudentRegNo() + " - " + ug.getFullName());
            cbIndividualStudent.setEnabled(false); // Student can only view own summary!
        }

        refreshIndividualSummary();
        return panel;
    }

    private void populateStudentComboToIndividual() {
        cbIndividualStudent.removeAllItems();
        for (User u : userDAO.getAllUsers()) {
            if (u instanceof UndergraduateUser) {
                UndergraduateUser ug = (UndergraduateUser) u;
                cbIndividualStudent.addItem(ug.getStudentRegNo() + " - " + ug.getFullName());
            }
        }
    }

    private void refreshIndividualSummary() {
        String sel = (String) cbIndividualStudent.getSelectedItem();
        if (sel == null) return;

        String regNo = sel.split(" - ")[0];
        AttendanceSummary summary = attendanceService.generateIndividualSummary(regNo, "ICT2132");

        lblTheoryVal.setText(summary.getTheoryAttended() + " / 15 (" + String.format("%.1f", summary.getTheoryPercentage()) + "%)");
        lblPracticalVal.setText(summary.getPracticalAttended() + " / 15 (" + String.format("%.1f", summary.getPracticalPercentage()) + "%)");
        lblCombinedVal.setText(summary.getTotalAttended() + " / 30 (" + String.format("%.1f", summary.getCombinedPercentage()) + "%)");
        lblMedsVal.setText(summary.getApprovedMedicalsCount() + " Approved Certificates");

        if (summary.is80PercentEligible()) {
            lblEligibleVal.setText("ELIGIBLE TO SIT FINAL EXAM");
            lblEligibleVal.setForeground(Theme.SUCCESS);
        } else if (summary.getApprovedMedicalsCount() > 0) {
            lblEligibleVal.setText("ELIGIBLE (MEDICAL OVERRIDE)");
            lblEligibleVal.setForeground(Theme.WARNING);
        } else {
            lblEligibleVal.setText("NOT ELIGIBLE (< 80%)");
            lblEligibleVal.setForeground(Theme.DANGER);
        }

        lblScenarioVal.setText(summary.getScenarioDescription());
    }

    // --- TAB 3: BATCH SUMMARY PANEL ---
    private JPanel createBatchSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 15));
        panel.setBackground(Theme.LIGHT_BG);

        JPanel topBar = Theme.createCardPanel();
        topBar.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));

        topBar.add(new JLabel("Course Module: ICT2132"));
        topBar.add(Box.createHorizontalStrut(20));
        topBar.add(new JLabel("View Component:"));

        cbBatchFilterComponent = new JComboBox<>(new String[]{"Combined (Theory + Practical)", "Theory Only", "Practical Only"});
        cbBatchFilterComponent.addActionListener(e -> refreshBatchSummary());
        topBar.add(cbBatchFilterComponent);

        JButton btnBatchRefresh = Theme.createPrimaryButton("Refresh Batch Report");
        btnBatchRefresh.addActionListener(e -> refreshBatchSummary());
        topBar.add(btnBatchRefresh);

        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Reg No", "Undergraduate Name", "Batch", "Theory %", "Practical %", "Selected View %", "Approved Meds", "Eligibility Status"};
        batchTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(batchTableModel);
        table.setFont(Theme.FONT_REGULAR);
        table.setRowHeight(24);

        // Highlight ineligible rows with warning color
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) table.getValueAt(row, 7);
                if (status != null && status.contains("Not Eligible")) {
                    c.setForeground(Theme.DANGER);
                    c.setFont(Theme.FONT_SUBHEADER);
                } else if (status != null && status.contains("Medical")) {
                    c.setForeground(Theme.WARNING);
                    c.setFont(Theme.FONT_SUBHEADER);
                } else {
                    c.setForeground(Theme.TEXT_MAIN);
                    c.setFont(Theme.FONT_REGULAR);
                }
                return c;
            }
        });

        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        refreshBatchSummary();
        return panel;
    }

    private void refreshBatchSummary() {
        if (batchTableModel == null) return;
        batchTableModel.setRowCount(0);

        int filterIdx = cbBatchFilterComponent != null ? cbBatchFilterComponent.getSelectedIndex() : 0;
        List<AttendanceSummary> list = attendanceService.generateBatchSummary("ICT2132");

        for (AttendanceSummary s : list) {
            double selectedPct;
            if (filterIdx == 1) selectedPct = s.getTheoryPercentage();
            else if (filterIdx == 2) selectedPct = s.getPracticalPercentage();
            else selectedPct = s.getCombinedPercentage();

            batchTableModel.addRow(new Object[]{
                s.getStudentRegNo(), s.getStudentName(), s.getBatch(),
                String.format("%.1f%%", s.getTheoryPercentage()),
                String.format("%.1f%%", s.getPracticalPercentage()),
                String.format("%.1f%%", selectedPct),
                s.getApprovedMedicalsCount(),
                s.getEligibilityStatus()
            });
        }
    }
}
