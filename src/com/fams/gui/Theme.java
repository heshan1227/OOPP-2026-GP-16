package com.fams.gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.awt.*;

public class Theme {
    public static final Color PRIMARY = new Color(37, 99, 235);      // Vibrant Blue
    public static final Color PRIMARY_DARK = new Color(29, 78, 216);
    public static final Color SECONDARY = new Color(71, 85, 105);    // Slate
    public static final Color DARK_BG = new Color(15, 23, 42);       // Navy Slate Header
    public static final Color LIGHT_BG = new Color(248, 250, 252);   // Background
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_MAIN = new Color(15, 23, 42);
    public static final Color TEXT_MUTED = new Color(100, 116, 139);
    public static final Color BORDER = new Color(226, 232, 240);

    public static final Color SUCCESS = new Color(16, 185, 129);     // Emerald Green
    public static final Color WARNING = new Color(245, 158, 11);    // Amber
    public static final Color DANGER = new Color(239, 68, 68);       // Red

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    private static void styleButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.BLACK);
        btn.setBackground(Color.WHITE);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(new LineBorder(new Color(203,213,225), 1), new EmptyBorder(8, 12, 8, 12)));
        btn.setMinimumSize(new Dimension(225, 48));
        btn.setPreferredSize(new Dimension(225, 48));
        btn.setMaximumSize(new Dimension(225, 48));
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(new Color(241,245,249)); btn.setForeground(Color.BLACK); }
            public void mouseExited(java.awt.event.MouseEvent e) { btn.setBackground(Color.WHITE); btn.setForeground(Color.BLACK); }
        });
    }

    public static JButton createPrimaryButton(String text) { JButton btn = new JButton(text); styleButton(btn); return btn; }
    public static JButton createSuccessButton(String text) { JButton btn = createPrimaryButton(text); return btn; }
    public static JButton createDangerButton(String text) { JButton btn = createPrimaryButton(text); return btn; }
    public static JButton createSecondaryButton(String text) { JButton btn = createPrimaryButton(text); return btn; }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(15, 15, 15, 15)));
        return panel;
    }

    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_HEADER);
        label.setForeground(TEXT_MAIN);
        return label;
    }
}
