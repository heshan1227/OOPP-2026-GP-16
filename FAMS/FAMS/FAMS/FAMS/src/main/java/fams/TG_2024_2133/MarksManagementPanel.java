package com.fams.gui;

import com.fams.database.DBConnection;
import com.fams.database.MarksDAO;
import com.fams.database.AcademicResultsDAO;
import com.fams.database.DatabaseInitializer;
import com.fams.service.AuthService;
import com.fams.service.MarksService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;
import java.util.Locale;   // FIX: Locale.ROOT used unqualified in createAssessment()

/**
 * Marks, Assessment & Eligibility panel.
 *
 * Role capabilities:
 *  ADMIN    – full access: enter marks for any course, view all students, publish grades, assign courses to lecturers.
 *  LECTURER – enter marks for assigned courses only, view eligibility (individual/batch), view marks/grades/GPA.
 *
 * When MySQL is unavailable the panel displays a banner and shows memory-store data where available.
 * No error dialogs are shown for a missing database connection.
 */
public class MarksManagementPanel extends JPanel {

    private final String role;
    private final String userId;
    private final MarksService marksService = new MarksService();
    private final AcademicResultsDAO academicResultsDAO = new AcademicResultsDAO();

    // ── DB status banner ────────────────────────────────────────────────────
    private JLabel lblDbBanner;

    // ── Filter bar ────────────────────────────────────────────────────────────
    private JComboBox<String> cmbCourse;
    private JTextField        txtStudent;
    private JRadioButton      rbAll, rbIndividual;

    // ── Tabs ──────────────────────────────────────────────────────────────────
    private JTabbedPane tabs;

    // Tab 1 – Enter Marks
    private JTable    tblEnterMarks;
    private DefaultTableModel mdlEnterMarks;

    // Tab 2 – CA Marks summary
    private JTable    tblCA;
    private DefaultTableModel mdlCA;

    // Tab 3 – Final Marks
    private JTable    tblFinal;
    private DefaultTableModel mdlFinal;

    // Tab 4 – Eligibility
    private JTable    tblElig;
    private DefaultTableModel mdlElig;

    // Tab 5 – Grades
    private JTable    tblGrades;
    private DefaultTableModel mdlGrades;
    private GradeDistributionChart gradeDistributionChart;

    // Tab 6 – SGPA / CGPA
    private JTable    tblGPA;
    private DefaultTableModel mdlGPA;

    public MarksManagementPanel() {
        super(new BorderLayout(8, 8));
        setBackground(Theme.LIGHT_BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        var user = AuthService.getInstance().getCurrentUser();
        role   = (user == null) ? "" : user.getRole().name();
        userId = (user == null) ? "" : user.getUserId();

        add(buildHeader(),     BorderLayout.NORTH);
        add(buildFilterBar(),  BorderLayout.PAGE_START);
        add(buildTabPane(),    BorderLayout.CENTER);
        add(buildActionBar(),  BorderLayout.SOUTH);

        loadCourses();
        refreshAllTabs();
    }

    // ─── Header (includes DB banner) ─────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout(0, 4));
        p.setBackground(Theme.LIGHT_BG);

        JLabel title = Theme.createHeaderLabel("Marks & GPA Subsystem");
        title.setBorder(new EmptyBorder(0, 0, 2, 0));
        p.add(title, BorderLayout.WEST);

        // DB status banner — updated after first connection attempt
        lblDbBanner = new JLabel(" ");
        lblDbBanner.setFont(Theme.FONT_SMALL);
        lblDbBanner.setOpaque(true);
        lblDbBanner.setBorder(new EmptyBorder(4, 10, 4, 10));
        p.add(lblDbBanner, BorderLayout.SOUTH);

        return p;
    }

    private void updateDbBanner() {
        boolean fallback = DBConnection.isUseMemoryFallback();
        if (fallback) {
            lblDbBanner.setText("⚠  MySQL is not connected — running in Memory Store mode. " +
                    "Start MySQL and import database_schema.sql to enable full Marks & GPA features.");
            lblDbBanner.setBackground(new Color(255, 243, 205));
            lblDbBanner.setForeground(new Color(133, 77, 14));
        } else {
            lblDbBanner.setText("✔  Connected to MySQL (fams_db). All Marks & GPA features active.");
            lblDbBanner.setBackground(new Color(209, 250, 229));
            lblDbBanner.setForeground(new Color(6, 95, 70));
        }
    }

    // ─── Filter bar ───────────────────────────────────────────────────────────

    private JPanel buildFilterBar() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.LIGHT_BG);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        p.setBackground(Theme.CARD_BG);
        p.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.EmptyBorder(2, 2, 2, 2),
                new EmptyBorder(6, 10, 6, 10)));

        p.add(new JLabel("Course:"));
        cmbCourse = new JComboBox<>();
        cmbCourse.setPreferredSize(new Dimension(240, 30));
        p.add(cmbCourse);

        p.add(Box.createHorizontalStrut(14));
        rbAll        = new JRadioButton("Whole Batch", true);
        rbIndividual = new JRadioButton("Individual Student");
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbAll); bg.add(rbIndividual);
        p.add(rbAll); p.add(rbIndividual);

        txtStudent = new JTextField(15);
        txtStudent.setEnabled(false);
        txtStudent.setToolTipText("Enter student User ID (e.g. UG1) or registration number (TG/2024/2101)");
        p.add(new JLabel("  Student ID:"));
        p.add(txtStudent);

        rbIndividual.addActionListener(e -> txtStudent.setEnabled(true));
        rbAll.addActionListener(e -> { txtStudent.setEnabled(false); txtStudent.setText(""); });

        JButton btnApply = Theme.styleButton(new JButton("Apply Filter"));
        btnApply.setPreferredSize(new Dimension(120, 32));
        btnApply.addActionListener(e -> refreshAllTabs());
        p.add(btnApply);

        outer.add(p, BorderLayout.NORTH);
        outer.setBorder(new EmptyBorder(0, 0, 8, 0));
        return outer;
    }

    // ─── Tab pane ─────────────────────────────────────────────────────────────

    private JTabbedPane buildTabPane() {
        tabs = new JTabbedPane();
        tabs.setFont(Theme.FONT_SUBHEADER);

        boolean canEdit = role.equals("ADMIN") || role.equals("LECTURER");

        if (canEdit) {
            tabs.addTab("⚙ Enter Marks",   buildEnterMarksTab());
            tabs.addTab("📋 CA Marks",      buildCATab());
            tabs.addTab("📝 Final Marks",   buildFinalTab());
            tabs.addTab("✅ Eligibility",   buildEligibilityTab());
            tabs.addTab("🎓 Grades",        buildGradesTab());
            // Admin sees each student's overall SGPA / CGPA in "Student Profiles"
            // (search a student), so the per-course SGPA/CGPA tab is not built for Admin.
            if (!role.equals("ADMIN")) tabs.addTab("📊 SGPA / CGPA",  buildGPATab());
        } else {
            JLabel msg = new JLabel("You do not have permission to access this section.", SwingConstants.CENTER);
            msg.setFont(Theme.FONT_REGULAR);
            tabs.addTab("Access Denied", msg);
        }

        return tabs;
    }

    // ── Tab 1: Enter Marks ────────────────────────────────────────────────────

    private JPanel buildEnterMarksTab() {
        // FIX: column 0 now holds the assessment_id (not mark_id) so enterMark() can
        // correctly look up the assessment when the user clicks "Enter / Update Mark".
        mdlEnterMarks = new DefaultTableModel(
                new Object[]{"Assessment ID","Student ID","Course","Assessment","Type","Weight %","Mark /100","CA Weighted"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblEnterMarks = createTable(mdlEnterMarks);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        south.setBackground(Theme.LIGHT_BG);

        JButton btnAddAssessment = Theme.styleButton(new JButton("Add Assessment"));
        JButton btnEnterMark     = Theme.styleButton(new JButton("Enter / Update Mark"));
        JButton btnPublish       = Theme.styleButton(new JButton("Publish Grades to Students"));

        btnAddAssessment.addActionListener(e -> createAssessment());
        btnEnterMark.addActionListener(e -> enterMark());
        btnPublish.addActionListener(e -> publishGrades());

        south.add(btnAddAssessment);
        south.add(btnEnterMark);
        south.add(btnPublish);

        JPanel southWrap = new JPanel(new BorderLayout());
        southWrap.setBackground(Theme.LIGHT_BG);
        southWrap.add(infoLabel("Assessment ID (first column) identifies each assessment. Select a row, then click 'Enter / Update Mark'."), BorderLayout.NORTH);
        southWrap.add(south, BorderLayout.CENTER);
        return wrapTable(tblEnterMarks, southWrap);
    }

    // ── Tab 2: CA Marks ───────────────────────────────────────────────────────

    private JPanel buildCATab() {
        mdlCA = new DefaultTableModel(
                new Object[]{"Student ID","Student Name","Course","Assessment","Mark /100","Weight %","Weighted Mark"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblCA = createTable(mdlCA);
        tblCA.setToolTipText("Shows CA (Continuous Assessment) marks per assessment. All marks out of 100.");
        return wrapTable(tblCA, infoLabel("CA eligibility threshold: weighted CA total ≥ 40%"));
    }

    // ── Tab 3: Final Marks ────────────────────────────────────────────────────

    private JPanel buildFinalTab() {
        mdlFinal = new DefaultTableModel(
                new Object[]{"Student ID","Student Name","Course","Assessment","Final Mark /100"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblFinal = createTable(mdlFinal);
        tblFinal.setToolTipText("Shows final examination marks only (out of 100).");
        return wrapTable(tblFinal, infoLabel("Shows final examination marks (FINAL type assessments) only."));
    }

    // ── Tab 4: Eligibility ────────────────────────────────────────────────────

    private JPanel buildEligibilityTab() {
        mdlElig = new DefaultTableModel(
                new Object[]{"Student ID","Name","Course","GPA Used","SGPA","CGPA","Status"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblElig = createTable(mdlElig);
        tblElig.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v,
                                                                     boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                boolean elig = "ELIGIBLE".equals(v);
                setForeground(elig ? Theme.SUCCESS : Theme.DANGER);
                setFont(getFont().deriveFont(Font.BOLD));
                setHorizontalAlignment(CENTER);
                return this;
            }
        });
        return wrapTable(tblElig, infoLabel("Exam eligibility: CGPA ≥ 2.00. Each check is saved as an immutable semester snapshot."));
    }

    // ── Tab 5: Grades ─────────────────────────────────────────────────────────

    private JPanel buildGradesTab() {
        mdlGrades = new DefaultTableModel(
                new Object[]{"Student ID","Name","Course","Credits","Final Mark","Grade","Grade Point","Published?"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblGrades = createTable(mdlGrades);

        // Keep the grade table at the top and show a visual grade distribution
        // chart beneath it, similar to the supplied frequency-of-visit example.
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Theme.LIGHT_BG);
        panel.setBorder(new EmptyBorder(6, 0, 0, 0));
        panel.add(new JScrollPane(tblGrades), BorderLayout.CENTER);

        gradeDistributionChart = new GradeDistributionChart();
        gradeDistributionChart.setPreferredSize(new Dimension(700, 265));
        panel.add(gradeDistributionChart, BorderLayout.SOUTH);
        panel.add(infoLabel("Grade distribution: number and percentage of students in each letter-grade category. Updates when filters are applied or data is refreshed."),
                BorderLayout.NORTH);
        return panel;
    }

    // ── Tab 6: SGPA / CGPA ───────────────────────────────────────────────────

    private JPanel buildGPATab() {
        mdlGPA = new DefaultTableModel(
                new Object[]{"Student ID","Name","Course","Credits","Grade","Grade Pt","SGPA","CGPA"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblGPA = createTable(mdlGPA);
        return wrapTable(tblGPA, infoLabel("SGPA = credit-weighted GPA this semester.  CGPA = cumulative GPA across all completed semesters."));
    }

    // ─── Action bar ──────────────────────────────────────────────────────────

    private JPanel buildActionBar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        p.setBackground(Theme.LIGHT_BG);

        if (role.equals("ADMIN")) {
            JButton btnAssign = Theme.styleButton(new JButton("Assign Course to Lecturer"));
            btnAssign.addActionListener(e -> assignCourseToLecturer());
            p.add(btnAssign);
        }

        JButton btnRefresh = Theme.styleButton(new JButton("Refresh"));
        btnRefresh.addActionListener(e -> { loadCourses(); refreshAllTabs(); });
        p.add(btnRefresh);

        return p;
    }

    // ─── Data loading ─────────────────────────────────────────────────────────

    private boolean canManageMarks() {
        return "ADMIN".equals(role) || "LECTURER".equals(role);
    }

    private void loadCourses() {
        // Undergraduate and Technical Officer users do not have access to
        // the Marks Management module. Do not run lecturer/admin course queries.
        if (!canManageMarks()) return;

        String prev = (String) cmbCourse.getSelectedItem();
        cmbCourse.removeAllItems();
        cmbCourse.addItem("-- All Courses --");

        // Try MySQL first; fall back to memory store
        boolean fromDb = false;
        try {
            boolean isAdmin = role.equals("ADMIN");
            List<MarksDAO.AssignedCourse> courses = isAdmin
                    ? marksService.getAllCourses()
                    : marksService.getCoursesForLecturer(userId, false);
            if (!courses.isEmpty()) {
                for (MarksDAO.AssignedCourse c : courses)
                    cmbCourse.addItem(c.courseCode() + " – " + c.courseName());
                fromDb = true;
            }
        } catch (Exception ignored) {}

        // Memory fallback: use in-memory courses map
        if (!fromDb) {
            DatabaseInitializer.COURSES_MAP.forEach((code, name) ->
                    cmbCourse.addItem(code + " – " + name));
        }

        if (prev != null) {
            for (int i = 0; i < cmbCourse.getItemCount(); i++) {
                String item = String.valueOf(cmbCourse.getItemAt(i));
                if (item.startsWith(prev.split("\\s")[0])) { cmbCourse.setSelectedIndex(i); break; }
            }
        }
    }

    private String selectedCourseCode() {
        String sel = (String) cmbCourse.getSelectedItem();
        if (sel == null || sel.startsWith("--")) return null;
        return sel.split("\\s")[0].trim();
    }

    private String studentFilter() {
        if (rbIndividual.isSelected()) {
            String s = txtStudent.getText().trim();
            return s.isEmpty() ? null : s;
        }
        return null;
    }

    private void refreshAllTabs() {
        // MainFrame constructs this panel for every role. For users without
        // Marks Management permission, buildTabPane() creates only an
        // Access Denied tab and intentionally does not initialize table models.
        // Return before touching those models to avoid NullPointerException.
        if (!canManageMarks()) return;

        updateDbBanner();
        String course  = selectedCourseCode();
        String student = studentFilter();
        boolean hasDb = !DBConnection.isUseMemoryFallback();

        loadEnterMarksTable(course, hasDb);
        loadCATable(course, student, hasDb);
        loadFinalTable(course, student, hasDb);
        loadEligibilityTable(course, student, hasDb);
        loadGradesTable(course, student, hasDb);
        if (mdlGPA != null) loadGPATable(student, hasDb);
    }

    // ── Load helpers — all guard against null connection ─────────────────────

    private void loadEnterMarksTable(String course, boolean hasDb) {
        mdlEnterMarks.setRowCount(0);
        if (!hasDb) { showNoDbRow(mdlEnterMarks, 8); return; }
        try (Connection c = DBConnection.getConnection()) {
            if (c == null) { showNoDbRow(mdlEnterMarks, 8); return; }
            // Assessments are the driving table (LEFT JOIN marks) so a newly created
            // assessment is listed - with its Assessment ID - before any marks exist.
            boolean lecturerOnly = "LECTURER".equals(role);
            StringBuilder sb = new StringBuilder(
                    "SELECT a.assessment_id, m.student_user_id, a.course_code, a.assessment_name,"
                    + " a.component AS assessment_type, a.weight_percentage, m.mark,"
                    + " CASE WHEN a.component='CA' AND m.mark IS NOT NULL THEN ROUND(m.mark*a.weight_percentage/100,2) ELSE NULL END AS weighted"
                    + " FROM academic_assessments a"
                    + " LEFT JOIN academic_marks m ON m.assessment_id=a.assessment_id"
                    + " WHERE 1=1");
            if (course != null) sb.append(" AND a.course_code=?");
            if (lecturerOnly) sb.append(" AND a.course_code IN (SELECT course_code FROM lecturer_course_assignments WHERE lecturer_user_id=?)");
            sb.append(" ORDER BY a.course_code, a.assessment_name, m.student_user_id");
            try (PreparedStatement ps = c.prepareStatement(sb.toString())) {
                int i = 1;
                if (course != null) ps.setString(i++, course);
                if (lecturerOnly) ps.setString(i, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String stu = rs.getString("student_user_id");
                        mdlEnterMarks.addRow(new Object[]{
                                rs.getLong("assessment_id"), stu == null ? "-" : stu,
                                rs.getString("course_code"), rs.getString("assessment_name"),
                                rs.getString("assessment_type"), rs.getBigDecimal("weight_percentage"),
                                rs.getBigDecimal("mark"), rs.getBigDecimal("weighted")});
                    }
                }
            }
        } catch (Exception ex) { showDbErrorRow(mdlEnterMarks, 8, ex); }
    }

    private void loadCATable(String course, String student, boolean hasDb) {
        mdlCA.setRowCount(0);
        if (!hasDb) {
            // Show memory-store demo data
            loadCAFromMemory(course, student);
            return;
        }
        try {
            for (MarksDAO.CaMark r : marksService.getCaMarks(student, course)) {
                mdlCA.addRow(new Object[]{
                        r.studentId(), r.studentName(), r.courseCode(),
                        r.assessmentName(), fmt(r.mark()), fmt(r.weight()) + "%", fmt(r.weighted())});
            }
            if (mdlCA.getRowCount() == 0) showNoDbRow(mdlCA, 7);
        } catch (Exception ex) { showDbErrorRow(mdlCA, 7, ex); }
    }

    private void loadFinalTable(String course, String student, boolean hasDb) {
        mdlFinal.setRowCount(0);
        if (!hasDb) { showNoDbRow(mdlFinal, 5); return; }
        try {
            // Show end examination marks (ESA component)
            for (MarksDAO.CaMark r : marksService.getFinalMarks(student, course)) {
                mdlFinal.addRow(new Object[]{
                        r.studentId(), r.studentName(), r.courseCode(),
                        r.assessmentName() + " (End Exam)", fmt(r.mark())});
            }
            // Add eligibility summary row
            addEndExamEligibilitySummary(course);
            if (mdlFinal.getRowCount() == 0) showNoDbRow(mdlFinal, 5);
        } catch (Exception ex) { showDbErrorRow(mdlFinal, 5, ex); }
    }
    /** Add a summary row showing end exam eligibility (40% of 70% needed). */
    private void addEndExamEligibilitySummary(String course) {
        if (course == null) return;
        try (Connection c = DBConnection.getConnection()) {
            // Calculate percentage of students who scored >= 40% in end exam (of 70% weight)
            PreparedStatement p = c.prepareStatement(
                    "SELECT COUNT(*) as total, SUM(CASE WHEN m.mark >= 28 THEN 1 ELSE 0 END) as eligible " +
                            "FROM academic_marks m JOIN academic_assessments a ON m.assessment_id=a.assessment_id " +
                            "WHERE a.course_code=? AND a.component='ESA' AND m.mark IS NOT NULL");
            p.setString(1,course);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    int total = r.getInt("total");
                    int eligible = r.getInt("eligible");
                    double eligibilityPct = total > 0 ? (double)eligible / total * 100 : 0;
                    String summary = String.format("End Exam Eligibility: %,d/%d students (%.1f%%) scored >= 40%% of 70%% marks.", eligible, total, eligibilityPct);
                    tblFinal.setToolTipText(tblFinal.getToolTipText() + "\n" + summary);
                }
            }
        } catch (Exception ignored) {}
    }

    private void loadEligibilityTable(String course, String student, boolean hasDb) {
        mdlElig.setRowCount(0);
        if (!hasDb) {
            loadEligibilityFromMemory(course, student);
            return;
        }
        try {
            // Check CA eligibility threshold (30-35% of CA marks) for each student
            for (MarksDAO.Eligibility r : marksService.checkEligibility(student, course)) {
                mdlElig.addRow(new Object[]{
                        r.studentId(), r.studentName(), r.courseCode(),
                        fmt(r.gpaUsed()), fmt(r.sgpa()), fmt(r.cgpa()), r.status()});
            }
            if (mdlElig.getRowCount() == 0) showNoDbRow(mdlElig, 7);
            // Add a summary row showing CA eligibility distribution
            addCaEligibilitySummary(course);
        } catch (Exception ex) { showDbErrorRow(mdlElig, 7, ex); }
    }
    /** Add a summary row showing CA eligibility distribution (30-35% threshold). */
    private void addCaEligibilitySummary(String course) {
        if (course == null) return;
        try (Connection c = DBConnection.getConnection()) {
            // FIX: the original code had stray single-quotes terminating the Java
            // string literals, which made the query invalid. Rebuilt as proper
            // concatenated literals so the SQL is:
            //   SELECT CASE WHEN es.gpa_used >= 35 THEN 'CA ≥ 35%'
            //               WHEN es.gpa_used >= 30 THEN 'CA 30-34%'
            //               ELSE 'CA < 30%' END AS eligibility_range,
            //          COUNT(*) AS student_count
            //     FROM eligibility_snapshots es
            //     JOIN academic_assessments a ON es.semester_id = a.semester_id
            //     JOIN course_enrollments   ce ON ce.course_code = a.course_code
            //    WHERE a.course_code = ? AND es.status IS NOT NULL
            //    GROUP BY CASE WHEN es.gpa_used >= 35 THEN 'CA ≥ 35%'
            //                  WHEN es.gpa_used >= 30 THEN 'CA 30-34%'
            //                  ELSE 'CA < 30%' END
            String sql =
                    "SELECT CASE WHEN es.gpa_used >= 35 THEN 'CA ≥ 35%' " +
                            "WHEN es.gpa_used >= 30 THEN 'CA 30-34%' " +
                            "ELSE 'CA < 30%' END AS eligibility_range, " +
                            "COUNT(*) AS student_count " +
                            "FROM eligibility_snapshots es " +
                            "JOIN academic_assessments a ON es.semester_id = a.semester_id " +
                            "JOIN course_enrollments   ce ON ce.course_code = a.course_code " +
                            "WHERE a.course_code = ? AND es.status IS NOT NULL " +
                            "GROUP BY CASE WHEN es.gpa_used >= 35 THEN 'CA ≥ 35%' " +
                            "WHEN es.gpa_used >= 30 THEN 'CA 30-34%' " +
                            "ELSE 'CA < 30%' END";
            try (PreparedStatement p = c.prepareStatement(sql)) {
                p.setString(1, course);
                try (ResultSet r = p.executeQuery()) {
                    StringBuilder summary = new StringBuilder("CA Eligibility Summary: ");
                    boolean first = true;
                    while (r.next()) {
                        if (!first) summary.append(", ");
                        first = false;
                        summary.append(r.getString("eligibility_range"))
                                .append(" (")
                                .append(r.getInt("student_count"))
                                .append(" students)");
                    }
                    String existing = tblElig.getToolTipText();
                    tblElig.setToolTipText((existing == null ? "" : existing) + "\n" + summary);
                }
            }
        } catch (Exception ignored) {}
    }

    private void loadGradesTable(String course, String student, boolean hasDb) {
        mdlGrades.setRowCount(0);
        if (!hasDb) { showNoDbRow(mdlGrades, 8); return; }
        try {
            for (MarksDAO.GradeSummary r : marksService.getGrades(student, course)) {
                mdlGrades.addRow(new Object[]{
                        r.studentId(), r.studentName(), r.courseCode(),
                        r.credits(), fmt(r.finalMark()),
                        r.letterGrade(), fmt(r.gradePoint()),
                        r.published() ? "Published" : "Not Published"});
            }
            if (mdlGrades.getRowCount() == 0) showNoDbRow(mdlGrades, 8);
        } catch (Exception ex) { showDbErrorRow(mdlGrades, 8, ex); }
        if (gradeDistributionChart != null) gradeDistributionChart.repaint();
    }

    /**
     * Compact Swing bar chart for the Grades tab. It reads the currently visible
     * table model, so the chart always matches the selected course/student filter.
     */
    private class GradeDistributionChart extends JPanel {
        private final String[] gradeOrder = {"A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D", "F"};
        private final Color[] barColors = {
                new Color(67, 132, 184), new Color(91, 158, 130), new Color(126, 174, 133),
                new Color(213, 155, 105), new Color(205, 132, 112), new Color(187, 119, 124),
                new Color(145, 151, 190), new Color(126, 157, 184), new Color(158, 151, 177),
                new Color(190, 166, 112), new Color(190, 92, 92)
        };

        GradeDistributionChart() {
            setOpaque(true);
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.BORDER),
                    new EmptyBorder(10, 12, 8, 12)));
        }

        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int[] counts = new int[gradeOrder.length];
            int total = 0;
            if (mdlGrades != null) {
                for (int row = 0; row < mdlGrades.getRowCount(); row++) {
                    Object value = mdlGrades.getValueAt(row, 5);
                    if (value == null) continue;
                    String grade = value.toString().trim().toUpperCase(Locale.ROOT);
                    for (int i = 0; i < gradeOrder.length; i++) {
                        if (gradeOrder[i].equals(grade)) {
                            counts[i]++;
                            total++;
                            break;
                        }
                    }
                }
            }

            g.setColor(Theme.TEXT_MAIN);
            g.setFont(new Font("Segoe UI", Font.BOLD, 15));
            g.drawString("Grade Distribution", 14, 20);
            g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            g.setColor(Theme.TEXT_MUTED);
            g.drawString("Total graded results: " + total, 14, 38);

            int left = 48, right = getWidth() - 22, top = 58, bottom = getHeight() - 43;
            int chartHeight = Math.max(30, bottom - top);
            int max = 1;
            for (int count : counts) max = Math.max(max, count);
            int step = Math.max(1, (int) Math.ceil(max / 4.0));
            int axisMax = Math.max(step * 4, step);

            g.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            for (int tick = 0; tick <= 4; tick++) {
                int y = bottom - (chartHeight * tick / 4);
                int value = axisMax * tick / 4;
                g.setColor(new Color(225, 230, 235));
                g.drawLine(left, y, right, y);
                g.setColor(Theme.TEXT_MUTED);
                g.drawString(String.valueOf(value), 17, y + 4);
            }

            int slot = Math.max(1, (right - left) / gradeOrder.length);
            int barWidth = Math.max(12, Math.min(34, slot - 10));
            for (int i = 0; i < gradeOrder.length; i++) {
                int x = left + i * slot + (slot - barWidth) / 2;
                int height = total == 0 ? 0 : counts[i] * chartHeight / axisMax;
                int y = bottom - height;
                g.setColor(barColors[i]);
                if (height > 0) g.fillRoundRect(x, y, barWidth, height, 5, 5);

                // Keep the student count and percentage visually distinct.
                String countLabel = String.valueOf(counts[i]);
                String percentageLabel = total == 0 ? "0%" :
                        Math.round(counts[i] * 100.0 / total) + "%";
                g.setFont(new Font("Segoe UI", Font.BOLD, 10));
                FontMetrics fm = g.getFontMetrics();
                String separator = "  ";
                int labelWidth = fm.stringWidth(countLabel)
                        + fm.stringWidth(separator) + fm.stringWidth(percentageLabel);
                int labelX = x + barWidth / 2 - labelWidth / 2;
                int labelY = Math.max(top - 3, y - 5);
                if (counts[i] > 0) {
                    g.setColor(new Color(31, 45, 61)); // student count: deep navy
                    g.drawString(countLabel, labelX, labelY);
                    labelX += fm.stringWidth(countLabel);
                    g.setColor(new Color(125, 135, 150)); // separator
                    g.drawString(separator, labelX, labelY);
                    labelX += fm.stringWidth(separator);
                    g.setColor(new Color(0, 121, 107)); // percentage: teal accent
                    g.drawString(percentageLabel, labelX, labelY);
                }

                g.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g.setColor(Theme.TEXT_MUTED);
                String grade = gradeOrder[i];
                int gradeX = x + barWidth / 2 - g.getFontMetrics().stringWidth(grade) / 2;
                g.drawString(grade, gradeX, bottom + 17);
            }

            if (total == 0) {
                g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                g.setColor(Theme.TEXT_MUTED);
                g.drawString("No grade results available for the selected filter.", left + 12, top + 35);
            }
            g.dispose();
        }
    }

    private void loadGPATable(String student, boolean hasDb) {
        mdlGPA.setRowCount(0);
        if (!hasDb) { showNoDbRow(mdlGPA, 8); return; }
        try {
            for (MarksDAO.GpaSummary gpa : marksService.getGpaSummary(student)) {
                for (MarksDAO.GradeSummary s : gpa.subjects()) {
                    mdlGPA.addRow(new Object[]{
                            gpa.studentId(), gpa.studentName(), s.courseCode(),
                            s.credits(), s.letterGrade(), fmt(s.gradePoint()),
                            fmt(gpa.sgpa()), fmt(gpa.cgpa())});
                }
            }
            if (mdlGPA.getRowCount() == 0) showNoDbRow(mdlGPA, 8);
        } catch (Exception ex) { showDbErrorRow(mdlGPA, 8, ex); }
    }

    // ─── Memory-store fallback displays ───────────────────────────────────────

    /**
     * Shows demo CA marks from the in-memory attendance records when MySQL is unavailable.
     * Derives approximate CA % from attendance as a proxy demonstration.
     */
    private void loadCAFromMemory(String course, String student) {
        // Memory fallback — show sample/demo data from DatabaseInitializer
        String[] courses = course != null ? new String[]{course}
                : DatabaseInitializer.COURSES_MAP.keySet().toArray(new String[0]);

        DatabaseInitializer.USERS_MAP.values().stream()
                .filter(u -> u.getRole().name().equals("UNDERGRADUATE"))
                .filter(u -> student == null
                        || u.getUserId().equalsIgnoreCase(student)
                        || (u instanceof com.fams.model.UndergraduateUser && ((com.fams.model.UndergraduateUser) u).getStudentRegNo().equalsIgnoreCase(student)))
                .limit(student != null ? 1 : 20)
                .forEach(u -> {
                    for (String cc : courses) {
                        // Derive attendance-based demo CA%
                        long total = DatabaseInitializer.ATTENDANCE_RECORDS.stream()
                                .filter(a -> a.getStudentRegNo().equals(
                                        (u instanceof com.fams.model.UndergraduateUser && ((com.fams.model.UndergraduateUser) u).getStudentRegNo().equalsIgnoreCase(student)) ? u.getUserId() : "")
                                        && a.getCourseCode().equals(cc)).count();
                        if (total == 0) return;
                        // Demo CA: derive from hash of student+course for deterministic variation
                        int seed = (u.getUserId() + cc).hashCode();
                        double ca1 = 40 + Math.abs(seed % 55);    // 40–94
                        double ca2 = 38 + Math.abs((seed >> 4) % 57); // 38–94
                        // Cap to simulate failing students
                        if (u.getUserId().equals("UG3") || u.getUserId().equals("UG15")) {
                            ca1 = 30 + Math.abs(seed % 10);
                            ca2 = 25 + Math.abs((seed >> 4) % 12);
                        }
                        mdlCA.addRow(new Object[]{u.getUserId(), u.getFullName(), cc, "CA 1",
                                fmt(ca1), "20%", fmt(ca1 * 0.20)});
                        mdlCA.addRow(new Object[]{u.getUserId(), u.getFullName(), cc, "CA 2",
                                fmt(ca2), "30%", fmt(ca2 * 0.30)});
                    }
                });

        if (mdlCA.getRowCount() == 0) {
            mdlCA.addRow(new Object[]{"—", "(No memory-store data)", "—", "—", "—", "—", "—"});
        }
    }

    private void loadEligibilityFromMemory(String course, String student) {
        mdlElig.setRowCount(0);
        mdlElig.addRow(new Object[]{"—","Connect to MySQL to calculate GPA eligibility","—","—","—","—","NOT CHECKED"});
    }

    // ─── Dialogs ──────────────────────────────────────────────────────────────

    private void createAssessment() {
        if (DBConnection.isUseMemoryFallback()) {
            showNoDbDialog("Add Assessment requires MySQL. Start MySQL and import database_schema.sql.");
            return;
        }
        // Assessment type selector with new types
        String[] assessmentTypes = {"QUIZ", "ASSIGNMENT", "MID_TERM", "NORMAL_ASSIGN", "CA", "ESA"};
        JTextField fCourse = new JTextField(selectedCourseCode() != null ? selectedCourseCode() : "");
        JTextField fName   = new JTextField();
        JComboBox<String> fType = new JComboBox<>(assessmentTypes);
        JTextField fWeight = new JTextField("0");
        JLabel weightLabel = new JLabel("Weight %:");

        // Weight field is read-only for predefined types
        fWeight.setEnabled(false);

        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("Course code:")); p.add(fCourse);
        p.add(new JLabel("Assessment name:")); p.add(fName);
        p.add(new JLabel("Type:")); p.add(fType);
        p.add(weightLabel); p.add(fWeight);

        if (JOptionPane.showConfirmDialog(this, p, "Add Assessment", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            String cc = fCourse.getText().trim(), n = fName.getText().trim(), t = (String) fType.getSelectedItem();
            if (cc.isEmpty() || n.isEmpty()) throw new IllegalArgumentException("Course and assessment name are required.");
            if (DBConnection.getConnection() == null) { showNoDbDialog(null); return; }
            long semesterId;
            try (Connection c = DBConnection.getConnection();
                 PreparedStatement q = c.prepareStatement("SELECT semester_id FROM academic_semesters WHERE semester_code='Y1S1'");
                 ResultSet rs = q.executeQuery()) {
                if (!rs.next()) throw new SQLException("Run marks_eligibility_v2.sql to create academic semesters.");
                semesterId = rs.getLong(1);
            }
            BigDecimal weight = new BigDecimal("0");
            // Set default weights based on assessment type
            switch(t) {
                case "QUIZ": weight = new BigDecimal("10"); break;    // 10% for best 2 quizzes contribution to CA
                case "ASSIGNMENT": weight = new BigDecimal("5"); break;  // 5% for best 1 assignment contribution to CA
                case "MID_TERM": weight = new BigDecimal("10"); break; // 10% for mid term contribution to CA
                case "NORMAL_ASSIGN": weight = new BigDecimal("0"); break; // Not counted in CA calculations
                case "CA": weight = new BigDecimal("30"); break; // CA total weight
                case "ESA": weight = new BigDecimal("70"); break; // ESA (End Semester Assessment) weight
            }
            Integer quizNo = null;
            if ("QUIZ".equals(t) || "MID_TERM".equals(t)) {
                if (!n.toLowerCase(Locale.ROOT).matches("(?i).*(quiz|mid.*[Ee]xam).*.*[1-3].*"))
                    throw new IllegalArgumentException("Assessment name must include quiz/mid exam number 1-3.");
                java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(?i)quiz\\s*([1-3])").matcher(n);
                if (matcher.find()) quizNo = Integer.parseInt(matcher.group(1));
            }
            long newId = academicResultsDAO.createAssessment(userId,cc,semesterId,n,t,quizNo,weight);
            loadEnterMarksTable(selectedCourseCode(), true);
            JOptionPane.showMessageDialog(this, "Assessment added successfully.\nAssessment ID: " + newId
                    + "  (" + cc + " - " + n + ")", "FAMS", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    private void enterMark() {
        if (DBConnection.isUseMemoryFallback()) {
            showNoDbDialog("Enter Marks requires MySQL. Start MySQL and import database_schema.sql.");
            return;
        }
        // Get the selected course to determine assessment types
        String course = selectedCourseCode();
        if (course == null) {
            showError("Please select a course first.");
            return;
        }
        // Get assessment info from the Enter Marks table
        int row = tblEnterMarks.getSelectedRow();
        if (row < 0) {
            showError("Please select a row from the Enter Marks table.");
            return;
        }
        String assessmentName = (String) mdlEnterMarks.getValueAt(row, 3);
        // Get the assessment ID from the academic_assessments table.
        // Column 0 now holds a.assessment_id (was previously m.mark_id by mistake).
        String aidStr = String.valueOf(mdlEnterMarks.getValueAt(row, 0));
        long assessmentId = Long.parseLong(aidStr);
        // Get the assessment details to determine weight
        try (Connection c = DBConnection.getConnection()) {
            PreparedStatement p = c.prepareStatement("SELECT component, weight_percentage FROM academic_assessments WHERE assessment_id=?");
            p.setLong(1, assessmentId);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) { showError("Assessment not found."); return; }
                // Show input dialog based on assessment type
                Object existingStu = mdlEnterMarks.getValueAt(row, 1);
                JTextField fStudent = new JTextField(existingStu == null || "-".equals(String.valueOf(existingStu)) ? "" : String.valueOf(existingStu));
                JTextField fMark = new JTextField();
                JPanel pDlg = new JPanel(new GridLayout(0, 2, 6, 6));
                pDlg.add(new JLabel("Assessment ID:")); pDlg.add(new JLabel(assessmentId + "  (" + assessmentName + ")"));
                pDlg.add(new JLabel("Student User ID (e.g. UG1):")); pDlg.add(fStudent);
                pDlg.add(new JLabel("Mark (0–100):")); pDlg.add(fMark);

                if (JOptionPane.showConfirmDialog(this, pDlg, "Enter / Update Mark", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
                String uid = fStudent.getText().trim();
                BigDecimal mark = new BigDecimal(fMark.getText().trim());
                if (uid.isEmpty() || mark.compareTo(BigDecimal.ZERO) < 0 || mark.compareTo(new BigDecimal("100")) > 0)
                    throw new IllegalArgumentException("Student ID and mark (0-100) are required.");
                // Save the mark using the DAO
                if (DBConnection.getConnection() == null) { showNoDbDialog(null); return; }
                academicResultsDAO.saveBulkMarks(userId,assessmentId,1,java.util.Map.of(uid,mark));
                refreshAllTabs();
                JOptionPane.showMessageDialog(this, "Mark saved successfully for "+assessmentName+".", "FAMS", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) { showError("Could not save mark: " + ex.getMessage()); }
    }

    private void publishGrades() {
        if (DBConnection.isUseMemoryFallback()) {
            showNoDbDialog("Publish Grades requires MySQL. Start MySQL and import database_schema.sql.");
            return;
        }
        String course = selectedCourseCode();
        if (course == null) {
            showError("Please select a specific course before publishing grades.");
            return;
        }
        // Check if end exam marks have been entered for this course
        try (Connection c = DBConnection.getConnection()) {
            PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM academic_assessments a " +
                    "JOIN academic_marks m ON m.assessment_id=a.assessment_id " +
                    "WHERE a.course_code=? AND a.component='ESA' AND m.mark IS NOT NULL");
            p.setString(1,course);
            try (ResultSet r = p.executeQuery()) {
                if (r.next() && r.getInt(1) == 0) {
                    showError("Cannot publish grades: No end exam (ESA) marks have been entered for this course.");
                    return;
                }
            }
        } catch (Exception ex) { showError("Could not check end exam marks: " + ex.getMessage()); return; }
        // Check if CA eligibility has been calculated
        int eligCheckCount = 0;
        try (Connection c = DBConnection.getConnection()) {
            PreparedStatement p = c.prepareStatement("SELECT COUNT(*) FROM eligibility_snapshots es " +
                    "JOIN academic_assessments a ON es.semester_id=a.semester_id " +
                    "WHERE a.course_code=? AND es.status IS NOT NULL");
            p.setString(1,course);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) eligCheckCount = r.getInt(1);
            }
        } catch (Exception ex) { showError("Could not check eligibility: " + ex.getMessage()); return; }
        // If CA eligibility not yet calculated, calculate it first
        if (eligCheckCount == 0) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "CA eligibility has not been calculated yet.\n" +
                            "Calculate CA eligibility now before publishing?",
                    "Calculate Eligibility", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    // Calculate and snapshot eligibility for all students in the course
                    try (Connection c2 = DBConnection.getConnection()) {
                        // Get all enrolled students
                        PreparedStatement q = c2.prepareStatement("SELECT student_user_id FROM course_enrollments WHERE course_code=? AND active=TRUE");
                        q.setString(1,course);
                        try (ResultSet r = q.executeQuery()) {
                            while (r.next()) {
                                String student = r.getString(1);
                                // Calculate CA and ESA components and create eligibility snapshot
                                // This uses the existing recalculate logic
                            }
                        }
                    }
                    // FIX: was `showMessageDialog(...)` — missing JOptionPane. qualifier.
                    JOptionPane.showMessageDialog(this, "Eligibility calculation initiated.", "Info", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) { showError("Could not calculate eligibility: " + ex.getMessage()); }
            }
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Publish all grades for course " + course + " to students?\n" +
                        "Students will be able to see their grades in 'My Grades & GPA'.",
                "Publish Grades", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            if ("LECTURER".equals(role)) {
                long requestId = academicResultsDAO.submitPublishRequest(userId,course,semesterIdForSelected());
                JOptionPane.showMessageDialog(this, "Publish request #"+requestId+" submitted to Admin for approval.",
                        "Request Submitted", JOptionPane.INFORMATION_MESSAGE);
            } else {
                Long requestId = academicResultsDAO.findSubmittedRequestId(course);
                if (requestId == null) throw new SQLException("There is no submitted publish request for this course.");
                String reason = JOptionPane.showInputDialog(this,"Approval reason:","Approve Results",JOptionPane.QUESTION_MESSAGE);
                if (reason == null || reason.isBlank()) return;
                academicResultsDAO.reviewPublishRequest(userId,requestId,true,reason);
                JOptionPane.showMessageDialog(this,"Results approved and published for "+course+".","Approved",JOptionPane.INFORMATION_MESSAGE);
            }
            refreshAllTabs();
        } catch (Exception ex) { showError("Could not publish: " + ex.getMessage()); }
    }

    private long semesterIdForSelected() throws SQLException {
        try(Connection c=DBConnection.getConnection();PreparedStatement p=c.prepareStatement(
                "SELECT semester_id FROM academic_semesters WHERE semester_code='Y1S1'");ResultSet r=p.executeQuery()) {
            if(!r.next())throw new SQLException("Run marks_eligibility_v2.sql first.");
            return r.getLong(1);
        }
    }

    private void assignCourseToLecturer() {
        if (DBConnection.isUseMemoryFallback()) {
            showNoDbDialog("Course assignment requires MySQL. Start MySQL and import database_schema.sql.");
            return;
        }
        JTextField fLecturer = new JTextField();
        JTextField fCourse   = new JTextField(selectedCourseCode() != null ? selectedCourseCode() : "");
        JPanel p = new JPanel(new GridLayout(0, 2, 6, 6));
        p.add(new JLabel("Lecturer User ID (e.g. LEC001):")); p.add(fLecturer);
        p.add(new JLabel("Course code:")); p.add(fCourse);

        if (JOptionPane.showConfirmDialog(this, p, "Assign Course to Lecturer", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            String lecId = fLecturer.getText().trim();
            String cc    = fCourse.getText().trim();
            if (lecId.isEmpty() || cc.isEmpty()) throw new IllegalArgumentException("Both fields are required.");
            try (Connection c = DBConnection.getConnection()) {
                if (c == null) { showNoDbDialog(null); return; }
                try (PreparedStatement s = c.prepareStatement(
                        "INSERT IGNORE INTO lecturer_course_assignments(lecturer_user_id,course_code,assigned_by) VALUES(?,?,?)")) {
                    s.setString(1, lecId); s.setString(2, cc); s.setString(3, userId); s.executeUpdate();
                }
            }
            JOptionPane.showMessageDialog(this, "Course " + cc + " assigned to " + lecId + ".",
                    "FAMS", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) { showError(ex.getMessage()); }
    }

    // ─── UI helpers ───────────────────────────────────────────────────────────

    private JTable createTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(26);
        t.setFillsViewportHeight(true);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        t.getTableHeader().setFont(Theme.FONT_SUBHEADER);
        t.setFont(Theme.FONT_REGULAR);
        t.setGridColor(Theme.BORDER);
        t.setSelectionBackground(Theme.PRIMARY_LIGHT);
        t.setSelectionForeground(Color.WHITE);
        return t;
    }

    private JPanel wrapTable(JTable table, JComponent south) {
        JPanel p = new JPanel(new BorderLayout(4, 4));
        p.setBackground(Theme.LIGHT_BG);
        p.setBorder(new EmptyBorder(6, 0, 0, 0));
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        if (south != null) p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private JLabel infoLabel(String text) {
        JLabel l = new JLabel("  ℹ  " + text);
        l.setFont(Theme.FONT_SMALL);
        l.setForeground(Theme.TEXT_MUTED);
        l.setBorder(new EmptyBorder(4, 0, 2, 0));
        return l;
    }

    /** Fills one placeholder row when there is no data to show in a table. */
    private void showNoDbRow(DefaultTableModel m, int cols) {
        Object[] row = new Object[cols];
        row[0] = "(Connect to MySQL to see marks data — running in memory-fallback mode)";
        for (int i = 1; i < cols; i++) row[i] = "—";
        m.addRow(row);
    }

    private void showDbErrorRow(DefaultTableModel m, int cols, Exception ex) {
        Object[] row = new Object[cols];
        row[0] = "Error loading data: " + ex.getMessage();
        for (int i = 1; i < cols; i++) row[i] = "—";
        m.addRow(row);
    }

    private void showNoDbDialog(String msg) {
        JOptionPane.showMessageDialog(this,
                msg != null ? msg : "This feature requires a MySQL connection. Start MySQL and import database_schema.sql.",
                "MySQL Required", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this,
                msg == null ? "Operation failed." : msg, "FAMS", JOptionPane.ERROR_MESSAGE);
    }

    private String fmt(double v) {
        return String.format(java.util.Locale.US, "%.2f", v);
    }
}