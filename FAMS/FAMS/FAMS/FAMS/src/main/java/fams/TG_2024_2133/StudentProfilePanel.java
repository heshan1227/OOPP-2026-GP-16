package fams.TG_2024_2133;

<<<<<<< HEAD:src/com/fams/gui/StudentProfilePanel.java
import com.fams.database.DBConnection;
import com.fams.database.MarksDAO;
import com.fams.database.UserDAO;
import com.fams.model.Role;
import com.fams.model.UndergraduateUser;
import com.fams.model.User;
import com.fams.service.MarksService;
=======
import fams.TG_2024_2113.MarksDAO;
import fams.TG_2024_2113.MarksManagement;
>>>>>>> 7d3243976a0642af534ee34d8a74c88cda0d97fa:FAMS/FAMS/src/main/java/fams/TG_2024_2133/StudentProfilePanel.java

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Read-only undergraduate directory for Lecturers and Admins.
 * Selecting a student shows the profile plus the student's approved results:
 * overall SGPA / CGPA (one value per student, not repeated per course),
 * exam eligibility and the per-course grade list - the same information the
 * student sees in "My Academic Results".
 */
public class StudentProfilePanel extends JPanel {
    private final UserDAO userDAO = new UserDAO();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Registration No.", "Full Name", "Batch", "Department", "Email", "Phone"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private final JTextField searchField = new JTextField(24);
    private final JLabel detailLabel = new JLabel("Select a student to view full profile details.");
    private List<UndergraduateUser> students = new ArrayList<>();

    // ── Results section (shown when a student is selected) ──────────────────
<<<<<<< HEAD:src/com/fams/gui/StudentProfilePanel.java
    private final MarksService marksService = new MarksService();
=======
    private final MarksManagement marksService = new MarksManagement();
>>>>>>> 7d3243976a0642af534ee34d8a74c88cda0d97fa:FAMS/FAMS/src/main/java/fams/TG_2024_2133/StudentProfilePanel.java
    private final JLabel lblSgpa = bigLabel("SGPA: —");
    private final JLabel lblCgpa = bigLabel("CGPA: —");
    private final JLabel lblElig = bigLabel("Eligibility: —");
    private final JLabel lblResultNote = new JLabel(" ");
    private final DefaultTableModel resultModel = new DefaultTableModel(
            new Object[]{"Course Code", "Course Name", "Credits", "Final Mark", "Grade", "Grade Points"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    public StudentProfilePanel() {
        setLayout(new BorderLayout(16, 16));
        setBackground(Theme.LIGHT_BG);
        setBorder(new EmptyBorder(22, 24, 22, 24));

        JLabel heading = new JLabel("Undergraduate Student Profiles");
        heading.setFont(Theme.FONT_HEADER);
        heading.setForeground(Theme.TEXT_MAIN);
        JLabel subtitle = new JLabel("Search and view student profile information (read-only).");
        subtitle.setFont(Theme.FONT_SMALL);
        subtitle.setForeground(Theme.TEXT_MUTED);

        JPanel top = new JPanel(new BorderLayout(4, 8));
        top.setOpaque(false);
        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.add(heading);
        titleBox.add(subtitle);
        top.add(titleBox, BorderLayout.NORTH);

        JPanel search = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        search.setOpaque(false);
        search.add(new JLabel("Search by name, registration number, batch or department:"));
        search.add(searchField);
        JButton clear = Theme.createSecondaryButton("Clear");
        search.add(clear);
        top.add(search, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));

        JPanel details = new JPanel(new BorderLayout(0, 8));
        details.setBackground(Color.WHITE);
        details.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER), new EmptyBorder(14, 16, 14, 16)));
        JLabel detailTitle = new JLabel("Student Profile & Results");
        detailTitle.setFont(Theme.FONT_SUBHEADER);
        detailTitle.setForeground(Theme.PRIMARY_DARK);
        detailLabel.setFont(Theme.FONT_REGULAR);
        detailLabel.setForeground(Theme.TEXT_MAIN);

        JPanel head = new JPanel(new BorderLayout(0, 6));
        head.setOpaque(false);
        head.add(detailTitle, BorderLayout.NORTH);
        head.add(detailLabel, BorderLayout.CENTER);

        // SGPA | CGPA | Eligibility summary strip
        JPanel summary = new JPanel(new GridLayout(1, 3, 16, 0));
        summary.setBackground(Theme.LIGHT_BG);
        summary.setBorder(new EmptyBorder(10, 12, 10, 12));
        summary.add(lblSgpa);
        summary.add(lblCgpa);
        summary.add(lblElig);

        lblResultNote.setFont(Theme.FONT_SMALL);
        lblResultNote.setForeground(Theme.TEXT_MUTED);

        JTable resultTable = new JTable(resultModel);
        resultTable.setRowHeight(26);
        resultTable.setFillsViewportHeight(true);
        resultTable.getTableHeader().setFont(Theme.FONT_SUBHEADER);
        resultTable.setFont(Theme.FONT_REGULAR);
        resultTable.setGridColor(Theme.BORDER);

        JPanel resultsBox = new JPanel(new BorderLayout(0, 6));
        resultsBox.setOpaque(false);
        resultsBox.add(summary, BorderLayout.NORTH);
        resultsBox.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        resultsBox.add(lblResultNote, BorderLayout.SOUTH);

        details.add(head, BorderLayout.NORTH);
        details.add(resultsBox, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, details);
        split.setResizeWeight(0.45);
        split.setBorder(null);
        add(split, BorderLayout.CENTER);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });
        clear.addActionListener(e -> searchField.setText(""));
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showSelectedStudent();
        });
        refreshStudents();
    }

    public void refreshStudents() {
<<<<<<< HEAD:src/com/fams/gui/StudentProfilePanel.java
        User current = com.fams.service.AuthService.getInstance().getCurrentUser();
=======
        User current = fams.TG_2024_2133.UserManagement.getInstance().getCurrentUser();
>>>>>>> 7d3243976a0642af534ee34d8a74c88cda0d97fa:FAMS/FAMS/src/main/java/fams/TG_2024_2133/StudentProfilePanel.java
        if (current == null || (current.getRole() != Role.LECTURER && current.getRole() != Role.ADMIN)) {
            students = new ArrayList<>();
            model.setRowCount(0);
            detailLabel.setText("Access denied. This feature is available to lecturers and administrators only.");
            return;
        }
        students = new ArrayList<>();
        for (User user : userDAO.getAllUsers()) {
            if (user instanceof UndergraduateUser) students.add((UndergraduateUser) user);
        }
        filter();
    }

    private void filter() {
        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
        model.setRowCount(0);
        for (UndergraduateUser student : students) {
            String searchable = String.join(" ", safe(student.getStudentRegNo()), safe(student.getFullName()),
                    safe(student.getBatch()), safe(student.getDepartment()), safe(student.getEmail()), safe(student.getPhone()));
            if (searchable.toLowerCase(Locale.ROOT).contains(query)) {
                model.addRow(new Object[]{student.getStudentRegNo(), student.getFullName(), student.getBatch(),
                        student.getDepartment(), student.getEmail(), student.getPhone()});
            }
        }
        detailLabel.setText("Select a student to view full profile details.");
        clearResults("Select a student to view results.");
    }

    private void showSelectedStudent() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return;
        String regNo = String.valueOf(table.getValueAt(viewRow, 0));
        for (UndergraduateUser s : students) {
            if (safe(s.getStudentRegNo()).equals(regNo)) {
                String status = (s.isRepeat() ? "Repeat student" : "Regular student")
                        + (s.isBatchMissed() ? " | Batch missed" : "");
                detailLabel.setText("<html><b>" + html(s.getFullName()) + "</b><br>Registration No.: " + html(s.getStudentRegNo())
                        + "<br>Batch: " + html(s.getBatch()) + "<br>Department: " + html(s.getDepartment())
                        + "<br>Email: " + html(s.getEmail()) + "<br>Phone: " + html(s.getPhone())
                        + "<br>Student Status: " + html(status) + "</html>");
                loadResults(s.getUserId());
                return;
            }
        }
    }

    // ─── Results (approved results only - same source the student sees) ─────

    private static JLabel bigLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 18));
        l.setForeground(Theme.PRIMARY_DARK);
        return l;
    }

    private void clearResults(String note) {
        resultModel.setRowCount(0);
        lblSgpa.setText("SGPA: —");
        lblCgpa.setText("CGPA: —");
        lblElig.setText("Eligibility: —");
        lblElig.setForeground(Theme.PRIMARY_DARK);
        lblResultNote.setText("  ℹ " + note);
    }

    private void loadResults(String studentUserId) {
        clearResults("No Admin-approved results are available for this student yet.");
        if (DBConnection.isUseMemoryFallback()) {
            lblResultNote.setText("  ℹ Connect to MySQL to view student results.");
            return;
        }
        try {
            // Per-course grade list (approved best attempts only)
            for (MarksDAO.GradeSummary g : marksService.getGrades(studentUserId, null)) {
                resultModel.addRow(new Object[]{g.courseCode(), g.courseName(), g.credits(),
                        String.format(Locale.US, "%.2f", g.finalMark()), g.letterGrade(),
                        String.format(Locale.US, "%.2f", g.gradePoint())});
            }
            // Overall SGPA (latest semester) and CGPA - one value for the whole student
            List<MarksDAO.GpaSummary> gpa = marksService.getGpaSummary(studentUserId);
            if (!gpa.isEmpty()) {
                lblSgpa.setText(String.format(Locale.US, "SGPA: %.2f", gpa.get(0).sgpa()));
                lblCgpa.setText(String.format(Locale.US, "CGPA: %.2f", gpa.get(0).cgpa()));
                lblResultNote.setText("  ℹ SGPA = latest semester (credit-weighted). CGPA = average of all semester SGPAs. "
                        + "Only Admin-approved results are shown.");
            }
            // Exam eligibility (latest snapshot)
            List<MarksDAO.Eligibility> elig = marksService.checkEligibility(studentUserId);
            if (elig.isEmpty() || "NOT_CHECKED".equals(elig.get(0).status())) {
                lblElig.setText("Eligibility: Not checked");
                lblElig.setForeground(Theme.TEXT_MUTED);
            } else {
                boolean ok = "ELIGIBLE".equals(elig.get(0).status());
                lblElig.setText("Eligibility: " + (ok ? "Eligible" : "Not Eligible"));
                lblElig.setForeground(ok ? Theme.SUCCESS : Theme.DANGER);
            }
        } catch (Exception ex) {
            lblResultNote.setText("  ⚠ Could not load results: " + ex.getMessage());
        }
    }

    private String safe(String value) { return value == null ? "" : value; }
    private String html(String value) { return safe(value).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }
}
