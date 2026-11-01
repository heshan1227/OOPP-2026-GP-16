package com.fams.gui;

import com.fams.database.AttendanceDAO;
import com.fams.database.UserDAO;
import com.fams.exception.FAMSException;
import com.fams.model.*;
import com.fams.service.AuthService;
import com.fams.service.MedicalService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Medical Records screen.
 *
 *  - Undergraduate : upload medicals for his / her own absent sessions + submission history.
 *  - Technical Officer / Admin : two tabs
 *        1) "Upload Medical"                      - same upload system, but for any selected student
 *        2) "Approve / Reject / View Medical"     - review every submitted medical
 *  - Lecturer : read-only review table (can view certificates).
 *
 * Rule: a student can have at most {@link MedicalService#MAX_MEDICALS_PER_COURSE} medicals per subject.
 * Approving / rejecting a medical updates the Attendance records automatically.
 */
public class MedicalPanel extends JPanel {
    private final MedicalService medicalService;
    private final UserDAO userDAO;
    private final AttendanceDAO attendanceDAO;

    // ---- Review tab (Approve / Reject / View) ----
    private DefaultTableModel reviewModel;
    private JTable reviewTable;
    private JComboBox<String> cbStatusFilter;
    private final List<MedicalRecord> reviewRecords = new ArrayList<>();

    // ---- Upload tab ----
    private UndergraduateUser uploadStudent;          // null for Admin / TO (they choose a student)
    private JComboBox<String> cbStudent;
    private JSpinner fromDateSpinner, toDateSpinner;
    private JTable absentTable;
    private DefaultTableModel absentModel;
    private List<AttendanceRecord> absentSessions = new ArrayList<>();
    private JTextField reasonField;
    private JLabel imageLabel;
    private JLabel slotsLabel;
    private String selectedImagePath = "";

    // ---- Student history ----
    private DefaultTableModel historyModel;
    private JTable historyTable;
    private final List<MedicalRecord> historyRecords = new ArrayList<>();

    private JTabbedPane tabs;

    public MedicalPanel() {
        this.medicalService = new MedicalService();
        this.userDAO = new UserDAO();
        this.attendanceDAO = new AttendanceDAO();

        setLayout(new BorderLayout(15, 15));
        setBackground(Theme.LIGHT_BG);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
    }

    private void initComponents() {
        User user = AuthService.getInstance().getCurrentUser();

        if (user instanceof UndergraduateUser) {
            uploadStudent = (UndergraduateUser) user;
            add(createUploadPanel(true), BorderLayout.CENTER);
            return;
        }

        boolean canReview = user != null && (user.getRole() == Role.TECHNICAL_OFFICER || user.getRole() == Role.ADMIN);
        if (canReview) {
            tabs = new JTabbedPane();
            tabs.setFont(Theme.FONT_SUBHEADER);
            tabs.addTab("Upload Medical", createUploadPanel(false));
            tabs.addTab("Approve / Reject / View Medical", createReviewPanel(true));
            tabs.addChangeListener(e -> {
                if (tabs.getSelectedIndex() == 1) refreshReviewTable();
                else loadAbsences(false);
            });
            add(tabs, BorderLayout.CENTER);
        } else {
            // Lecturer: view only
            add(createReviewPanel(false), BorderLayout.CENTER);
        }
    }

    /** Called by the main window whenever the Medical Records screen is opened. */
    public void refreshAll() {
        if (reviewModel != null) refreshReviewTable();
        if (historyModel != null) refreshHistory();
        if (absentModel != null) loadAbsences(false);
    }

    // =====================================================================================
    //  UPLOAD PANEL  (Student / Technical Officer / Admin)
    // =====================================================================================

    private JPanel createUploadPanel(boolean studentMode) {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setOpaque(false);

        // ---- Filter card ----
        JPanel filterCard = Theme.createCardPanel();
        filterCard.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 8, 6, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0; g.gridy = 0; g.gridwidth = 4;
        filterCard.add(Theme.createHeaderLabel(studentMode
                ? "Submit Medical for Absent Attendance"
                : "Upload Medical for a Student's Absent Attendance"), g);

        g.gridwidth = 1;
        if (!studentMode) {
            g.gridy++;
            g.gridx = 0;
            filterCard.add(new JLabel("Student:"), g);
            g.gridx = 1; g.gridwidth = 3;
            cbStudent = new JComboBox<>();
            populateStudents();
            cbStudent.addActionListener(e -> {
                selectedImagePath = "";
                if (imageLabel != null) imageLabel.setText("No image selected");
                loadAbsences(false);
            });
            filterCard.add(cbStudent, g);
            g.gridwidth = 1;
        }

        g.gridy++;
        g.gridx = 0;
        filterCard.add(new JLabel("From Date:"), g);
        g.gridx = 1;
        fromDateSpinner = dateSpinner(LocalDate.now().minusMonths(4));
        filterCard.add(fromDateSpinner, g);
        g.gridx = 2;
        filterCard.add(new JLabel("To Date:"), g);
        g.gridx = 3;
        toDateSpinner = dateSpinner(LocalDate.now());
        filterCard.add(toDateSpinner, g);

        g.gridx = 0; g.gridy++; g.gridwidth = 4;
        JButton search = Theme.createPrimaryButton("Find Absent Course Sessions");
        search.addActionListener(e -> loadAbsences(true));
        filterCard.add(search, g);
        panel.add(filterCard, BorderLayout.NORTH);

        // ---- Absent sessions + submit card ----
        JPanel center = Theme.createCardPanel();
        center.setLayout(new BorderLayout(8, 8));

        JPanel centerTop = new JPanel(new GridLayout(2, 1, 0, 2));
        centerTop.setOpaque(false);
        centerTop.add(Theme.createHeaderLabel("Select the course session(s) for medical submission"));
        slotsLabel = new JLabel(" ");
        slotsLabel.setFont(Theme.FONT_SMALL);
        slotsLabel.setForeground(Theme.PRIMARY);
        centerTop.add(slotsLabel);
        center.add(centerTop, BorderLayout.NORTH);

        String[] cols = {"Select", "Date", "Course Code", "Component", "Session", "Attendance"};
        absentModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 0; }
            @Override public Class<?> getColumnClass(int col) { return col == 0 ? Boolean.class : Object.class; }
        };
        absentModel.addTableModelListener(e -> updateSlotsLabel());
        absentTable = new JTable(absentModel);
        styleMaroonTable(absentTable);
        absentTable.getColumnModel().getColumn(0).setMaxWidth(75);
        center.add(new JScrollPane(absentTable), BorderLayout.CENTER);

        JPanel submit = new JPanel(new GridBagLayout());
        submit.setOpaque(false);
        GridBagConstraints f = new GridBagConstraints();
        f.insets = new Insets(5, 7, 5, 7);
        f.fill = GridBagConstraints.HORIZONTAL;
        f.gridx = 0; f.gridy = 0;
        submit.add(new JLabel("Reason / Diagnosis:"), f);
        f.gridx = 1; f.weightx = 1;
        reasonField = new JTextField();
        submit.add(reasonField, f);
        f.gridx = 0; f.gridy = 1; f.weightx = 0;
        submit.add(new JLabel("Medical Certificate Photo:"), f);
        f.gridx = 1; f.weightx = 1;
        JPanel attach = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        attach.setOpaque(false);
        imageLabel = new JLabel("No image selected");
        attach.add(imageLabel);
        JButton choose = Theme.createSecondaryButton("Choose Medical Photo");
        choose.addActionListener(e -> selectImageFile());
        attach.add(choose);
        submit.add(attach, f);
        f.gridx = 0; f.gridy = 2; f.gridwidth = 2; f.weightx = 0;
        JButton send = Theme.createPrimaryButton("Submit Medical for Selected Courses");
        send.addActionListener(e -> submitSelectedMedicals());
        submit.add(send, f);
        center.add(submit, BorderLayout.SOUTH);
        panel.add(center, BorderLayout.CENTER);

        // ---- Student's own history ----
        if (studentMode) {
            JPanel history = Theme.createCardPanel();
            history.setLayout(new BorderLayout(6, 6));

            JPanel historyHeader = new JPanel(new BorderLayout());
            historyHeader.setOpaque(false);
            historyHeader.add(Theme.createHeaderLabel("My Medical Submission History"), BorderLayout.WEST);
            JButton viewOwn = Theme.createSecondaryButton("View Certificate");
            viewOwn.addActionListener(e -> {
                int row = historyTable.getSelectedRow();
                if (row == -1) {
                    JOptionPane.showMessageDialog(this, "Select a medical from your history first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                showCertificateDialog(historyRecords.get(historyTable.convertRowIndexToModel(row)));
            });
            historyHeader.add(viewOwn, BorderLayout.EAST);
            history.add(historyHeader, BorderLayout.NORTH);

            String[] hcols = {"Med ID", "Course", "Session", "Absent Date", "Reason", "Status", "Certificate"};
            historyModel = new DefaultTableModel(hcols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            historyTable = new JTable(historyModel);
            styleMaroonTable(historyTable);
            history.add(new JScrollPane(historyTable), BorderLayout.CENTER);
            history.setPreferredSize(new Dimension(500, 165));
            panel.add(history, BorderLayout.SOUTH);
            refreshHistory();
        }

        loadAbsences(false);
        return panel;
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

    private String selectedRegNo() {
        if (uploadStudent != null) return uploadStudent.getStudentRegNo();
        if (cbStudent == null) return null;
        String sel = (String) cbStudent.getSelectedItem();
        if (sel == null) return null;
        int idx = sel.indexOf(" - ");
        return idx < 0 ? sel : sel.substring(0, idx);
    }

    private String selectedStudentName() {
        if (uploadStudent != null) return uploadStudent.getFullName();
        if (cbStudent == null) return "";
        String sel = (String) cbStudent.getSelectedItem();
        if (sel == null) return "";
        int idx = sel.indexOf(" - ");
        return idx < 0 ? sel : sel.substring(idx + 3);
    }

    private JSpinner dateSpinner(LocalDate value) {
        Date initial = java.sql.Date.valueOf(value);
        JSpinner spinner = new JSpinner(new SpinnerDateModel(initial, null, null, java.util.Calendar.DAY_OF_MONTH));
        spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
        spinner.setPreferredSize(new Dimension(145, 30));
        return spinner;
    }

    private LocalDate spinnerDate(JSpinner spinner) {
        return java.time.Instant.ofEpochMilli(((Date) spinner.getValue()).getTime())
                .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }

    /** Lists the student's absent sessions that do not already have a pending / approved medical. */
    private void loadAbsences(boolean notify) {
        if (absentModel == null) return;
        try {
            String regNo = selectedRegNo();
            absentModel.setRowCount(0);
            absentSessions = new ArrayList<>();
            if (regNo == null) {
                updateSlotsLabel();
                return;
            }
            LocalDate from = spinnerDate(fromDateSpinner), to = spinnerDate(toDateSpinner);
            if (from.isAfter(to)) throw new IllegalArgumentException("From Date must be before or equal to To Date.");

            absentSessions = attendanceDAO.getAbsencesAvailableForMedical(regNo, from, to);
            for (AttendanceRecord r : absentSessions) {
                absentModel.addRow(new Object[]{Boolean.FALSE, r.getSessionDate(), r.getCourseCode(),
                        r.getComponentType().getDisplayName(), "Session " + r.getSessionNumber(), "Absent"});
            }
            updateSlotsLabel();
            if (notify && absentSessions.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "No absent course sessions without a medical were found for the selected date range.",
                        "No Absences", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Date Selection Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Shows how many of the 3 medical slots per subject are used / about to be used. */
    private void updateSlotsLabel() {
        if (slotsLabel == null) return;
        String regNo = selectedRegNo();
        if (regNo == null) { slotsLabel.setText(" "); return; }

        Map<String, Integer> selectedPerCourse = new TreeMap<>();
        Map<String, Boolean> courses = new TreeMap<>();
        for (int i = 0; i < absentSessions.size(); i++) {
            String course = absentSessions.get(i).getCourseCode();
            courses.put(course, Boolean.TRUE);
            if (i < absentModel.getRowCount() && Boolean.TRUE.equals(absentModel.getValueAt(i, 0))) {
                selectedPerCourse.merge(course, 1, Integer::sum);
            }
        }
        if (courses.isEmpty()) {
            slotsLabel.setForeground(Theme.PRIMARY);
            slotsLabel.setText("No absent sessions without a medical in this date range. (Max "
                    + MedicalService.MAX_MEDICALS_PER_COURSE + " medicals per subject)");
            return;
        }

        boolean overLimit = false;
        StringBuilder sb = new StringBuilder("Medical slots used (max " + MedicalService.MAX_MEDICALS_PER_COURSE + " per subject):  ");
        for (String course : courses.keySet()) {
            int used = medicalService.getUsedMedicalCount(regNo, course);
            int selected = selectedPerCourse.getOrDefault(course, 0);
            if (used + selected > MedicalService.MAX_MEDICALS_PER_COURSE) overLimit = true;
            sb.append(course).append(" ").append(used).append("/").append(MedicalService.MAX_MEDICALS_PER_COURSE);
            if (selected > 0) sb.append(" (+").append(selected).append(" selected)");
            sb.append("     ");
        }
        slotsLabel.setForeground(overLimit ? Theme.DANGER : Theme.PRIMARY);
        slotsLabel.setText(sb.toString());
    }

    private void submitSelectedMedicals() {
        if (absentTable.isEditing()) absentTable.getCellEditor().stopCellEditing();

        String regNo = selectedRegNo();
        if (regNo == null) {
            JOptionPane.showMessageDialog(this, "Please select a student.");
            return;
        }
        String reason = reasonField.getText().trim();
        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the medical reason / diagnosis.");
            return;
        }
        if (selectedImagePath == null || selectedImagePath.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please select the medical certificate photo.");
            return;
        }
        List<Integer> selected = new ArrayList<>();
        for (int i = 0; i < absentModel.getRowCount(); i++) {
            if (Boolean.TRUE.equals(absentModel.getValueAt(i, 0))) selected.add(i);
        }
        if (selected.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Select at least one absent course session using the checkbox.");
            return;
        }

        String studentName = selectedStudentName();
        Path saved = null;
        try {
            List<MedicalRecord> records = new ArrayList<>();
            long stamp = System.currentTimeMillis();
            for (Integer row : selected) {
                AttendanceRecord a = absentSessions.get(row);
                String ref = "MED/" + LocalDate.now().getYear() + "/" + stamp + "/" + a.getRecordId();
                records.add(new MedicalRecord(0, regNo, studentName, a.getCourseCode(), a.getComponentType(),
                        a.getSessionNumber(), a.getSessionDate(), ref, reason, LocalDate.now(),
                        MedicalStatus.PENDING, null, ""));
            }

            // Check every rule (3 per subject, duplicates, permissions) BEFORE copying the image.
            medicalService.validateSubmission(records);

            Path uploadDir = Path.of("uploads", "medical");
            Files.createDirectories(uploadDir);
            File source = new File(selectedImagePath);
            saved = uploadDir.resolve(regNo.replaceAll("[^A-Za-z0-9_-]", "_") + "_" + stamp + "_" + source.getName());
            Files.copy(source.toPath(), saved, StandardCopyOption.REPLACE_EXISTING);
            for (MedicalRecord r : records) r.setImagePath(saved.toString());

            medicalService.submitMedicals(records);

            JOptionPane.showMessageDialog(this,
                    "Medical certificate submitted for " + records.size() + " absent session(s).\n"
                            + "Status: Pending - it will update the attendance once a Technical Officer approves it.",
                    "Submitted", JOptionPane.INFORMATION_MESSAGE);
            selectedImagePath = "";
            imageLabel.setText("No image selected");
            reasonField.setText("");
            if (historyModel != null) refreshHistory();
            if (reviewModel != null) refreshReviewTable();
            loadAbsences(false);
        } catch (FAMSException ex) {
            deleteQuietly(saved);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Submit Medical", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            deleteQuietly(saved);
            JOptionPane.showMessageDialog(this, "Unable to submit medical: " + ex.getMessage(), "Submission Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) return;
        try { Files.deleteIfExists(path); } catch (Exception ignored) { /* nothing else to do */ }
    }

    private void selectImageFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Medical Certificate Images (*.jpg, *.png, *.jpeg)", "jpg", "jpeg", "png"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            selectedImagePath = selectedFile.getAbsolutePath();
            if (imageLabel != null) imageLabel.setText("Attached: " + selectedFile.getName());
        }
    }

    private void refreshHistory() {
        if (historyModel == null) return;
        historyModel.setRowCount(0);
        historyRecords.clear();
        for (MedicalRecord record : medicalService.getMedicalsForCurrentUser()) {
            historyRecords.add(record);
            String imagePath = record.getImagePath() == null ? "" : record.getImagePath();
            String fileName = imagePath.isBlank() ? "Not attached" : new File(imagePath).getName();
            historyModel.addRow(new Object[]{
                    record.getMedicalId(), record.getCourseCode(),
                    record.getComponentType().getDisplayName() + " S" + record.getSessionNumber(),
                    record.getAbsentDate(), record.getReason(), record.getStatus().getDisplayName(), fileName
            });
        }
    }

    // =====================================================================================
    //  REVIEW PANEL  (Approve / Reject / View)  -  Technical Officer, Admin (+ read-only Lecturer)
    // =====================================================================================

    private JPanel createReviewPanel(boolean canReview) {
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);
        JLabel pageTitle = Theme.createHeaderLabel("Undergraduate Medical Records & Certificate Documents");
        pageTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(pageTitle);
        headerPanel.add(Box.createVerticalStrut(10));

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        actionBtns.setBackground(Color.WHITE);
        actionBtns.setAlignmentX(Component.LEFT_ALIGNMENT);

        actionBtns.add(new JLabel("Show:"));
        cbStatusFilter = new JComboBox<>(new String[]{"All", "Pending", "Approved", "Rejected"});
        cbStatusFilter.addActionListener(e -> refreshReviewTable());
        actionBtns.add(cbStatusFilter);

        JButton btnRefresh = Theme.createSecondaryButton("Refresh Records");
        btnRefresh.addActionListener(e -> refreshReviewTable());
        actionBtns.add(btnRefresh);

        JButton btnViewImage = Theme.createSecondaryButton("View Medical");
        btnViewImage.addActionListener(e -> viewSelectedCertificate());
        actionBtns.add(btnViewImage);

        if (canReview) {
            JButton btnApprove = Theme.createSuccessButton("Approve Selected");
            btnApprove.addActionListener(e -> updateSelectedMedicalStatus(MedicalStatus.APPROVED));
            JButton btnReject = Theme.createDangerButton("Reject Selected");
            btnReject.addActionListener(e -> updateSelectedMedicalStatus(MedicalStatus.REJECTED));
            actionBtns.add(btnApprove);
            actionBtns.add(btnReject);
        }

        headerPanel.add(actionBtns);
        tableCard.add(headerPanel, BorderLayout.NORTH);

        String[] cols = {"Med ID", "Reg No", "Student Name", "Course", "Component", "Session", "Absent Date",
                "Ref No", "Reason", "Status", "Certificate File", "Reviewed By"};
        reviewModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        reviewTable = new JTable(reviewModel);
        styleMaroonTable(reviewTable);
        tableCard.add(new JScrollPane(reviewTable), BorderLayout.CENTER);

        refreshReviewTable();

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(tableCard, BorderLayout.CENTER);
        return wrapper;
    }

    private void refreshReviewTable() {
        if (reviewModel == null) return;
        reviewModel.setRowCount(0);
        reviewRecords.clear();

        String filter = cbStatusFilter == null ? "All" : (String) cbStatusFilter.getSelectedItem();
        for (MedicalRecord m : medicalService.getMedicalsForCurrentUser()) {
            if (filter != null && !filter.equals("All") && !m.getStatus().getDisplayName().equals(filter)) continue;
            reviewRecords.add(m);

            String imagePath = m.getImagePath() == null ? "" : m.getImagePath();
            String fileName = imagePath.isBlank() ? "Not attached" : new File(imagePath).getName();

            reviewModel.addRow(new Object[]{
                    m.getMedicalId(), m.getStudentRegNo(), m.getStudentName(),
                    m.getCourseCode(), m.getComponentType().getDisplayName(),
                    "Session " + m.getSessionNumber(), m.getAbsentDate(),
                    m.getReferenceNo(), m.getReason(), m.getStatus().getDisplayName(),
                    fileName,
                    m.getReviewedBy() == null ? "Pending Review" : m.getReviewedBy()
            });
        }
    }

    private MedicalRecord selectedReviewRecord() {
        int row = reviewTable.getSelectedRow();
        if (row == -1) return null;
        int modelRow = reviewTable.convertRowIndexToModel(row);
        return modelRow < reviewRecords.size() ? reviewRecords.get(modelRow) : null;
    }

    private void viewSelectedCertificate() {
        MedicalRecord m = selectedReviewRecord();
        if (m == null) {
            JOptionPane.showMessageDialog(this, "Please select a medical record from the table to view its certificate document.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        showCertificateDialog(m);
    }

    private void updateSelectedMedicalStatus(MedicalStatus newStatus) {
        MedicalRecord m = selectedReviewRecord();
        if (m == null) {
            JOptionPane.showMessageDialog(this, "Please select a medical record from the table first.",
                    "Selection Required", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (m.getStatus() == newStatus) {
            JOptionPane.showMessageDialog(this, "Medical ID #" + m.getMedicalId() + " is already " + newStatus.getDisplayName() + ".",
                    "No Change", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            medicalService.updateApprovalStatus(m.getMedicalId(), newStatus);
            String attendanceNote = newStatus == MedicalStatus.APPROVED
                    ? "\nAttendance updated: " + m.getCourseCode() + " " + m.getComponentType().getDisplayName()
                    + " session " + m.getSessionNumber() + " is now 'Absent (Approved Medical)'."
                    : "\nAttendance updated: the absence has no medical cover now.";
            JOptionPane.showMessageDialog(this,
                    "Medical ID #" + m.getMedicalId() + " status updated to " + newStatus.getDisplayName() + "." + attendanceNote,
                    "Status Updated", JOptionPane.INFORMATION_MESSAGE);
            refreshReviewTable();
        } catch (FAMSException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Cannot Update Medical", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Authorization/Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // =====================================================================================
    //  CERTIFICATE VIEWER
    // =====================================================================================

    private void showCertificateDialog(MedicalRecord m) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(owner instanceof Frame ? (Frame) owner : null,
                "Medical Certificate Document View - " + m.getReferenceNo(), true);
        dlg.setSize(720, 660);
        dlg.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel topInfo = new JPanel(new GridLayout(4, 1, 4, 4));
        topInfo.setBackground(Color.WHITE);
        topInfo.add(new JLabel("Student: " + m.getStudentName() + " (" + m.getStudentRegNo() + ")", SwingConstants.CENTER));
        topInfo.add(new JLabel("Reference No: " + m.getReferenceNo() + "     |     Status: " + m.getStatus().getDisplayName(), SwingConstants.CENTER));
        topInfo.add(new JLabel(m.getCourseCode() + " - " + m.getComponentType().getDisplayName() + " session "
                + m.getSessionNumber() + " (" + m.getAbsentDate() + ")", SwingConstants.CENTER));
        String path = m.getImagePath() == null ? "" : m.getImagePath();
        topInfo.add(new JLabel("Image File: " + (path.isBlank() ? "Not attached" : path), SwingConstants.CENTER));
        panel.add(topInfo, BorderLayout.NORTH);

        JPanel imgDisplayPanel = new JPanel(new BorderLayout());
        imgDisplayPanel.setBackground(new Color(248, 250, 252));
        imgDisplayPanel.setBorder(new CompoundBorder(new LineBorder(Theme.BORDER, 1), new EmptyBorder(12, 12, 12, 12)));

        File file = path.isBlank() ? null : new File(path);
        ImageIcon icon = (file != null && file.isFile()) ? new ImageIcon(file.getAbsolutePath()) : null;
        if (icon != null && icon.getIconWidth() > 0) {
            int maxW = 640, maxH = 400;
            double scale = Math.min(1.0, Math.min((double) maxW / icon.getIconWidth(), (double) maxH / icon.getIconHeight()));
            Image img = icon.getImage().getScaledInstance(
                    Math.max(1, (int) (icon.getIconWidth() * scale)),
                    Math.max(1, (int) (icon.getIconHeight() * scale)), Image.SCALE_SMOOTH);
            imgDisplayPanel.add(new JLabel(new ImageIcon(img), SwingConstants.CENTER), BorderLayout.CENTER);
        } else {
            JPanel certGraphic = new JPanel(new GridBagLayout());
            certGraphic.setBackground(new Color(254, 252, 232));
            certGraphic.setBorder(new LineBorder(new Color(202, 138, 4), 2));
            String note = path.isBlank()
                    ? "No certificate image was uploaded for this record (sample preview)."
                    : "The uploaded image file could not be found on this computer (sample preview).";
            JLabel lblCertText = new JLabel("<html><div style='text-align: center; padding: 20px;'>" +
                    "<h2 style='color:#854d0e;'>MEDICAL CERTIFICATE OF FITNESS</h2>" +
                    "<p><b>Faculty of Technology - Health Services</b></p><hr>" +
                    "<p>This certifies that <b>" + m.getStudentName() + "</b> (" + m.getStudentRegNo()
                    + ") was under medical treatment on " + m.getAbsentDate() + ".</p>" +
                    "<p><b>Diagnosis:</b> " + m.getReason() + "</p>" +
                    "<p style='margin-top:20px; color:#6b7280;'><i>" + note + "</i></p>" +
                    "</div></html>");
            certGraphic.add(lblCertText);
            imgDisplayPanel.add(certGraphic, BorderLayout.CENTER);
        }
        panel.add(new JScrollPane(imgDisplayPanel), BorderLayout.CENTER);

        JButton btnClose = Theme.createPrimaryButton("Close View");
        btnClose.addActionListener(e -> dlg.dispose());
        panel.add(btnClose, BorderLayout.SOUTH);

        dlg.add(panel);
        dlg.setVisible(true);
    }

    // =====================================================================================
    //  TABLE STYLING (maroon theme)
    // =====================================================================================

    private void styleMaroonTable(JTable target) {
        target.setFont(Theme.FONT_REGULAR);
        target.setRowHeight(28);
        target.setGridColor(Color.WHITE);
        target.setShowGrid(true);
        target.setSelectionBackground(new Color(218, 190, 197));
        target.setSelectionForeground(Theme.PRIMARY_DARK);
        target.setFillsViewportHeight(true);
        javax.swing.table.JTableHeader header = target.getTableHeader();
        header.setFont(Theme.FONT_SUBHEADER);
        header.setBackground(Theme.PRIMARY_DARK);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new javax.swing.table.TableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
                JLabel label = new JLabel(value == null ? "" : value.toString(), SwingConstants.CENTER);
                label.setOpaque(true);
                label.setFont(Theme.FONT_SUBHEADER);
                label.setForeground(Color.WHITE);
                label.setBackground(Theme.PRIMARY_DARK);
                label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.WHITE));
                return label;
            }
        });
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                if (!selected) {
                    c.setBackground(row % 2 == 0 ? new Color(245, 232, 236) : new Color(232, 211, 217));
                    c.setForeground(Theme.PRIMARY_DARK);
                }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        };
        for (int i = 0; i < target.getColumnCount(); i++) {
            if (target.getColumnClass(i) != Boolean.class) target.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }
}
