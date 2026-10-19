package com.fams.gui;

import com.fams.database.DBConnection;
import com.fams.model.User;
import com.fams.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentCardPanel;

    private DashboardHomePanel dashboardPanel;
    private AttendancePanel attendancePanel;
    private MedicalPanel medicalPanel;
    private NoticePanel noticePanel;
    private TimetablePanel timetablePanel;
    private CourseManagementPanel coursePanel;
    private MarksManagementPanel marksPanel;
    private UserManagementPanel userPanel;

    public MainFrame() {
        setTitle("Faculty Academic Management System (FAMS) - University of Ruhuna");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        User currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser == null) return;

        JPanel mainLayout = new JPanel(new BorderLayout());
        mainLayout.setBackground(Theme.LIGHT_BG);

        // --- TOP BAR ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Theme.DARK_BG);
        topBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        JLabel sysLogo = new JLabel("FAMS | Faculty of Technology");
        sysLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sysLogo.setForeground(Color.WHITE);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userPanel.setOpaque(false);

        JLabel userInfo = new JLabel(currentUser.getFullName() + " [" + currentUser.getRole().getDisplayName() + "]");
        userInfo.setFont(Theme.FONT_SUBHEADER);
        userInfo.setForeground(Color.WHITE);

        JButton btnLogout = Theme.createSecondaryButton("Switch Role / Logout");
        btnLogout.setFont(Theme.FONT_SMALL);
        btnLogout.addActionListener(e -> logout());

        userPanel.add(userInfo);
        userPanel.add(btnLogout);

        topBar.add(sysLogo, BorderLayout.WEST);
        topBar.add(userPanel, BorderLayout.EAST);
        mainLayout.add(topBar, BorderLayout.NORTH);

        // --- LEFT SIDEBAR ---
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(30, 41, 59)); // Slate Navy
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(new EmptyBorder(15, 10, 15, 10));

        JLabel menuTitle = new JLabel("  NAVIGATION MENU");
        menuTitle.setFont(Theme.FONT_SMALL);
        menuTitle.setForeground(new Color(148, 163, 184));
        sidebar.add(menuTitle);
        sidebar.add(Box.createVerticalStrut(15));

        sidebar.add(createNavButton("Dashboard", "CARD_DASHBOARD"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("User Management", "CARD_USERS"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("Course Management", "CARD_COURSES"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("Marks & Eligibility", "CARD_MARKS"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("Attendance Subsystem", "CARD_ATTENDANCE"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("Medical Records", "CARD_MEDICAL"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("Notices Board", "CARD_NOTICES"));
        sidebar.add(Box.createVerticalStrut(8));
        sidebar.add(createNavButton("Timetables", "CARD_TIMETABLE"));

        sidebar.add(Box.createVerticalGlue());

        JLabel roleNotice = new JLabel("<html><body style='width: 170px;'>Logged in as:<br><b>" + currentUser.getRole().getDisplayName() + "</b></body></html>");
        roleNotice.setFont(Theme.FONT_SMALL);
        roleNotice.setForeground(new Color(203, 213, 225));
        sidebar.add(roleNotice);

        mainLayout.add(sidebar, BorderLayout.WEST);

        // --- CENTER CARD CONTAINER ---
        cardLayout = new CardLayout();
        contentCardPanel = new JPanel(cardLayout);
        contentCardPanel.setBackground(Theme.LIGHT_BG);

        dashboardPanel = new DashboardHomePanel();
        attendancePanel = new AttendancePanel();
        medicalPanel = new MedicalPanel();
        noticePanel = new NoticePanel();
        timetablePanel = new TimetablePanel();
        coursePanel = new CourseManagementPanel();
        userPanel = new UserManagementPanel();
        marksPanel = new MarksManagementPanel();

        contentCardPanel.add(dashboardPanel, "CARD_DASHBOARD");
        contentCardPanel.add(userPanel, "CARD_USERS");
        contentCardPanel.add(coursePanel, "CARD_COURSES");
        contentCardPanel.add(marksPanel, "CARD_MARKS");
        contentCardPanel.add(attendancePanel, "CARD_ATTENDANCE");
        contentCardPanel.add(medicalPanel, "CARD_MEDICAL");
        contentCardPanel.add(noticePanel, "CARD_NOTICES");
        contentCardPanel.add(timetablePanel, "CARD_TIMETABLE");

        mainLayout.add(contentCardPanel, BorderLayout.CENTER);

        // --- BOTTOM STATUS BAR ---
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(241, 245, 249));
        statusBar.setBorder(new EmptyBorder(6, 15, 6, 15));

        String dbModeText = DBConnection.isUseMemoryFallback() ?
                "Database Status: Memory Store (Thread-Safe Fallback Active for Demo)" :
                "Database Status: Connected to MySQL (fams_db)";

        JLabel lblDbStatus = new JLabel(dbModeText);
        lblDbStatus.setFont(Theme.FONT_SMALL);
        lblDbStatus.setForeground(DBConnection.isUseMemoryFallback() ? Theme.PRIMARY_DARK : Theme.SUCCESS);

        JLabel lblGroup = new JLabel("ICT2132 Mini Project | Group B16 | Integrated Modules: Users, Courses, Marks, Attendance, Medical, Notices & Timetables");
        lblGroup.setFont(Theme.FONT_SMALL);
        lblGroup.setForeground(Theme.TEXT_MUTED);

        statusBar.add(lblDbStatus, BorderLayout.WEST);
        statusBar.add(lblGroup, BorderLayout.EAST);

        mainLayout.add(statusBar, BorderLayout.SOUTH);

        add(mainLayout);
    }

    private JButton createNavButton(String text, String cardName) {
        JButton btn = Theme.createPrimaryButton(text);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.addActionListener(e -> cardLayout.show(contentCardPanel, cardName));
        return btn;
    }

    private void logout() {
        AuthService.getInstance().logout();
        dispose();
        new LoginFrame(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        }).setVisible(true);
    }
}
