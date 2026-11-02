package com.fams.gui;

import com.fams.database.UserDAO;
import com.fams.model.Role;
import com.fams.model.UndergraduateUser;
import com.fams.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Read-only undergraduate profile directory for lecturers. */
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

        JPanel details = new JPanel(new BorderLayout());
        details.setBackground(Color.WHITE);
        details.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER), new EmptyBorder(14, 16, 14, 16)));
        JLabel detailTitle = new JLabel("Student Profile Details");
        detailTitle.setFont(Theme.FONT_SUBHEADER);
        detailTitle.setForeground(Theme.PRIMARY_DARK);
        detailLabel.setFont(Theme.FONT_REGULAR);
        detailLabel.setForeground(Theme.TEXT_MAIN);
        details.add(detailTitle, BorderLayout.NORTH);
        details.add(detailLabel, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scroll, details);
        split.setResizeWeight(0.72);
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
        User current = com.fams.service.AuthService.getInstance().getCurrentUser();
        if (current == null || current.getRole() != Role.LECTURER) {
            students = new ArrayList<>();
            model.setRowCount(0);
            detailLabel.setText("Access denied. This feature is available to lecturers only.");
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
                return;
            }
        }
    }

    private String safe(String value) { return value == null ? "" : value; }
    private String html(String value) { return safe(value).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"); }
}
