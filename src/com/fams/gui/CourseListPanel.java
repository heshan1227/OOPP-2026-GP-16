package com.fams.gui;

import com.fams.database.DBConnection;
import com.fams.database.DatabaseInitializer;
import com.fams.model.User;
import com.fams.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Dedicated Course List panel for Undergraduates and other users to view
 * their enrolled semester course modules, credit breakdown, and lecture materials.
 */
public class CourseListPanel extends JPanel {
    private DefaultTableModel tableModel;
    private JTable table;
    private JTextField txtSearch;
    private JComboBox<String> cbFilterDept;
    private JLabel lblTotalCourses;
    private JLabel lblTotalCredits;

    private final List<CourseItem> allCourses = new ArrayList<>();

    public CourseListPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(Theme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initUI();
        loadCourses();
    }

    private void initUI() {
        // --- TOP HEADER ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);

        JLabel lblTitle = Theme.createHeaderLabel("Course List — Enrolled Academic Modules");
        lblTitle.setFont(Theme.FONT_TITLE);
        lblTitle.setForeground(Theme.PRIMARY_DARK);

        JLabel lblSub = new JLabel("Faculty of Technology | University of Ruhuna — Current Academic Session");
        lblSub.setFont(Theme.FONT_SMALL);
        lblSub.setForeground(Theme.TEXT_MUTED);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(lblTitle);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(lblSub);

        headerRow.add(titlePanel, BorderLayout.WEST);

        User currentUser = AuthService.getInstance().getCurrentUser();
        String studentReg = (currentUser != null) ? currentUser.getUsername() : "Student";
        JLabel regBadge = new JLabel(" Student: " + studentReg + " | Semester 1 - 2026 ");
        regBadge.setFont(Theme.FONT_SUBHEADER);
        regBadge.setOpaque(true);
        regBadge.setBackground(new Color(224, 231, 255));
        regBadge.setForeground(Theme.PRIMARY_DARK);
        regBadge.setBorder(new EmptyBorder(6, 12, 6, 12));
        headerRow.add(regBadge, BorderLayout.EAST);

        topContainer.add(headerRow);
        topContainer.add(Box.createVerticalStrut(15));

        // --- STATS OVERVIEW CARDS ---
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 15, 0));
        statsGrid.setOpaque(false);

        lblTotalCourses = new JLabel("4 Courses");
        lblTotalCredits = new JLabel("10 Credits");

        statsGrid.add(createMiniCard("Enrolled Modules", lblTotalCourses, Theme.PRIMARY));
        statsGrid.add(createMiniCard("Total Credits", lblTotalCredits, Theme.SUCCESS));
        statsGrid.add(createMiniCard("Department", new JLabel("ICT Department"), Theme.SECONDARY));
        statsGrid.add(createMiniCard("Enrollment Status", new JLabel("Active (Good Standing)"), new Color(16, 185, 129)));

        topContainer.add(statsGrid);
        topContainer.add(Box.createVerticalStrut(15));

        // --- FILTER BAR ---
        JPanel filterCard = Theme.createCardPanel();
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));

        filterCard.add(new JLabel("Search Module:"));
        txtSearch = new JTextField(16);
        txtSearch.setFont(Theme.FONT_REGULAR);
        filterCard.add(txtSearch);

        filterCard.add(Box.createHorizontalStrut(10));
        filterCard.add(new JLabel("Department:"));
        cbFilterDept = new JComboBox<>(new String[]{"All Departments", "ICT Department", "ET Department", "BST Department"});
        filterCard.add(cbFilterDept);

        JButton btnReset = Theme.createSecondaryButton("Clear Search");
        btnReset.setPreferredSize(new Dimension(110, 32));
        btnReset.addActionListener(e -> {
            txtSearch.setText("");
            cbFilterDept.setSelectedIndex(0);
            applyFilter();
        });
        filterCard.add(btnReset);

        JButton btnRefresh = Theme.createSecondaryButton("Refresh");
        btnRefresh.setPreferredSize(new Dimension(90, 32));
        btnRefresh.addActionListener(e -> loadCourses());
        filterCard.add(btnRefresh);

        topContainer.add(filterCard);

        add(topContainer, BorderLayout.NORTH);

        // Document listener for dynamic search
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        cbFilterDept.addActionListener(e -> applyFilter());

        // --- CENTER TABLE ---
        JPanel tableCard = Theme.createCardPanel();
        tableCard.setLayout(new BorderLayout(10, 10));

        String[] cols = {"Course Code", "Course / Module Name", "Theory Credits", "Practical Credits", "Total Credits", "Department", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(Theme.FONT_REGULAR);
        table.setRowHeight(30);
        table.getTableHeader().setFont(Theme.FONT_SUBHEADER);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(300);
        table.getColumnModel().getColumn(2).setCellRenderer(center);
        table.getColumnModel().getColumn(3).setCellRenderer(center);
        table.getColumnModel().getColumn(4).setCellRenderer(center);
        table.getColumnModel().getColumn(6).setCellRenderer(center);

        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);

        // Table Bottom Actions
        JPanel bottomActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        bottomActions.setOpaque(false);

        JButton btnViewMaterials = Theme.createPrimaryButton("View Course Materials");
        btnViewMaterials.setPreferredSize(new Dimension(190, 38));
        btnViewMaterials.addActionListener(e -> viewMaterialsForSelectedCourse());
        bottomActions.add(btnViewMaterials);

        tableCard.add(bottomActions, BorderLayout.SOUTH);

        add(tableCard, BorderLayout.CENTER);
    }

    private JPanel createMiniCard(String title, JLabel valLabel, Color accentColor) {
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lbl = new JLabel(title);
        lbl.setFont(Theme.FONT_SMALL);
        lbl.setForeground(Theme.TEXT_MUTED);

        valLabel.setFont(Theme.FONT_HEADER);
        valLabel.setForeground(accentColor);

        card.add(lbl);
        card.add(Box.createVerticalStrut(4));
        card.add(valLabel);
        return card;
    }

    public void loadCourses() {
        allCourses.clear();
        boolean loadedFromDb = false;

        User user = AuthService.getInstance().getCurrentUser();
        String userId = user != null ? user.getUserId() : "UG1";

        try (Connection c = DBConnection.getConnection()) {
            if (c != null) {
                String sql = "SELECT c.course_code, c.course_name, c.theory_credits, c.practical_credits, c.department " +
                        "FROM course_enrollments e " +
                        "JOIN courses c ON c.course_code = e.course_code " +
                        "WHERE e.student_user_id = ? AND e.active = TRUE " +
                        "ORDER BY c.course_code";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            allCourses.add(new CourseItem(
                                    rs.getString("course_code"),
                                    rs.getString("course_name"),
                                    rs.getInt("theory_credits"),
                                    rs.getInt("practical_credits"),
                                    rs.getString("department"),
                                    "Enrolled (Active)"
                            ));
                            loadedFromDb = true;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        // If not loaded or empty, populate standard semester enrolled modules
        if (!loadedFromDb || allCourses.isEmpty()) {
            allCourses.add(new CourseItem("ICT2132", "Object Oriented Programming Practicum", 2, 1, "ICT Department", "Enrolled (Active)"));
            allCourses.add(new CourseItem("ICT2113", "Database Systems Practicum", 2, 1, "ICT Department", "Enrolled (Active)"));
            allCourses.add(new CourseItem("ICT2122", "Data Structures & Algorithms", 2, 1, "ICT Department", "Enrolled (Active)"));
            allCourses.add(new CourseItem("ENG2102", "Technical Communication", 2, 0, "Department of Interdisciplinary", "Enrolled (Active)"));
        }

        applyFilter();
    }

    private void applyFilter() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);

        String query = txtSearch == null ? "" : txtSearch.getText().trim().toLowerCase();
        String deptFilter = cbFilterDept == null ? "All Departments" : (String) cbFilterDept.getSelectedItem();

        int sumCredits = 0;
        int count = 0;

        for (CourseItem item : allCourses) {
            boolean matchesSearch = query.isEmpty() ||
                    item.code.toLowerCase().contains(query) ||
                    item.name.toLowerCase().contains(query);

            boolean matchesDept = "All Departments".equals(deptFilter) ||
                    item.department.equalsIgnoreCase(deptFilter);

            if (matchesSearch && matchesDept) {
                tableModel.addRow(new Object[]{
                        item.code,
                        item.name,
                        item.theoryCredits,
                        item.practicalCredits,
                        (item.theoryCredits + item.practicalCredits),
                        item.department,
                        item.status
                });
                sumCredits += (item.theoryCredits + item.practicalCredits);
                count++;
            }
        }

        if (lblTotalCourses != null) lblTotalCourses.setText(count + " Courses");
        if (lblTotalCredits != null) lblTotalCredits.setText(sumCredits + " Credits");
    }

    private void viewMaterialsForSelectedCourse() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a course from the list to view its materials.", "Select Course", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = table.convertRowIndexToModel(row);
        String courseCode = (String) tableModel.getValueAt(modelRow, 0);
        String courseName = (String) tableModel.getValueAt(modelRow, 1);

        DefaultTableModel matModel = new DefaultTableModel(new Object[]{"Material ID", "Title / Topic", "File Path / Document", "Uploaded Date"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        boolean loaded = false;
        try (Connection c = DBConnection.getConnection()) {
            if (c != null) {
                String sql = "SELECT material_id, title, file_path, uploaded_date FROM course_materials WHERE course_code = ? ORDER BY uploaded_date DESC";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, courseCode);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            matModel.addRow(new Object[]{
                                    rs.getInt("material_id"),
                                    rs.getString("title"),
                                    rs.getString("file_path"),
                                    rs.getTimestamp("uploaded_date")
                            });
                            loaded = true;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        if (!loaded || matModel.getRowCount() == 0) {
            matModel.addRow(new Object[]{1, "Lecture 01 - Course Introduction & OOP Concepts", "lecture_01_intro.pdf", "2026-10-10"});
            matModel.addRow(new Object[]{2, "Practical Sheet 01 - Java Swing & Layouts", "practicum_sheet_01.pdf", "2026-10-15"});
            matModel.addRow(new Object[]{3, "Assignment Brief - Mini Project Group Evaluation", "assignment_brief.pdf", "2026-10-18"});
        }

        JTable matTable = new JTable(matModel);
        matTable.setFont(Theme.FONT_REGULAR);
        matTable.setRowHeight(26);

        JScrollPane sp = new JScrollPane(matTable);
        sp.setPreferredSize(new Dimension(650, 220));

        JOptionPane.showMessageDialog(this, sp, "Course Materials: " + courseCode + " - " + courseName, JOptionPane.PLAIN_MESSAGE);
    }

    private static class CourseItem {
        final String code;
        final String name;
        final int theoryCredits;
        final int practicalCredits;
        final String department;
        final String status;

        CourseItem(String code, String name, int theoryCredits, int practicalCredits, String department, String status) {
            this.code = code;
            this.name = name;
            this.theoryCredits = theoryCredits;
            this.practicalCredits = practicalCredits;
            this.department = department;
            this.status = status;
        }
    }
}
