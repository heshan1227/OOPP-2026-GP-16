package gui;

import dao.UserDAO;
import dao.UserDAOImpl;
import model.User;
import service.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminDashboardFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private UserDAO userDAO;

    public AdminDashboardFrame() {
        this.userDAO = new UserDAOImpl();
        initUI();
        loadUserData();
    }

    private void initUI() {
        User user = AuthService.getCurrentUser();
        setTitle("FAMS - Admin Control Panel | " + (user != null ? user.getName() : "Administrator"));
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel container = new JPanel(new BorderLayout());

        container.add(createHeaderPanel(), BorderLayout.NORTH);
        container.add(createSidebarPanel(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);

        mainContentPanel.add(createUserManagementPanel(), "UserManagement");
        mainContentPanel.add(new CourseManagementPanel(), "CourseManagement");
        mainContentPanel.add(createPlaceholderPanel("Notice Management Module (TG2108)"), "NoticeManagement");
        mainContentPanel.add(createPlaceholderPanel("Timetable Management Module (TG2108)"), "TimetableManagement");

        container.add(mainContentPanel, BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        statusBar.add(new JLabel(" Logged in as: " + (user != null ? user.getUsername() : "Admin") + " [ADMIN]"));
        container.add(statusBar, BorderLayout.SOUTH);

        add(container);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(40, 60, 100));
        header.setPreferredSize(new Dimension(1000, 60));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel title = new JLabel("Faculty Academic Management System - Admin Dashboard");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));

        JButton btnLogout = new JButton("Logout");
        btnLogout.setBackground(new Color(200, 60, 60));
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFocusPainted(false);
        btnLogout.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Logged out successfully.");
            this.dispose();
            new LoginFrame(new AuthService(userDAO)).setVisible(true);
        });

        header.add(title, BorderLayout.WEST);
        header.add(btnLogout, BorderLayout.EAST);
        return header;
    }

    private JPanel createSidebarPanel() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, 550));
        sidebar.setBackground(new Color(230, 235, 245));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

        sidebar.add(Box.createVerticalStrut(15));

        JButton btnUserMgmt = createSidebarButton("User Management");
        JButton btnCourseMgmt = createSidebarButton("Course Management");
        JButton btnNoticeMgmt = createSidebarButton("Notice Management");
        JButton btnTimetableMgmt = createSidebarButton("Timetable Management");

        btnUserMgmt.addActionListener(e -> cardLayout.show(mainContentPanel, "UserManagement"));
        btnCourseMgmt.addActionListener(e -> cardLayout.show(mainContentPanel, "CourseManagement"));
        btnNoticeMgmt.addActionListener(e -> cardLayout.show(mainContentPanel, "NoticeManagement"));
        btnTimetableMgmt.addActionListener(e -> cardLayout.show(mainContentPanel, "TimetableManagement"));

        sidebar.add(btnUserMgmt);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnCourseMgmt);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnNoticeMgmt);
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(btnTimetableMgmt);

        return sidebar;
    }

    private JButton createSidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(180, 40));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        return btn;
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnAddUser = new JButton("Add New User");
        JButton btnEditUser = new JButton("Edit Selected");
        JButton btnDeleteUser = new JButton("Delete User");
        JButton btnRefresh = new JButton("Refresh List");

        topBar.add(btnAddUser);
        topBar.add(btnEditUser);
        topBar.add(btnDeleteUser);
        topBar.add(btnRefresh);

        panel.add(topBar, BorderLayout.NORTH);

        String[] columnNames = {"User ID", "Username", "Name", "Email", "Role"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        userTable = new JTable(tableModel);
        panel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        // Action Listeners
        btnAddUser.addActionListener(e -> showAddUserDialog());
        btnEditUser.addActionListener(e -> showEditUserDialog());
        btnDeleteUser.addActionListener(e -> deleteSelectedUser());
        btnRefresh.addActionListener(e -> loadUserData());

        return panel;
    }

    private void loadUserData() {
        tableModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        for (User u : users) {
            tableModel.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getName(), u.getEmail(), u.getRole()});
        }
    }

    private void showAddUserDialog() {
        UserFormDialog dialog = new UserFormDialog(this, null);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            boolean success = userDAO.createUser(dialog.getUser(), "");
            if (success) {
                JOptionPane.showMessageDialog(this, "User added successfully!");
                loadUserData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add user to database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditUserDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        User userToEdit = userDAO.getUserById(userId);

        if (userToEdit != null) {
            UserFormDialog dialog = new UserFormDialog(this, userToEdit);
            dialog.setVisible(true);
            if (dialog.isSaved()) {
                boolean success = userDAO.updateUser(dialog.getUser());
                if (success) {
                    JOptionPane.showMessageDialog(this, "User updated successfully!");
                    loadUserData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update user.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete user ID " + userId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (userDAO.deleteUser(userId)) {
                JOptionPane.showMessageDialog(this, "User deleted successfully!");
                loadUserData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setForeground(Color.GRAY);
        panel.add(label);
        return panel;
    }
}
