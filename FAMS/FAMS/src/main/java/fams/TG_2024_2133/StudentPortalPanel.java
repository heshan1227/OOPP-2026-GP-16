package fams.TG_2024_2133;

import fams.TG_2024_2113.MarksDAO;
import fams.TG_2024_2113.GPAManagement;
import fams.TG_2024_2113.GradeResult;
import fams.TG_2024_2113.MarksManagement;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.List;

/**
 * Read-only personal portal for UNDERGRADUATE students.
 * Shows only published grades and GPA, filtered by selected course.
 * Students cannot see unpublished marks or other students' data.
 */
public class StudentPortalPanel extends JPanel {

    private final User user = UserManagement.getInstance().getCurrentUser();
    private final MarksManagement marksService = new MarksManagement();
    private final JTabbedPane tabs = new JTabbedPane();

    // Course filter
    private JComboBox<String> cmbCourse;

    // Tab models
    private DefaultTableModel mdlGrades;
    private DefaultTableModel mdlCA;
    private DefaultTableModel mdlFinal;

    // Summary labels
    private JLabel lblSgpa;
    private JLabel lblCgpa;
    private JLabel lblGpaStatus;

    public StudentPortalPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.LIGHT_BG);
        setBorder(new EmptyBorder(18, 18, 18, 18));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildFilterBar(), BorderLayout.PAGE_START);
        add(tabs, BorderLayout.CENTER);

        buildTabs();
        loadCourses();
        refresh();
    }

    // ─── Header ───────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.LIGHT_BG);
        JLabel title = Theme.createHeaderLabel("My Academic Results");
        title.setBorder(new EmptyBorder(0, 0, 4, 0));
        p.add(title, BorderLayout.WEST);

        if (user != null) {
            String regNo = (user instanceof UndergraduateUser) ? ((UndergraduateUser) user).getStudentRegNo() : "";
            JLabel sub = new JLabel("Registered as: " + user.getFullName() +
                (!regNo.isEmpty() ? "  |  Reg No: " + regNo : ""));
            sub.setFont(Theme.FONT_SMALL);
            sub.setForeground(Theme.TEXT_MUTED);
            p.add(sub, BorderLayout.SOUTH);
        }
        return p;
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

        p.add(new JLabel("Filter by Course:"));
        cmbCourse = new JComboBox<>();
        cmbCourse.setPreferredSize(new Dimension(280, 30));
        p.add(cmbCourse);

        JButton btnApply = Theme.styleButton(new JButton("Apply Filter"));
        btnApply.setPreferredSize(new Dimension(120, 32));
        btnApply.addActionListener(e -> refresh());
        p.add(btnApply);

        outer.add(p, BorderLayout.NORTH);
        outer.setBorder(new EmptyBorder(0, 0, 8, 0));
        return outer;
    }

    // ─── Tabs ─────────────────────────────────────────────────────────────────

    private void buildTabs() {
        tabs.setFont(Theme.FONT_SUBHEADER);
        tabs.addTab("🎓 My Grades & GPA", buildGradesPanel());
        tabs.addTab("📋 CA Marks",         buildCaPanel());
        tabs.addTab("📝 Final Marks",       buildFinalPanel());
    }

    // Tab 1 – Grades & GPA
    private JPanel buildGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Theme.LIGHT_BG);
        panel.setBorder(new EmptyBorder(12, 4, 4, 4));

        // GPA summary card
        JPanel gpaCard = new JPanel(new GridLayout(1, 3, 20, 0));
        gpaCard.setBackground(Theme.CARD_BG);
        gpaCard.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.EmptyBorder(2, 2, 2, 2),
                new EmptyBorder(12, 18, 12, 18)));

        lblSgpa = makeBigLabel("SGPA: —");
        lblCgpa = makeBigLabel("CGPA: —");
        lblGpaStatus = new JLabel("Results will appear once grades are published.", SwingConstants.CENTER);
        lblGpaStatus.setFont(Theme.FONT_SMALL);
        lblGpaStatus.setForeground(Theme.TEXT_MUTED);

        gpaCard.add(lblSgpa);
        gpaCard.add(lblCgpa);
        gpaCard.add(lblGpaStatus);
        panel.add(gpaCard, BorderLayout.NORTH);

        mdlGrades = new DefaultTableModel(
            new Object[]{"Course Code","Course Name","Credits","Final Mark","Grade","Grade Points"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = styledTable(mdlGrades);
        t.setToolTipText("Only published grades are shown. Pending results appear once the lecturer publishes them.");
        panel.add(new JScrollPane(t), BorderLayout.CENTER);

        JLabel note = new JLabel("  ℹ Grades are published by your lecturer. Contact your lecturer if results are missing.");
        note.setFont(Theme.FONT_SMALL);
        note.setForeground(Theme.TEXT_MUTED);
        panel.add(note, BorderLayout.SOUTH);

        return panel;
    }

    // Tab 2 – CA Marks
    private JPanel buildCaPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBackground(Theme.LIGHT_BG);
        panel.setBorder(new EmptyBorder(12, 4, 4, 4));

        mdlCA = new DefaultTableModel(
            new Object[]{"Course","Assessment","Mark /100","Weight %","Weighted Mark"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = styledTable(mdlCA);
        panel.add(new JScrollPane(t), BorderLayout.CENTER);

        JLabel note = new JLabel("  ℹ CA marks are only visible after your lecturer publishes them.");
        note.setFont(Theme.FONT_SMALL);
        note.setForeground(Theme.TEXT_MUTED);
        panel.add(note, BorderLayout.SOUTH);
        return panel;
    }

    // Tab 3 – Final Marks
    private JPanel buildFinalPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setBackground(Theme.LIGHT_BG);
        panel.setBorder(new EmptyBorder(12, 4, 4, 4));

        mdlFinal = new DefaultTableModel(
            new Object[]{"Course","Assessment Name","Final Mark /100"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable t = styledTable(mdlFinal);
        panel.add(new JScrollPane(t), BorderLayout.CENTER);

        JLabel note = new JLabel("  ℹ Final marks are only visible after your lecturer publishes them.");
        note.setFont(Theme.FONT_SMALL);
        note.setForeground(Theme.TEXT_MUTED);
        panel.add(note, BorderLayout.SOUTH);
        return panel;
    }

    // ─── Data loading ─────────────────────────────────────────────────────────

    private void loadCourses() {
        cmbCourse.removeAllItems();
        cmbCourse.addItem("-- All My Courses --");
        if (user == null) return;
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) return;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT c.course_code, c.course_name FROM course_enrollments e " +
                    "JOIN courses c ON c.course_code = e.course_code " +
                    "WHERE e.student_user_id = ? AND e.active = TRUE ORDER BY c.course_code")) {
                ps.setString(1, user.getUserId());
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next())
                        cmbCourse.addItem(rs.getString("course_code") + " – " + rs.getString("course_name"));
                }
            }
        } catch (Exception ignored) {}
    }

    private String selectedCourse() {
        String sel = (String) cmbCourse.getSelectedItem();
        if (sel == null || sel.startsWith("--")) return null;
        return sel.split(" ")[0].trim();
    }

    private void refresh() {
        if (user == null) return;
        String courseFilter = selectedCourse();
        loadGradesAndGpa(courseFilter);
        // Undergraduates can view their own CA and end-exam marks only after
        // the Admin has approved the lecturer's result publication request.
        loadCaMarks(courseFilter);
        loadFinalMarks(courseFilter);
    }

    private void loadGradesAndGpa(String courseFilter) {
        mdlGrades.setRowCount(0);
        lblSgpa.setText("SGPA: —");
        lblCgpa.setText("CGPA: —");
        lblGpaStatus.setText("Loading…");
        if (user == null) return;
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) { lblGpaStatus.setText("Connect to MySQL to view your approved results."); return; }
            StringBuilder sql = new StringBuilder(
                "SELECT c.course_code,c.course_name,ar.credits,ar.final_mark,ar.grade_code,ar.grade_point,"+
                "s.semester_code,s.semester_no,ar.gpa_module FROM academic_results ar "+
                "JOIN courses c ON c.course_code=ar.course_code "+
                "JOIN academic_semesters s ON s.semester_id=ar.semester_id "+
                "WHERE ar.student_user_id=? AND ar.is_approved=TRUE AND ar.is_best=TRUE ");
            sql.append("ORDER BY s.semester_no DESC,c.course_code");
            java.util.Map<String,double[]> semesterGpa=new java.util.LinkedHashMap<>();
            int count=0;
            try(PreparedStatement ps=con.prepareStatement(sql.toString())) {
                ps.setString(1,user.getUserId());
                try(ResultSet rs=ps.executeQuery()) {
                    while(rs.next()) {
                        String sem=rs.getString("semester_code");
                        int credits=rs.getInt("credits");
                        double gp=rs.getDouble("grade_point");
                        String moduleCode=rs.getString("course_code");
                        if(courseFilter==null || courseFilter.equals(moduleCode)) {
                            mdlGrades.addRow(new Object[]{moduleCode,rs.getString("course_name"),
                                credits,String.format("%.2f",rs.getDouble("final_mark")),
                                rs.getString("grade_code"),String.format("%.2f",gp)});
                            count++;
                        }
                        if(credits>0 && rs.getBoolean("gpa_module") &&
                           !java.util.Set.of("N","W").contains(rs.getString("grade_code"))) {
                            double[] totals=semesterGpa.computeIfAbsent(sem,k->new double[2]);
                            totals[0]+=gp*credits; totals[1]+=credits;
                        }
                    }
                }
            }
            if(count==0) { lblGpaStatus.setText("No Admin-approved results are available yet."); return; }
            java.util.List<Double> sgpas=new java.util.ArrayList<>();
            double latest=0; boolean first=true;
            for(double[] t:semesterGpa.values()) {
                double value=t[1]==0?0:Math.round((t[0]/t[1])*100.0)/100.0;
                sgpas.add(value);
                if(first){latest=value;first=false;}
            }
            double cgpa=sgpas.stream().mapToDouble(Double::doubleValue).average().orElse(0);
            lblSgpa.setText(String.format("SGPA: %.2f",latest));
            lblCgpa.setText(String.format("CGPA: %.2f",Math.round(cgpa*100.0)/100.0));
            try(PreparedStatement ps=con.prepareStatement(
                "SELECT status FROM eligibility_snapshots WHERE student_user_id=? ORDER BY checked_at DESC LIMIT 1")) {
                ps.setString(1,user.getUserId());
                try(ResultSet rs=ps.executeQuery()) {
                    if(rs.next()) lblGpaStatus.setText("Exam Eligibility: "+("ELIGIBLE".equals(rs.getString(1))?"Eligible":"Not Eligible"));
                    else lblGpaStatus.setText("Eligibility has not yet been checked.");
                }
            }
        } catch(Exception ex) {
            lblGpaStatus.setText("Approved results are not available: "+ex.getMessage());
        }
    }

    private void loadCaMarks(String courseFilter) {
        mdlCA.setRowCount(0);
        if (user == null) return;
        String sql =
            "SELECT c.course_code,a.assessment_name,m.mark,a.weight_percentage," +
            "ROUND(m.mark*a.weight_percentage/100,2) AS weighted " +
            "FROM academic_marks m " +
            "JOIN academic_assessments a ON a.assessment_id=m.assessment_id " +
            "JOIN courses c ON c.course_code=a.course_code " +
            "JOIN academic_results ar ON ar.student_user_id=m.student_user_id " +
            "AND ar.course_code=a.course_code AND ar.semester_id=a.semester_id " +
            "AND ar.is_approved=TRUE AND ar.is_best=TRUE " +
            "WHERE m.student_user_id=? AND a.component IN ('QUIZ','ASSIGNMENT','MID_TERM') " +
            (courseFilter == null ? "" : "AND a.course_code=? ") +
            "ORDER BY c.course_code,a.assessment_name";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                mdlCA.addRow(new Object[]{"—","Connect to MySQL to view published CA marks","—","—","—"});
                return;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, user.getUserId());
                if (courseFilter != null) ps.setString(2, courseFilter);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        mdlCA.addRow(new Object[]{rs.getString("course_code"),
                            rs.getString("assessment_name"),
                            String.format("%.2f", rs.getDouble("mark")),
                            String.format("%.2f%%", rs.getDouble("weight_percentage")),
                            String.format("%.2f", rs.getDouble("weighted"))});
                    }
                }
            }
            if (mdlCA.getRowCount() == 0)
                mdlCA.addRow(new Object[]{"—","No Admin-approved CA marks are available yet","—","—","—"});
        } catch (Exception ex) {
            mdlCA.addRow(new Object[]{"—","Could not load CA marks: " + ex.getMessage(),"—","—","—"});
        }
    }

    private void loadFinalMarks(String courseFilter) {
        mdlFinal.setRowCount(0);
        if (user == null) return;
        String sql =
            "SELECT c.course_code,a.assessment_name,m.mark " +
            "FROM academic_marks m " +
            "JOIN academic_assessments a ON a.assessment_id=m.assessment_id " +
            "JOIN courses c ON c.course_code=a.course_code " +
            "JOIN academic_results ar ON ar.student_user_id=m.student_user_id " +
            "AND ar.course_code=a.course_code AND ar.semester_id=a.semester_id " +
            "AND ar.is_approved=TRUE AND ar.is_best=TRUE " +
            "WHERE m.student_user_id=? AND a.component='ESA' " +
            (courseFilter == null ? "" : "AND a.course_code=? ") +
            "ORDER BY c.course_code";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                mdlFinal.addRow(new Object[]{"—","Connect to MySQL to view published end-exam marks","—"});
                return;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, user.getUserId());
                if (courseFilter != null) ps.setString(2, courseFilter);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        mdlFinal.addRow(new Object[]{rs.getString("course_code"),
                            rs.getString("assessment_name"),
                            String.format("%.2f", rs.getDouble("mark"))});
                    }
                }
            }
            if (mdlFinal.getRowCount() == 0)
                mdlFinal.addRow(new Object[]{"—","No Admin-approved end-exam marks are available yet","—"});
        } catch (Exception ex) {
            mdlFinal.addRow(new Object[]{"—","Could not load end-exam marks: " + ex.getMessage(),"—"});
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setRowHeight(26);
        t.setFillsViewportHeight(true);
        t.getTableHeader().setFont(Theme.FONT_SUBHEADER);
        t.setFont(Theme.FONT_REGULAR);
        t.setGridColor(Theme.BORDER);
        t.setSelectionBackground(Theme.PRIMARY_LIGHT);
        t.setSelectionForeground(Color.WHITE);
        return t;
    }

    private JLabel makeBigLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 20));
        l.setForeground(Theme.PRIMARY_DARK);
        return l;
    }

    // Compatibility helper for code that may still call studentKey()
    private String studentKey() {
        return user instanceof UndergraduateUser u ? u.getStudentRegNo() : (user != null ? user.getUserId() : "");
    }
}
