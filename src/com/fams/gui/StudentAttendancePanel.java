package com.fams.gui;

import com.fams.database.AttendanceDAO;
import com.fams.database.DBConnection;
import com.fams.model.AttendanceRecord;
import com.fams.model.UndergraduateUser;
import com.fams.model.User;
import com.fams.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Student-facing attendance view: course list first, then the selected course's daily records and summary. */
public class StudentAttendancePanel extends JPanel {
    private final User user = AuthService.getInstance().getCurrentUser();
    private final String regNo;
    private final CardLayout viewLayout = new CardLayout();
    private final JPanel views = new JPanel(viewLayout);
    private final JPanel detailHost = new JPanel(new BorderLayout(8, 8));

    public StudentAttendancePanel() {
        regNo = user instanceof UndergraduateUser
                ? ((UndergraduateUser) user).getStudentRegNo() : user.getUserId();
        setLayout(new BorderLayout(10, 10));
        setBackground(Theme.LIGHT_BG);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        JLabel heading = Theme.createHeaderLabel("View Your Daily Attendance");
        add(heading, BorderLayout.NORTH);

        detailHost.setOpaque(false);
        views.add(createCourseList(), "COURSES");
        views.add(detailHost, "DETAIL");
        add(views, BorderLayout.CENTER);
    }

    private JPanel createCourseList() {
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BorderLayout(6, 6));
        JLabel title = new JLabel("Select the Course Unit For view Attendance", SwingConstants.CENTER);
        title.setFont(Theme.FONT_SUBHEADER);
        title.setForeground(Theme.PRIMARY);
        card.add(title, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"Subject", "Name", "Department", "View"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return column == 3; }
        };
        JTable table = new JTable(model);
        styleTable(table);
        table.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox(), model, this));
        loadCourses(model);
        card.add(new JScrollPane(table), BorderLayout.CENTER);
        return card;
    }

    private void loadCourses(DefaultTableModel model) {
        model.setRowCount(0);
        String sql = "SELECT DISTINCT c.course_code,c.course_name,c.department FROM courses c " +
                "LEFT JOIN course_enrollments e ON e.course_code=c.course_code AND e.student_user_id=? AND e.active=TRUE " +
                "LEFT JOIN attendance_records ar ON ar.course_code=c.course_code AND ar.student_reg_no=? " +
                "WHERE e.enrollment_id IS NOT NULL OR ar.record_id IS NOT NULL ORDER BY c.course_code";
        try (Connection con = DBConnection.getConnection()) {
            if (con == null) {
                model.addRow(new Object[]{"-", "Database connection unavailable", "-", "View"});
                return;
            }
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, user.getUserId());
                ps.setString(2, regNo);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) model.addRow(new Object[]{rs.getString("course_code"), rs.getString("course_name"), rs.getString("department"), "View"});
                }
            }
        } catch (SQLException ex) {
            model.addRow(new Object[]{"-", "Unable to load course units: " + ex.getMessage(), "-", "View"});
        }
    }

    private String openCode;
    private String openName;

    /** Called when the Attendance tab is shown again (e.g. after a medical was approved). */
    public void refresh() {
        if (openCode != null) showCourse(openCode, openName);
    }

    private void showCourse(String code, String name) {
        openCode = code;
        openName = name;
        detailHost.removeAll();
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setOpaque(false);
        JLabel title = new JLabel("Course : " + code + " - " + name, SwingConstants.CENTER);
        title.setFont(Theme.FONT_SUBHEADER);
        title.setForeground(Theme.PRIMARY);
        content.add(title, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(new Object[]{"L Id", "Date", "Hours", "Type", "Time", "Group", "Status"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable records = new JTable(model);
        styleTable(records);
        int attended = 0, medicalCovered = 0, total = 0;
        List<AttendanceRecord> list = new ArrayList<>(new AttendanceDAO().getAttendanceByStudent(regNo, code));
        list.sort(Comparator.comparing(AttendanceRecord::getSessionDate).thenComparingInt(AttendanceRecord::getRecordId));
        for (AttendanceRecord r : list) {
            total += 2;
            String status;
            if (r.isPresent()) {
                attended += 2;
                status = "Present";
            } else if (r.isHasMedicalSubmitted() && r.isMedicalApproved()) {
                medicalCovered += 2;
                status = "Absent (Medical Approved)";
            } else if (r.isHasMedicalSubmitted()) {
                status = "Absent (Medical Pending)";
            } else {
                status = "Absent";
            }
            model.addRow(new Object[]{r.getRecordId(), r.getSessionDate(), 2,
                    r.getComponentType().getDisplayName(), "-", "-", status});
        }
        if (list.isEmpty()) {
            model.addRow(new Object[]{"-", "No attendance records found", "-", "-", "-", "-", "-"});
        }
        content.add(new JScrollPane(records), BorderLayout.CENTER);

        JPanel summary = Theme.createCardPanel();
        summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
        JLabel summaryTitle = new JLabel("Summary of Your Attendance", SwingConstants.CENTER);
        summaryTitle.setFont(Theme.FONT_SUBHEADER);
        JLabel courseSummary = new JLabel("Attendance of Lecture - in " + code + " Course Unit", SwingConstants.CENTER);
        courseSummary.setFont(Theme.FONT_SUBHEADER);
        courseSummary.setForeground(Theme.PRIMARY);
        int participated = attended + medicalCovered;
        String medicalNote = medicalCovered > 0 ? "  (incl. " + medicalCovered + " medical-approved)" : "";
        JLabel hours = new JLabel("Total Participated Hours : " + participated + medicalNote + "     |     Total Hours : " + total, SwingConstants.CENTER);
        hours.setFont(Theme.FONT_REGULAR);
        int percent = total == 0 ? 0 : (int)Math.round(participated * 100.0 / total);
        JProgressBar progress = new JProgressBar(0, 100);
        progress.setValue(percent);
        progress.setStringPainted(true);
        progress.setString(percent + "%");
        progress.setForeground(Theme.PRIMARY);
        JButton back = Theme.createSecondaryButton("← Go Back to Course Units");
        back.addActionListener(e -> { openCode = null; openName = null; viewLayout.show(views, "COURSES"); });
        summary.add(summaryTitle); summary.add(Box.createVerticalStrut(8)); summary.add(courseSummary);
        summary.add(Box.createVerticalStrut(6)); summary.add(hours); summary.add(Box.createVerticalStrut(8));
        summary.add(progress); summary.add(Box.createVerticalStrut(8)); summary.add(back);
        content.add(summary, BorderLayout.SOUTH);
        detailHost.add(content, BorderLayout.CENTER);
        detailHost.revalidate(); detailHost.repaint();
        viewLayout.show(views, "DETAIL");
    }

    private static void styleTable(JTable table) {
        table.setRowHeight(29);
        table.setFont(Theme.FONT_REGULAR);
        table.setForeground(Theme.PRIMARY_DARK);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(218, 190, 197));
        table.setSelectionForeground(Theme.PRIMARY_DARK);
        table.setFillsViewportHeight(true);
        table.setShowGrid(true);
        table.setGridColor(Color.WHITE);
        table.setAutoCreateRowSorter(true);

        table.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected, boolean focus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                if (!selected) { c.setBackground(row % 2 == 0 ? new Color(245, 232, 236) : new Color(232, 211, 217)); c.setForeground(Theme.PRIMARY_DARK); }
                setHorizontalAlignment(SwingConstants.CENTER);
                return c;
            }
        });
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(Theme.PRIMARY_DARK);
        header.setForeground(Color.WHITE);
        header.setOpaque(true);
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new HeaderRenderer());
    }

    /** Explicit header renderer prevents Look & Feel defaults from hiding header text. */
    private static class HeaderRenderer extends JLabel implements TableCellRenderer {
        HeaderRenderer() {
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setBackground(Theme.PRIMARY_DARK);
            setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.WHITE));
        }
        @Override public Component getTableCellRendererComponent(JTable table, Object value,
                boolean selected, boolean focused, int row, int column) {
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    private static void styleViewButton(JButton button) {
        button.setUI(new BasicButtonUI());
        button.setText("View");
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(Theme.PRIMARY);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(true);
        button.setFocusPainted(false);
        button.setBorder(new LineBorder(Theme.PRIMARY_DARK, 1, true));
        button.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private static class ButtonRenderer extends JButton implements TableCellRenderer {
        ButtonRenderer() { styleViewButton(this); }
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean selected, boolean focused, int r, int c) {
            setText(v == null || v.toString().trim().isEmpty() ? "View" : v.toString());
            setBackground(selected ? Theme.PRIMARY_LIGHT : Theme.PRIMARY);
            setForeground(Color.WHITE);
            return this;
        }
    }

    private static class ButtonEditor extends DefaultCellEditor {
        private final JButton button = new JButton("View");
        private final DefaultTableModel model;
        private final StudentAttendancePanel owner;
        private int row;
        ButtonEditor(JCheckBox check, DefaultTableModel model, StudentAttendancePanel owner) {
            super(check);
            this.model = model;
            this.owner = owner;
            styleViewButton(button);
            button.addActionListener(e -> {
                fireEditingStopped();
                String code = String.valueOf(model.getValueAt(row, 0));
                String name = String.valueOf(model.getValueAt(row, 1));
                if (!code.equals("-") && !name.startsWith("Database connection")
                        && !name.startsWith("Unable to load")) owner.showCourse(code, name);
            });
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v,
                boolean selected, int r, int c) {
            row = t.convertRowIndexToModel(r);
            button.setText(v == null || v.toString().trim().isEmpty() ? "View" : v.toString());
            return button;
        }
        @Override public Object getCellEditorValue() { return "View"; }
    }

}
