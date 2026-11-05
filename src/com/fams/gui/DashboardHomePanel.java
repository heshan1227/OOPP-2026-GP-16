package com.fams.gui;

import com.fams.model.User;
import com.fams.service.AuthService;
import com.fams.service.NoticeService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DashboardHomePanel extends JPanel {
    private final NoticeService noticeService;
    private final java.util.function.Consumer<String> navigationHandler;

    private CircularAvatar avatar;
    private JLabel lblWelcome;
    private JLabel lblDesc;

    public DashboardHomePanel() {
        this(null);
    }

    public DashboardHomePanel(java.util.function.Consumer<String> navigationHandler) {
        this.noticeService = new NoticeService();
        this.navigationHandler = navigationHandler;
        setLayout(new BorderLayout(15, 15));
        setBackground(Theme.LIGHT_BG);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents();
    }

    private void initComponents() {
        User user = AuthService.getInstance().getCurrentUser();
        if (user == null) return;

        // Welcome Header Banner
        JPanel welcomeCard = Theme.createCardPanel();
        welcomeCard.setLayout(new BorderLayout(15, 0));

        JPanel bannerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        bannerLeft.setOpaque(false);

        // Circular profile photo avatar inside red circle area
        avatar = new CircularAvatar(68, user.getProfilePictureUrl(), user.getFullName());
        avatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        avatar.setToolTipText("Click to view My Profile");
        if (navigationHandler != null) {
            avatar.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    navigationHandler.accept("CARD_PROFILE");
                }
            });
        }

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setBackground(Color.WHITE);

        lblWelcome = new JLabel("Welcome back, " + user.getFullName() + "!");
        lblWelcome.setFont(Theme.FONT_TITLE);
        lblWelcome.setForeground(Theme.PRIMARY_DARK);

        lblDesc = new JLabel(user.getRoleSpecificDescription());
        lblDesc.setFont(Theme.FONT_REGULAR);
        lblDesc.setForeground(Theme.TEXT_MUTED);

        headerText.add(lblWelcome);
        headerText.add(Box.createVerticalStrut(5));
        headerText.add(lblDesc);

        bannerLeft.add(avatar);
        bannerLeft.add(headerText);

        JLabel roleBadge = new JLabel(" ROLE: " + user.getRole().getDisplayName().toUpperCase() + " ");
        roleBadge.setOpaque(true);
        roleBadge.setBackground(Theme.PRIMARY);
        roleBadge.setForeground(Color.WHITE);
        roleBadge.setFont(Theme.FONT_SUBHEADER);
        roleBadge.setBorder(new EmptyBorder(6, 12, 6, 12));

        welcomeCard.add(bannerLeft, BorderLayout.CENTER);
        welcomeCard.add(roleBadge, BorderLayout.EAST);

        add(welcomeCard, BorderLayout.NORTH);

        // Center Grid: Stats & Module Overview
        JPanel centerGrid = new JPanel(new GridLayout(2, 2, 15, 15));
        centerGrid.setBackground(Theme.LIGHT_BG);

        centerGrid.add(createStatCard("Attendance Subsystem", "15 Theory + 15 Practical Sessions", "Technical Officer: Add/Update Records | Lecturer & UG: View Summaries", Theme.PRIMARY, "CARD_ATTENDANCE"));
        centerGrid.add(createStatCard("Medical Subsystem", "Medical Verification & Approval", "Pending, Approved, Rejected Status Tracking", Theme.SUCCESS, "CARD_MEDICAL"));
        centerGrid.add(createStatCard("Notice Board", noticeService.getNoticesForCurrentSession().size() + " Active Notices", "Admin: Publish/Delete | All Roles: View Notices", Theme.WARNING, "CARD_NOTICES"));
        centerGrid.add(createStatCard("Timetables", "Department Schedules", "Admin: Build Schedule | TO & UG: View Timetable", Theme.SECONDARY, "CARD_TIMETABLE"));

        add(centerGrid, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, String mainValue, String subText, Color accentColor, String cardTarget) {
        JPanel card = Theme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(Theme.FONT_HEADER);
        lblTitle.setForeground(accentColor);

        JLabel lblValue = new JLabel(mainValue);
        lblValue.setFont(Theme.FONT_TITLE);
        lblValue.setForeground(Theme.TEXT_MAIN);

        JLabel lblSub = new JLabel(subText);
        lblSub.setFont(Theme.FONT_SMALL);
        lblSub.setForeground(Theme.TEXT_MUTED);

        card.add(lblTitle);
        card.add(Box.createVerticalStrut(8));
        card.add(lblValue);
        card.add(Box.createVerticalStrut(5));
        card.add(lblSub);

        if (cardTarget != null && navigationHandler != null) {
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    navigationHandler.accept(cardTarget);
                }
            });
        }

        return card;
    }

    public void refreshUserProfile() {
        User user = AuthService.getInstance().getCurrentUser();
        if (user == null) return;
        if (avatar != null) {
            avatar.setFallbackInitials(user.getFullName());
            avatar.setImagePath(user.getProfilePictureUrl());
        }
        if (lblWelcome != null) {
            lblWelcome.setText("Welcome back, " + user.getFullName() + "!");
        }
        if (lblDesc != null) {
            lblDesc.setText(user.getRoleSpecificDescription());
        }
    }
}
