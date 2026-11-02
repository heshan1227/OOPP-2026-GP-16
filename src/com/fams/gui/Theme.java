package com.fams.gui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

/** Central visual theme for the existing FAMS Swing interface. */
public final class Theme {
    private Theme() { }

    // University maroon palette
    public static final Color PRIMARY = new Color(91, 23, 40);       // Deep maroon
    public static final Color PRIMARY_DARK = new Color(65, 14, 28);  // Dark maroon
    public static final Color PRIMARY_LIGHT = new Color(150, 61, 82);
    public static final Color SECONDARY = new Color(91, 72, 77);
    public static final Color DARK_BG = new Color(65, 14, 28);
    public static final Color LIGHT_BG = new Color(248, 245, 246);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_MAIN = new Color(43, 31, 34);
    public static final Color TEXT_MUTED = new Color(119, 102, 107);
    public static final Color BORDER = new Color(229, 216, 220);

    // Semantic colours remain distinct for status feedback.
    public static final Color SUCCESS = new Color(22, 120, 82);
    public static final Color WARNING = new Color(180, 112, 20);
    public static final Color DANGER = new Color(177, 40, 50);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    /** Apply the shared FAMS button treatment to any JButton, including legacy buttons. */
    public static JButton styleButton(JButton btn) {
        if (btn == null || Boolean.TRUE.equals(btn.getClientProperty("fams.buttonStyled"))) return btn;

        String label = btn.getText() == null ? "" : btn.getText().trim().toLowerCase(java.util.Locale.ROOT);
        boolean danger = label.contains("delete") || label.contains("remove") || label.contains("reject");
        boolean secondary = label.contains("cancel") || label.contains("refresh") || label.contains("reset")
                || label.contains("browse") || label.equals("...") || label.contains("view")
                || label.contains("attach") || label.contains("upload") || label.contains("close");

        Color background = danger ? DANGER : (secondary ? Color.WHITE : PRIMARY);
        Color foreground = danger || !secondary ? Color.WHITE : PRIMARY;
        Color hover = danger ? new Color(145, 28, 40)
                : (secondary ? new Color(245, 232, 236) : PRIMARY_DARK);
        Color pressed = danger ? new Color(120, 20, 32)
                : (secondary ? new Color(235, 218, 223) : new Color(48, 9, 21));

        // Use BasicButtonUI so Windows native Look & Feel does not wash out custom colours.
        btn.setUI(new BasicButtonUI());
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(foreground);
        btn.setBackground(background);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorderPainted(true);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(new LineBorder(secondary ? PRIMARY_LIGHT : background.darker(), 2, true),
                new EmptyBorder(9, 16, 9, 16)));
        btn.setMargin(new Insets(9, 16, 9, 16));
        btn.setMinimumSize(new Dimension(100, 40));
        btn.setPreferredSize(new Dimension(Math.max(110, btn.getPreferredSize().width), 42));
        btn.putClientProperty("fams.buttonStyled", Boolean.TRUE);

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(hover);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(btn.isEnabled() ? background : new Color(226, 222, 223));
            }
            @Override public void mousePressed(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(pressed);
            }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) {
                    Point p = e.getPoint();
                    btn.setBackground(btn.contains(p) ? hover : background);
                }
            }
        });
        btn.addPropertyChangeListener("enabled", e -> {
            btn.setBackground(Boolean.TRUE.equals(e.getNewValue()) ? background : new Color(226, 222, 223));
            btn.setForeground(Boolean.TRUE.equals(e.getNewValue()) ? foreground : new Color(105, 95, 98));
        });
        return btn;
    }

    private static JButton button(String text, Color background, Color foreground, Color hover) {
        JButton btn = new JButton(text);
        // Force reliable colour rendering across Windows, Linux, and macOS Look & Feels.
        btn.setUI(new BasicButtonUI());
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(foreground);
        btn.setBackground(background);
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new CompoundBorder(new LineBorder(background.darker(), 2, true), new EmptyBorder(9, 14, 9, 14)));
        btn.setMinimumSize(new Dimension(100, 40));
        btn.setPreferredSize(new Dimension(225, 44));
        btn.putClientProperty("fams.buttonStyled", Boolean.TRUE);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { if (btn.isEnabled()) btn.setBackground(hover); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                boolean selected = Boolean.TRUE.equals(btn.getClientProperty("fams.navSelected"));
                btn.setBackground(btn.isEnabled() ? (selected ? PRIMARY_LIGHT : background) : new Color(226, 222, 223));
            }
            @Override public void mousePressed(java.awt.event.MouseEvent e) { if (btn.isEnabled()) btn.setBackground(background.darker()); }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(btn.contains(e.getPoint()) ? hover
                        : (Boolean.TRUE.equals(btn.getClientProperty("fams.navSelected")) ? PRIMARY_LIGHT : background));
            }
        });
        btn.addPropertyChangeListener("enabled", e -> {
            boolean enabled = Boolean.TRUE.equals(e.getNewValue());
            btn.setBackground(enabled ? background : new Color(226, 222, 223));
            btn.setForeground(enabled ? foreground : new Color(105, 95, 98));
        });
        return btn;
    }

    /** Keep the active sidebar item highlighted even after the pointer leaves it. */
    public static void setNavSelected(JButton btn, boolean selected) {
        if (btn == null) return;
        btn.putClientProperty("fams.navSelected", selected);
        // Unselected items must contrast with the dark-maroon sidebar.
        btn.setBackground(selected ? PRIMARY_LIGHT : Color.WHITE);
        btn.setForeground(selected ? Color.WHITE : PRIMARY_DARK);
    }

    public static JButton createPrimaryButton(String text) {
        return button(text, PRIMARY, Color.WHITE, PRIMARY_DARK);
    }

    public static JButton createSuccessButton(String text) {
        return button(text, SUCCESS, Color.WHITE, new Color(16, 96, 65));
    }

    public static JButton createDangerButton(String text) {
        return button(text, DANGER, Color.WHITE, new Color(135, 25, 38));
    }

    public static JButton createSecondaryButton(String text) {
        return button(text, Color.WHITE, PRIMARY, new Color(245, 232, 236));
    }

    /** Install defaults before any window is created so even JOptionPane and future buttons are readable. */
    public static void installButtonDefaults() {
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("Button.background", PRIMARY);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.select", PRIMARY_DARK);
        UIManager.put("Button.focus", PRIMARY_LIGHT);
        UIManager.put("Button.disabledText", new Color(105, 95, 98));
        UIManager.put("Button.margin", new Insets(8, 14, 8, 14));
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("OptionPane.buttonMinimumSize", new Dimension(90, 36));
    }

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
