package fams.TG_2024_2133;

import fams.TG_2024_2108.AttendancePanel;
import fams.TG_2024_2108.MedicalPanel;
import fams.TG_2024_2108.NoticePanel;
import fams.TG_2024_2108.TimetablePanel;
import fams.TG_2024_2113.MarksManagementPanel;
import fams.TG_2024_2138.CourseListPanel;
import fams.TG_2024_2138.CourseManagementPanel;


import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentCardPanel;
    private final java.util.Map<String, JButton> navButtons = new java.util.LinkedHashMap<>();

    private DashboardHomePanel dashboardPanel;
    private AttendancePanel attendancePanel;
    private MedicalPanel medicalPanel;
    private NoticePanel noticePanel;
    private TimetablePanel timetablePanel;
    private CourseManagementPanel coursePanel;
    private MarksManagementPanel marksPanel;
    private UserManagementPanel userPanel;
    private ProfilePanel profilePanel;
    private StudentPortalPanel studentPortalPanel;
    private CourseListPanel courseListPanel;
    private StudentProfilePanel studentProfilePanel;

    private CircularAvatar topBarAvatar;
    private JLabel userInfoLabel;

    public MainFrame() {
        setTitle("Faculty Academic Management System (FAMS) - University of Ruhuna");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        User currentUser = UserManagement.getInstance().getCurrentUser();
        if (currentUser == null) return;

        JPanel mainLayout = new Theme.BackdropPanel(new BorderLayout());

        // --- TOP BAR ---
        JPanel topBar = Theme.createDarkGlassPanel(new BorderLayout(), 0);
        topBar.setBorder(new EmptyBorder(10, 20, 10, 20));

        JLabel sysLogo = new JLabel("FAMS | Faculty of Technology");
        sysLogo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sysLogo.setForeground(Color.WHITE);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        userPanel.setOpaque(false);

        topBarAvatar = new CircularAvatar(32, currentUser.getProfilePictureUrl(), currentUser.getFullName());
        topBarAvatar.setBorderWidth(1.5f);
        topBarAvatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        topBarAvatar.setToolTipText("View Profile");
        topBarAvatar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showCard("CARD_PROFILE");
            }
        });

        userInfoLabel = new JLabel(currentUser.getFullName() + " [" + currentUser.getRole().getDisplayName() + "]");
        userInfoLabel.setFont(Theme.FONT_SUBHEADER);
        userInfoLabel.setForeground(Color.WHITE);

        JButton btnLogout = Theme.createSecondaryButton("Logout");
        
        btnLogout.setFont(Theme.FONT_SMALL);
        btnLogout.addActionListener(e -> logout());

        userPanel.add(topBarAvatar);
        userPanel.add(userInfoLabel);
        userPanel.add(btnLogout);

        topBar.add(sysLogo, BorderLayout.WEST);
        topBar.add(userPanel, BorderLayout.EAST);
        mainLayout.add(topBar, BorderLayout.NORTH);


        // --- LEFT SIDEBAR ---
        JPanel sidebar = Theme.createDarkGlassPanel(null, 0);
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(new EmptyBorder(15, 10, 15, 10));

        JLabel menuTitle = new JLabel("  NAVIGATION MENU");
        menuTitle.setFont(Theme.FONT_SMALL);
        menuTitle.setForeground(new Color(191, 210, 230));
        sidebar.add(menuTitle);
        sidebar.add(Box.createVerticalStrut(15));

        // Get the logged-in user's role
        String role = currentUser.getRole().name();

        // Dashboard: available to all roles
        sidebar.add(createNavButton("Dashboard", "CARD_DASHBOARD"));
        sidebar.add(Box.createVerticalStrut(8));

        // User Management: Admin only
        if (role.equals("ADMIN")) {
            sidebar.add(createNavButton("User Management", "CARD_USERS"));
            sidebar.add(Box.createVerticalStrut(8));
        }

        // Student profiles + results (SGPA/CGPA/eligibility): Admin and Lecturer (read-only)
        if (role.equals("LECTURER") || role.equals("ADMIN")) {
            sidebar.add(createNavButton("Student Profiles", "CARD_STUDENT_PROFILES"));
            sidebar.add(Box.createVerticalStrut(8));
        }

        // Course Management: Admin and Lecturer
        if (role.equals("ADMIN") || role.equals("LECTURER")) {
            sidebar.add(createNavButton("Course Management", "CARD_COURSES"));
            sidebar.add(Box.createVerticalStrut(8));
        }

        // Marks & Eligibility: Admin and Lecturer
        if (role.equals("ADMIN") || role.equals("LECTURER")) {
            sidebar.add(createNavButton("Marks & Eligibility", "CARD_MARKS"));
            sidebar.add(Box.createVerticalStrut(8));
        }

        // Attendance: Admin, Lecturer, Technical Officer, Undergraduate
        if (role.equals("ADMIN")
                || role.equals("LECTURER")
                || role.equals("TECHNICAL_OFFICER")
                || role.equals("UNDERGRADUATE")) {
            sidebar.add(createNavButton("Attendance Subsystem", "CARD_ATTENDANCE"));
            sidebar.add(Box.createVerticalStrut(8));
        }

        // Medical Records: Admin, Lecturer, Technical Officer, Undergraduate
        if (role.equals("ADMIN")
                || role.equals("LECTURER")
                || role.equals("TECHNICAL_OFFICER")
                || role.equals("UNDERGRADUATE")) {
            sidebar.add(createNavButton("Medical Records", "CARD_MEDICAL"));
            sidebar.add(Box.createVerticalStrut(8));
        }

        // Notices Board: available to all roles
        sidebar.add(createNavButton("Notices Board", "CARD_NOTICES"));
        sidebar.add(Box.createVerticalStrut(8));

        // Timetables: available to all roles
        sidebar.add(createNavButton("Timetables", "CARD_TIMETABLE"));
        sidebar.add(Box.createVerticalStrut(8));

        // Course List: available to Undergraduate and roles without Course Management
        if (role.equals("UNDERGRADUATE") || role.equals("TECHNICAL_OFFICER")) {
            sidebar.add(createNavButton("Course List", "CARD_COURSE_LIST"));
            sidebar.add(Box.createVerticalStrut(8));
        }

        // Personal academic results: undergraduate students only
        if (role.equals("UNDERGRADUATE")) {
            sidebar.add(createNavButton("My Grades & GPA", "CARD_STUDENT_PORTAL"));
            sidebar.add(Box.createVerticalStrut(8));
        }

        // My Profile: available to all roles
        sidebar.add(createNavButton("My Profile", "CARD_PROFILE"));

        sidebar.add(Box.createVerticalGlue());

        JLabel roleNotice = new JLabel(
                "<html><body style='width: 170px;'>Logged in as:<br><b>"
                        + currentUser.getRole().getDisplayName()
                        + "</b></body></html>"
        );
        roleNotice.setFont(Theme.FONT_SMALL);
        roleNotice.setForeground(new Color(214, 226, 240));
        sidebar.add(roleNotice);

        mainLayout.add(sidebar, BorderLayout.WEST);


        // --- CENTER CARD CONTAINER ---
        cardLayout = new CardLayout();
        contentCardPanel = new JPanel(cardLayout);
        contentCardPanel.setOpaque(false);

        dashboardPanel = new DashboardHomePanel(this::showCard);
        attendancePanel = new AttendancePanel();
        medicalPanel = new MedicalPanel();
        noticePanel = new NoticePanel();
        timetablePanel = new TimetablePanel();
        coursePanel = new CourseManagementPanel();
        courseListPanel = new CourseListPanel();
        userPanel = new UserManagementPanel();
        marksPanel = new MarksManagementPanel();
        profilePanel = new ProfilePanel(this::onProfileUpdated);
        studentPortalPanel = new StudentPortalPanel();
        studentProfilePanel = new StudentProfilePanel();

        contentCardPanel.add(dashboardPanel, "CARD_DASHBOARD");
        contentCardPanel.add(userPanel, "CARD_USERS");
        contentCardPanel.add(coursePanel, "CARD_COURSES");
        contentCardPanel.add(courseListPanel, "CARD_COURSE_LIST");
        contentCardPanel.add(marksPanel, "CARD_MARKS");
        contentCardPanel.add(attendancePanel, "CARD_ATTENDANCE");
        contentCardPanel.add(medicalPanel, "CARD_MEDICAL");
        contentCardPanel.add(noticePanel, "CARD_NOTICES");
        contentCardPanel.add(timetablePanel, "CARD_TIMETABLE");
        contentCardPanel.add(profilePanel, "CARD_PROFILE");
        contentCardPanel.add(studentPortalPanel, "CARD_STUDENT_PORTAL");
        contentCardPanel.add(studentProfilePanel, "CARD_STUDENT_PROFILES");

        mainLayout.add(contentCardPanel, BorderLayout.CENTER);

        // --- BOTTOM STATUS BAR ---
        JPanel statusBar = Theme.createLightGlassPanel(new BorderLayout(), 0);
        statusBar.setBorder(new EmptyBorder(6, 15, 6, 15));

        String dbModeText = DBConnection.isUseMemoryFallback() ?
                "Database Status: Memory Store (Thread-Safe Fallback Active for Demo)" :
                "Database Status: Connected to MySQL (fams_db)";

        JLabel lblDbStatus = new JLabel(dbModeText);
        lblDbStatus.setFont(Theme.FONT_SMALL);
        lblDbStatus.setForeground(DBConnection.isUseMemoryFallback() ? Theme.PRIMARY : Theme.SUCCESS);

        JLabel lblGroup = new JLabel("ICT2132 Mini Project | Faculty of Technology");
        lblGroup.setFont(Theme.FONT_SMALL);
        lblGroup.setForeground(Theme.TEXT_MUTED);

        statusBar.add(lblDbStatus, BorderLayout.WEST);
        statusBar.add(lblGroup, BorderLayout.EAST);

        mainLayout.add(statusBar, BorderLayout.SOUTH);

        add(mainLayout);
        Theme.applyGlass(mainLayout);

        // Initially select Dashboard
        showCard("CARD_DASHBOARD");
    }

    private JButton createNavButton(String text, String cardName) {
        // Frosted glass buttons on the dark Storm Blue sidebar.
        JButton btn = Theme.createNavButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(210, 42));
        btn.setMinimumSize(new Dimension(190, 42));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        navButtons.put(cardName, btn);
        btn.addActionListener(e -> showCard(cardName));
        return btn;
    }

    public void showCard(String cardName) {
        // Reload data so a medical approved/rejected in one screen shows up in the other immediately.
        if ("CARD_ATTENDANCE".equals(cardName) && attendancePanel != null) attendancePanel.refreshAll();
        if ("CARD_MEDICAL".equals(cardName) && medicalPanel != null) medicalPanel.refreshAll();
        if ("CARD_STUDENT_PROFILES".equals(cardName) && studentProfilePanel != null) studentProfilePanel.refreshStudents();
        if ("CARD_MARKS".equals(cardName)) {
            // Rebuild the marks panel so course lists and data are always current
            if (marksPanel != null) contentCardPanel.remove(marksPanel);
            marksPanel = new MarksManagementPanel();
            contentCardPanel.add(marksPanel, "CARD_MARKS");
        }
        if ("CARD_STUDENT_PORTAL".equals(cardName)) {
            if (studentPortalPanel != null) contentCardPanel.remove(studentPortalPanel);
            studentPortalPanel = new StudentPortalPanel();
            contentCardPanel.add(studentPortalPanel, "CARD_STUDENT_PORTAL");
        }
        cardLayout.show(contentCardPanel, cardName);
        for (java.util.Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton b = entry.getValue();
            Theme.setNavSelected(b, entry.getKey().equals(cardName));
        }
        Theme.applyGlass(contentCardPanel);
    }

    private void onProfileUpdated() {
        User currentUser = UserManagement.getInstance().getCurrentUser();
        if (currentUser != null) {
            if (topBarAvatar != null) {
                topBarAvatar.setFallbackInitials(currentUser.getFullName());
                topBarAvatar.setImagePath(currentUser.getProfilePictureUrl());
            }
            if (userInfoLabel != null) {
                userInfoLabel.setText(currentUser.getFullName() + " [" + currentUser.getRole().getDisplayName() + "]");
            }
        }
        if (dashboardPanel != null) {
            dashboardPanel.refreshUserProfile();
        }
    }

    private void logout() {
        UserManagement.getInstance().logout();
        dispose();
        new LoginFrame(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        }).setVisible(true);
    }
}
