package com.fams.gui;

import com.fams.model.Notice;
import com.fams.model.Role;
import com.fams.model.User;
import com.fams.service.AuthService;
import com.fams.service.NoticeService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NoticePanel extends JPanel {
    private final NoticeService noticeService;
    private JPanel noticeCardsContainer;

    public NoticePanel() {
        this.noticeService = new NoticeService();

        setLayout(new BorderLayout(15, 15));
        setBackground(Theme.LIGHT_BG);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
    }

    private void initComponents() {
        User user = AuthService.getInstance().getCurrentUser();

        // Top Control Bar
        JPanel topCard = Theme.createCardPanel();
        topCard.setLayout(new BorderLayout());

        JLabel lblTitle = Theme.createHeaderLabel("Faculty & Department Notice Board");
        topCard.add(lblTitle, BorderLayout.WEST);

        if (user != null && user.getRole() == Role.ADMIN) {
            JButton btnCreateNotice = Theme.createPrimaryButton("+ Publish New Notice (Admin)");
            btnCreateNotice.addActionListener(e -> showCreateNoticeDialog());
            topCard.add(btnCreateNotice, BorderLayout.EAST);
        }

        add(topCard, BorderLayout.NORTH);

        // Center Scroll Area for Notice Cards
        noticeCardsContainer = new JPanel();
        noticeCardsContainer.setLayout(new BoxLayout(noticeCardsContainer, BoxLayout.Y_AXIS));
        noticeCardsContainer.setBackground(Theme.LIGHT_BG);

        JScrollPane scrollPane = new JScrollPane(noticeCardsContainer);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        refreshNotices();
    }

    private void refreshNotices() {
        noticeCardsContainer.removeAll();
        List<Notice> list = noticeService.getNoticesForCurrentSession();

        if (list.isEmpty()) {
            JLabel lblEmpty = new JLabel("No published notices available for your role at this time.", SwingConstants.CENTER);
            lblEmpty.setFont(Theme.FONT_REGULAR);
            lblEmpty.setForeground(Theme.TEXT_MUTED);
            noticeCardsContainer.add(lblEmpty);
        } else {
            for (Notice n : list) {
                noticeCardsContainer.add(createNoticeCard(n));
                noticeCardsContainer.add(Box.createVerticalStrut(12));
            }
        }
        noticeCardsContainer.revalidate();
        noticeCardsContainer.repaint();
    }

    private JPanel createNoticeCard(Notice n) {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);

        Color borderColor = n.isImportant() ? Theme.DANGER : Theme.BORDER;
        card.setBorder(new CompoundBorder(new LineBorder(borderColor, n.isImportant() ? 2 : 1, true), new EmptyBorder(12, 15, 12, 15)));

        JPanel cardHeader = new JPanel(new BorderLayout());
        cardHeader.setBackground(Color.WHITE);

        JLabel lblNoticeTitle = new JLabel(n.getTitle());
        lblNoticeTitle.setFont(Theme.FONT_HEADER);
        lblNoticeTitle.setForeground(Theme.TEXT_MAIN);

        JLabel lblMeta = new JLabel("Published by " + n.getPublishedBy() + " on " + n.getPublishedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        lblMeta.setFont(Theme.FONT_SMALL);
        lblMeta.setForeground(Theme.TEXT_MUTED);

        cardHeader.add(lblNoticeTitle, BorderLayout.WEST);
        cardHeader.add(lblMeta, BorderLayout.EAST);

        JTextArea txtContent = new JTextArea(n.getContent());
        txtContent.setFont(Theme.FONT_REGULAR);
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        txtContent.setEditable(false);
        txtContent.setBackground(Color.WHITE);
        txtContent.setBorder(new EmptyBorder(8, 0, 8, 0));

        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.WHITE);

        JLabel lblTarget = new JLabel("Target Audience: " + n.getTargetRoleText());
        lblTarget.setFont(Theme.FONT_SMALL);
        lblTarget.setForeground(Theme.PRIMARY);

        footerPanel.add(lblTarget, BorderLayout.WEST);

        User currentUser = AuthService.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getRole() == Role.ADMIN) {
            JButton btnDelete = new JButton("Delete Notice");
            btnDelete.setFont(Theme.FONT_SMALL);
            btnDelete.setForeground(Theme.DANGER);
            btnDelete.addActionListener(e -> {
                try {
                    noticeService.deleteNotice(n.getNoticeId());
                    refreshNotices();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            footerPanel.add(btnDelete, BorderLayout.EAST);
        }

        card.add(cardHeader, BorderLayout.NORTH);
        card.add(txtContent, BorderLayout.CENTER);
        card.add(footerPanel, BorderLayout.SOUTH);

        return card;
    }

    private void showCreateNoticeDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Publish New Notice (Admin)", true);
        dlg.setSize(450, 400);
        dlg.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        panel.add(new JLabel("Notice Title:"), gbc);
        gbc.gridy++;
        JTextField txtTitle = new JTextField(25);
        panel.add(txtTitle, gbc);

        gbc.gridy++;
        panel.add(new JLabel("Notice Content:"), gbc);
        gbc.gridy++;
        JTextArea txtContent = new JTextArea(5, 25);
        txtContent.setLineWrap(true);
        panel.add(new JScrollPane(txtContent), gbc);

        gbc.gridy++;
        panel.add(new JLabel("Target Role:"), gbc);
        gbc.gridy++;
        JComboBox<Object> cbTarget = new JComboBox<>(new Object[]{"ALL USERS", Role.UNDERGRADUATE, Role.LECTURER, Role.TECHNICAL_OFFICER});
        panel.add(cbTarget, gbc);

        gbc.gridy++;
        JCheckBox chkImp = new JCheckBox("Mark as Important Notice");
        panel.add(chkImp, gbc);

        gbc.gridy++;
        JButton btnPublish = Theme.createPrimaryButton("Publish Notice");
        btnPublish.addActionListener(e -> {
            try {
                String title = txtTitle.getText().trim();
                String content = txtContent.getText().trim();
                Object targetObj = cbTarget.getSelectedItem();
                Role targetRole = (targetObj instanceof Role) ? (Role) targetObj : null;
                boolean imp = chkImp.isSelected();

                User admin = AuthService.getInstance().getCurrentUser();
                Notice notice = new Notice(0, title, content, admin.getFullName(), LocalDateTime.now(), targetRole, imp);
                noticeService.publishNotice(notice);

                dlg.dispose();
                refreshNotices();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        panel.add(btnPublish, gbc);

        dlg.add(panel);
        dlg.setVisible(true);
    }
}
