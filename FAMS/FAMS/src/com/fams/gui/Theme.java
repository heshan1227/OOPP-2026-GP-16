package com.fams.gui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicPanelUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Central visual theme for the FAMS Swing interface.
 *
 * Palette: Storm Blue (#34506D) with a "liquid glass" treatment.
 * Swing cannot blur what is behind a component, so the glass effect is built from
 * a soft gradient backdrop, translucent rounded panels, a specular top highlight,
 * a light edge stroke and a soft drop shadow.
 *
 * Call {@link #applyGlass(Component)} once on any finished window / dialog / panel tree so
 * legacy panels (plain JPanels with LIGHT_BG / white backgrounds) pick up the glass look.
 */
public final class Theme {
    private Theme() { }

    // ───────────────────────── Storm Blue palette ─────────────────────────
    public static final Color STORM_BLUE = new Color(0x34, 0x50, 0x6D);   // #34506D
    public static final Color PRIMARY = STORM_BLUE;
    public static final Color PRIMARY_DARK = new Color(0x22, 0x37, 0x4D);  // deep storm
    public static final Color PRIMARY_LIGHT = new Color(0x5E, 0x82, 0xA8); // lifted storm blue
    public static final Color SECONDARY = new Color(0x5B, 0x6E, 0x82);
    public static final Color DARK_BG = new Color(0x1C, 0x2D, 0x40);
    public static final Color LIGHT_BG = new Color(0xE6, 0xEE, 0xF6);      // key colour for transparent panels
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_MAIN = new Color(0x16, 0x24, 0x33);
    public static final Color TEXT_MUTED = new Color(0x5A, 0x6C, 0x80);
    public static final Color BORDER = new Color(0xC3, 0xD1, 0xE0);

    // Semantic colours remain distinct for status feedback.
    public static final Color SUCCESS = new Color(22, 120, 82);
    public static final Color WARNING = new Color(180, 112, 20);
    public static final Color DANGER = new Color(177, 40, 50);

    // Table tints (solid so cell renderers stay crisp)
    public static final Color TABLE_ROW_A = new Color(244, 248, 252);
    public static final Color TABLE_ROW_B = new Color(226, 235, 244);
    public static final Color TABLE_SELECT = new Color(170, 194, 220);

    // Glass tints
    public static final Color GLASS_FILL = new Color(255, 255, 255, 150);
    public static final Color GLASS_FILL_STRONG = new Color(255, 255, 255, 190);
    public static final Color GLASS_DARK = new Color(0x34, 0x50, 0x6D, 232);
    public static final Color GLASS_DARKER = new Color(0x22, 0x37, 0x4D, 238);

    private static final Color DISABLED_BG = new Color(205, 214, 224, 210);
    private static final Color DISABLED_FG = new Color(110, 124, 140);
    private static final Color BTN_HOVER = new Color(0x46, 0x6A, 0x90);

    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    // ───────────────────────── Glass painting ─────────────────────────

    /** Paints the soft Storm Blue backdrop that every glass surface sits on. */
    public static void paintBackdrop(Graphics2D g, int w, int h) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new GradientPaint(0, 0, new Color(0xDD, 0xE8, 0xF3), w, h, new Color(0xA7, 0xBD, 0xD4)));
        g2.fillRect(0, 0, w, h);
        float r = Math.max(w, h);
        blob(g2, w * 0.12f, h * 0.08f, r * 0.60f, new Color(0x5E, 0x82, 0xA8, 120));
        blob(g2, w * 0.92f, h * 0.90f, r * 0.60f, new Color(0x34, 0x50, 0x6D, 105));
        blob(g2, w * 0.80f, h * 0.10f, r * 0.45f, new Color(255, 255, 255, 150));
        blob(g2, w * 0.25f, h * 0.95f, r * 0.40f, new Color(0x8F, 0xB0, 0xD0, 90));
        g2.dispose();
    }

    private static void blob(Graphics2D g, float cx, float cy, float radius, Color c) {
        Color clear = new Color(c.getRed(), c.getGreen(), c.getBlue(), 0);
        g.setPaint(new RadialGradientPaint(new Point2D.Float(cx, cy), radius,
                new float[]{0f, 1f}, new Color[]{c, clear}));
        g.fill(new Rectangle2D.Float(cx - radius, cy - radius, radius * 2, radius * 2));
    }

    /**
     * Paints one liquid-glass surface: soft shadow, translucent body, specular sheen and a light edge.
     * The shape (plus shadow) stays inside x,y,w,h.
     */
    public static void paintGlass(Graphics2D g, int x, int y, int w, int h, int radius, Color tint, boolean shadow) {
        if (w <= 4 || h <= 4) return;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        boolean flat = radius <= 0;
        if (flat) shadow = false;
        int inset = flat ? 0 : (shadow ? 4 : 1);
        int bottomInset = flat ? 0 : (shadow ? 6 : 1);
        int topInset = flat ? 0 : 1;
        float bx = x + inset, by = y + topInset, bw = w - inset * 2, bh = h - topInset - bottomInset;
        RoundRectangle2D body = new RoundRectangle2D.Float(bx, by, bw, bh, radius, radius);

        if (shadow) {
            for (int i = 4; i >= 1; i--) {
                g2.setColor(new Color(0x1C, 0x2D, 0x40, 9 + (4 - i) * 2));
                g2.fill(new RoundRectangle2D.Float(bx - i + 1, by + 1, bw + (i - 1) * 2, bh + i, radius + i, radius + i));
            }
        }

        boolean dark = luminance(tint) < 0.45f;

        g2.setColor(tint);
        g2.fill(body);

        Shape oldClip = g2.getClip();
        g2.clip(body);
        // specular sheen at the top
        g2.setPaint(new GradientPaint(0, by, new Color(255, 255, 255, dark ? 46 : 130),
                0, by + bh * 0.55f, new Color(255, 255, 255, 0)));
        g2.fill(new Rectangle2D.Float(bx, by, bw, bh * 0.55f));
        // faint cool glow rising from the bottom edge
        g2.setPaint(new GradientPaint(0, by + bh, new Color(0x8F, 0xB0, 0xD0, dark ? 40 : 45),
                0, by + bh * 0.70f, new Color(0x8F, 0xB0, 0xD0, 0)));
        g2.fill(new Rectangle2D.Float(bx, by + bh * 0.70f, bw, bh * 0.30f));
        g2.setClip(oldClip);

        // glass edge: bright on top, softer at the bottom
        g2.setStroke(new BasicStroke(1.2f));
        g2.setPaint(new GradientPaint(0, by, new Color(255, 255, 255, dark ? 110 : 235),
                0, by + bh, new Color(255, 255, 255, dark ? 30 : 90)));
        g2.draw(new RoundRectangle2D.Float(bx + 0.6f, by + 0.6f, bw - 1.2f, bh - 1.2f, radius, radius));
        g2.dispose();
    }

    private static float luminance(Color c) {
        return (0.299f * c.getRed() + 0.587f * c.getGreen() + 0.114f * c.getBlue()) / 255f;
    }

    /** Panel painted as a rounded translucent glass surface. */
    public static class GlassPanel extends JPanel {
        private final int radius;
        private final Color tint;
        private final boolean shadow;

        public GlassPanel(LayoutManager layout, int radius, Color tint, boolean shadow) {
            super(layout);
            this.radius = radius;
            this.tint = tint;
            this.shadow = shadow;
            setOpaque(false);
            putClientProperty("fams.glass", Boolean.TRUE);
        }

        public GlassPanel(LayoutManager layout) { this(layout, 20, GLASS_FILL_STRONG, true); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            paintGlass(g2, 0, 0, getWidth(), getHeight(), radius, tint, shadow);
            g2.dispose();
        }
    }

    /** Panel that paints the Storm Blue backdrop. Use as the root of a window or content area. */
    public static class BackdropPanel extends JPanel {
        public BackdropPanel(LayoutManager layout) {
            super(layout);
            setOpaque(true);
            setBackground(LIGHT_BG);
        }

        @Override protected void paintComponent(Graphics g) {
            paintBackdrop((Graphics2D) g, getWidth(), getHeight());
        }
    }

    /** Dark storm-blue glass bar (top bar / sidebar). */
    public static JPanel createDarkGlassPanel(LayoutManager layout, int radius) {
        return new GlassPanel(layout, radius, GLASS_DARK, true);
    }

    /** Light glass bar (status bar etc.). */
    public static JPanel createLightGlassPanel(LayoutManager layout, int radius) {
        return new GlassPanel(layout, radius, GLASS_FILL, false);
    }

    /** UI delegate that turns any plain JPanel into a glass card. */
    private static final class GlassPanelUI extends BasicPanelUI {
        @Override public void update(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            paintGlass(g2, 0, 0, c.getWidth(), c.getHeight(), 18, GLASS_FILL_STRONG, true);
            Object edge = ((JComponent) c).getClientProperty("fams.glassEdge");
            if (edge instanceof Color) {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(2f));
                g2.setColor((Color) edge);
                g2.draw(new RoundRectangle2D.Float(5, 2, c.getWidth() - 10, c.getHeight() - 9, 18, 18));
            }
            g2.dispose();
            paint(g, c);
        }
    }

    // ───────────────────────── Buttons ─────────────────────────

    /** Rounded glass button: gradient body, specular sheen and light edge. */
    private static final class GlassButtonUI extends BasicButtonUI {
        @Override public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = c.getWidth(), h = c.getHeight();
            Color base = b.getBackground();
            boolean dark = luminance(base) < 0.55f;
            RoundRectangle2D shape = new RoundRectangle2D.Float(1, 1, w - 2, h - 2, 16, 16);

            g2.setPaint(new GradientPaint(0, 0, lighten(base, dark ? 0.14f : 0.0f), 0, h, darken(base, dark ? 0.0f : 0.04f)));
            g2.fill(shape);

            Shape old = g2.getClip();
            g2.clip(shape);
            g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, dark ? 70 : 140),
                    0, h * 0.55f, new Color(255, 255, 255, 0)));
            g2.fill(new Rectangle2D.Float(0, 0, w, h * 0.55f));
            g2.setClip(old);

            Object edge = b.getClientProperty("fams.glassBorder");
            Color ec = edge instanceof Color ? (Color) edge : new Color(255, 255, 255, 150);
            g2.setStroke(new BasicStroke(1.3f));
            g2.setColor(ec);
            g2.draw(new RoundRectangle2D.Float(1.2f, 1.2f, w - 2.4f, h - 2.4f, 16, 16));
            g2.dispose();
            super.paint(g, c);
        }

        private static Color lighten(Color c, float f) {
            return new Color(Math.min(255, Math.round(c.getRed() + (255 - c.getRed()) * f)),
                    Math.min(255, Math.round(c.getGreen() + (255 - c.getGreen()) * f)),
                    Math.min(255, Math.round(c.getBlue() + (255 - c.getBlue()) * f)), c.getAlpha());
        }

        private static Color darken(Color c, float f) {
            return new Color(Math.round(c.getRed() * (1 - f)), Math.round(c.getGreen() * (1 - f)),
                    Math.round(c.getBlue() * (1 - f)), c.getAlpha());
        }
    }

    private static void glassifyButton(JButton btn, Color background, Color foreground, Color edge, Insets pad) {
        btn.setUI(new GlassButtonUI());
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(foreground);
        btn.setBackground(background);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setRolloverEnabled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(pad));
        btn.setMargin(pad);
        btn.setMinimumSize(new Dimension(100, 40));
        btn.putClientProperty("fams.glassBorder", edge);
        btn.putClientProperty("fams.buttonStyled", Boolean.TRUE);
    }

    private static void wireStates(JButton btn, Color background, Color foreground, Color hover, Color pressed) {
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { if (btn.isEnabled()) btn.setBackground(hover); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(btn.isEnabled() ? background : DISABLED_BG);
            }
            @Override public void mousePressed(java.awt.event.MouseEvent e) { if (btn.isEnabled()) btn.setBackground(pressed); }
            @Override public void mouseReleased(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(btn.contains(e.getPoint()) ? hover : background);
            }
        });
        btn.addPropertyChangeListener("enabled", e -> {
            boolean on = Boolean.TRUE.equals(e.getNewValue());
            btn.setBackground(on ? background : DISABLED_BG);
            btn.setForeground(on ? foreground : DISABLED_FG);
        });
    }

    /** Apply the shared FAMS button treatment to any JButton, including legacy buttons. */
    public static JButton styleButton(JButton btn) {
        if (btn == null || Boolean.TRUE.equals(btn.getClientProperty("fams.buttonStyled"))) return btn;

        String label = btn.getText() == null ? "" : btn.getText().trim().toLowerCase(java.util.Locale.ROOT);
        boolean danger = label.contains("delete") || label.contains("remove") || label.contains("reject");
        boolean secondary = label.contains("cancel") || label.contains("refresh") || label.contains("reset")
                || label.contains("browse") || label.equals("...") || label.contains("view")
                || label.contains("attach") || label.contains("upload") || label.contains("close");

        Color background = danger ? DANGER : (secondary ? new Color(255, 255, 255, 185) : PRIMARY);
        Color foreground = danger || !secondary ? Color.WHITE : PRIMARY;
        Color hover = danger ? new Color(145, 28, 40) : (secondary ? new Color(255, 255, 255, 235) : BTN_HOVER);
        Color pressed = danger ? new Color(120, 20, 32) : (secondary ? new Color(205, 220, 235, 240) : PRIMARY_DARK);
        Color edge = secondary ? new Color(0x5E, 0x82, 0xA8, 190) : new Color(255, 255, 255, 140);

        glassifyButton(btn, background, foreground, edge, new Insets(9, 16, 9, 16));
        btn.setPreferredSize(new Dimension(Math.max(110, btn.getPreferredSize().width), 42));
        wireStates(btn, background, foreground, hover, pressed);
        return btn;
    }

    private static JButton button(String text, Color background, Color foreground, Color hover, Color pressed, Color edge) {
        JButton btn = new JButton(text);
        glassifyButton(btn, background, foreground, edge, new Insets(9, 14, 9, 14));
        btn.setPreferredSize(new Dimension(225, 44));
        wireStates(btn, background, foreground, hover, pressed);
        return btn;
    }

    // Nav buttons: frosted white glass on the dark sidebar.
    private static final Color NAV_IDLE = new Color(255, 255, 255, 36);
    private static final Color NAV_HOVER = new Color(255, 255, 255, 78);
    private static final Color NAV_SELECTED = new Color(255, 255, 255, 222);

    /** Sidebar navigation button. */
    public static JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        glassifyButton(btn, NAV_IDLE, Color.WHITE, new Color(255, 255, 255, 80), new Insets(9, 14, 9, 14));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setPreferredSize(new Dimension(210, 42));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled() && !isNavSelected(btn)) btn.setBackground(NAV_HOVER);
            }
            @Override public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(isNavSelected(btn) ? NAV_SELECTED : NAV_IDLE);
            }
        });
        return btn;
    }

    private static boolean isNavSelected(JButton b) {
        return Boolean.TRUE.equals(b.getClientProperty("fams.navSelected"));
    }

    /** Keep the active sidebar item highlighted even after the pointer leaves it. */
    public static void setNavSelected(JButton btn, boolean selected) {
        if (btn == null) return;
        btn.putClientProperty("fams.navSelected", selected);
        btn.setBackground(selected ? NAV_SELECTED : NAV_IDLE);
        btn.setForeground(selected ? PRIMARY_DARK : Color.WHITE);
        btn.putClientProperty("fams.glassBorder", selected ? new Color(255, 255, 255, 255) : new Color(255, 255, 255, 80));
        btn.repaint();
    }

    public static JButton createPrimaryButton(String text) {
        return button(text, PRIMARY, Color.WHITE, BTN_HOVER, PRIMARY_DARK, new Color(255, 255, 255, 140));
    }

    public static JButton createSuccessButton(String text) {
        return button(text, SUCCESS, Color.WHITE, new Color(16, 96, 65), new Color(10, 76, 50), new Color(255, 255, 255, 140));
    }

    public static JButton createDangerButton(String text) {
        return button(text, DANGER, Color.WHITE, new Color(145, 28, 40), new Color(120, 20, 32), new Color(255, 255, 255, 140));
    }

    public static JButton createSecondaryButton(String text) {
        return button(text, new Color(255, 255, 255, 185), PRIMARY, new Color(255, 255, 255, 235),
                new Color(205, 220, 235, 240), new Color(0x5E, 0x82, 0xA8, 190));
    }

    // ───────────────────────── Defaults for every new component ─────────────────────────

    /** Install defaults before any window is created so even JOptionPane and future components match. */
    public static void installButtonDefaults() {
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("Button.background", new ColorUIResource(PRIMARY));
        UIManager.put("Button.foreground", new ColorUIResource(Color.WHITE));
        UIManager.put("Button.select", new ColorUIResource(PRIMARY_DARK));
        UIManager.put("Button.focus", new ColorUIResource(PRIMARY_LIGHT));
        UIManager.put("Button.disabledText", new ColorUIResource(DISABLED_FG));
        UIManager.put("Button.margin", new Insets(8, 14, 8, 14));
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 13));
        UIManager.put("OptionPane.buttonMinimumSize", new Dimension(90, 36));
        installGlassDefaults();
    }

    /** Storm Blue defaults for fields, selections, tabs and dialogs. */
    public static void installGlassDefaults() {
        Color field = new Color(0xFA, 0xFC, 0xFF);
        javax.swing.border.Border fieldBorder = new FieldBorder(new Color(0xB3, 0xC5, 0xD8));
        for (String k : new String[]{"TextField", "PasswordField", "FormattedTextField", "TextArea", "TextPane", "EditorPane"}) {
            UIManager.put(k + ".background", new ColorUIResource(field));
            UIManager.put(k + ".selectionBackground", new ColorUIResource(PRIMARY_LIGHT));
            UIManager.put(k + ".selectionForeground", new ColorUIResource(Color.WHITE));
            UIManager.put(k + ".caretForeground", new ColorUIResource(PRIMARY_DARK));
        }
        UIManager.put("TextField.border", fieldBorder);
        UIManager.put("PasswordField.border", fieldBorder);
        UIManager.put("FormattedTextField.border", fieldBorder);
        UIManager.put("ComboBox.background", new ColorUIResource(field));
        UIManager.put("ComboBox.selectionBackground", new ColorUIResource(PRIMARY_LIGHT));
        UIManager.put("ComboBox.selectionForeground", new ColorUIResource(Color.WHITE));
        UIManager.put("Spinner.background", new ColorUIResource(field));
        UIManager.put("List.selectionBackground", new ColorUIResource(PRIMARY_LIGHT));
        UIManager.put("List.selectionForeground", new ColorUIResource(Color.WHITE));
        UIManager.put("Table.selectionBackground", new ColorUIResource(TABLE_SELECT));
        UIManager.put("Table.selectionForeground", new ColorUIResource(PRIMARY_DARK));
        UIManager.put("Table.gridColor", new ColorUIResource(BORDER));
        UIManager.put("ScrollBar.thumb", new ColorUIResource(PRIMARY_LIGHT));
        UIManager.put("TabbedPane.foreground", new ColorUIResource(TEXT_MAIN));
        UIManager.put("TabbedPane.selectedForeground", new ColorUIResource(Color.WHITE));
        UIManager.put("Panel.background", new ColorUIResource(LIGHT_BG));
        UIManager.put("OptionPane.background", new ColorUIResource(LIGHT_BG));
        UIManager.put("OptionPane.messageForeground", new ColorUIResource(TEXT_MAIN));
        UIManager.put("ToolTip.background", new ColorUIResource(PRIMARY_DARK));
        UIManager.put("ToolTip.foreground", new ColorUIResource(Color.WHITE));
    }

    /** Light 1px field border with comfortable padding. */
    private static final class FieldBorder extends CompoundBorder implements javax.swing.plaf.UIResource {
        FieldBorder(Color line) {
            super(new LineBorder(line, 1), new EmptyBorder(4, 8, 4, 8));
        }
    }

    // ───────────────────────── Tabs ─────────────────────────

    private static final class GlassTabbedPaneUI extends BasicTabbedPaneUI {
        @Override protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(7, 16, 7, 16);
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            tabAreaInsets = new Insets(2, 2, 6, 2);
            contentBorderInsets = new Insets(0, 0, 0, 0);
        }

        @Override protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex,
                                                    int x, int y, int w, int h, boolean isSelected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D r = new RoundRectangle2D.Float(x + 2, y + 1, w - 4, h - 2, 14, 14);
            if (isSelected) {
                g2.setPaint(new GradientPaint(0, y, PRIMARY_LIGHT, 0, y + h, PRIMARY));
            } else {
                g2.setColor(new Color(255, 255, 255, 120));
            }
            g2.fill(r);
            g2.setPaint(new GradientPaint(0, y, new Color(255, 255, 255, isSelected ? 80 : 130),
                    0, y + h * 0.5f, new Color(255, 255, 255, 0)));
            g2.fill(new Rectangle2D.Float(x + 3, y + 2, w - 6, h * 0.5f));
            g2.setColor(new Color(255, 255, 255, isSelected ? 190 : 150));
            g2.setStroke(new BasicStroke(1.1f));
            g2.draw(r);
            g2.dispose();
        }

        @Override protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
                                                int x, int y, int w, int h, boolean isSelected) { }

        @Override protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) { }

        @Override protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects,
                                                     int tabIndex, Rectangle iconRect, Rectangle textRect,
                                                     boolean isSelected) { }
    }

    // ───────────────────────── Legacy panels → glass ─────────────────────────

    /**
     * Walks a finished component tree and converts legacy plain panels to glass:
     * LIGHT_BG panels become transparent (so the backdrop shows), white card panels become
     * glass cards (or transparent when already inside glass), scroll panes lose their
     * opaque viewports and tabs get glass tab styling. Safe to call repeatedly.
     */
    public static void applyGlass(Component root) {
        if (root == null) return;
        applyGlass(root, insideGlass(root));
    }

    private static boolean insideGlass(Component c) {
        for (Component p = c; p != null; p = p.getParent()) {
            if (p instanceof JComponent && Boolean.TRUE.equals(((JComponent) p).getClientProperty("fams.glass"))) return true;
        }
        return false;
    }

    private static void applyGlass(Component c, boolean inGlass) {
        boolean childInGlass = inGlass;
        if (c instanceof JComponent && Boolean.TRUE.equals(((JComponent) c).getClientProperty("fams.glass"))) childInGlass = true;

        if (c instanceof JTabbedPane) {
            JTabbedPane tp = (JTabbedPane) c;
            if (!(tp.getUI() instanceof GlassTabbedPaneUI)) tp.setUI(new GlassTabbedPaneUI());
            tp.setOpaque(false);
            tp.setFont(FONT_SUBHEADER);
        } else if (c instanceof JScrollPane) {
            JScrollPane sp = (JScrollPane) c;
            Component view = sp.getViewport().getView();
            boolean content = view instanceof JTable || view instanceof JTextComponent
                    || view instanceof JList || view instanceof JTree;
            if (!content) {
                sp.setOpaque(false);
                sp.getViewport().setOpaque(false);
                sp.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
            } else if (view instanceof JTable) {
                sp.getViewport().setBackground(TABLE_ROW_A);
                sp.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 200)));
            }
        } else if (c instanceof JCheckBox || c instanceof JRadioButton) {
            ((JComponent) c).setOpaque(false);
        } else if (c instanceof JSplitPane) {
            ((JSplitPane) c).setOpaque(false);
            ((JSplitPane) c).setBorder(null);
        } else if (c instanceof JPanel && c.getClass() == JPanel.class) {
            JPanel p = (JPanel) c;
            Color bg = p.getBackground();
            if (p.getUI() instanceof GlassPanelUI) {
                childInGlass = true;
            } else if (LIGHT_BG.equals(bg)) {
                p.setOpaque(false);
            } else if (Color.WHITE.equals(bg) && p.isOpaque()) {
                if (inGlass) {
                    p.setOpaque(false);
                } else {
                    p.setUI(new GlassPanelUI());
                    p.setOpaque(false);
                    p.putClientProperty("fams.glass", Boolean.TRUE);
                    childInGlass = true;
                }
            }
        }

        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) applyGlass(child, childInGlass);
        }
        if (c instanceof JScrollPane) {
            Component view = ((JScrollPane) c).getViewport().getView();
            if (view != null) applyGlass(view, childInGlass);
        }
        if (c instanceof JComponent) ((JComponent) c).repaint();
    }

    // ───────────────────────── Shared builders ─────────────────────────

    public static JPanel createCardPanel() {
        JPanel panel = new GlassPanel(new BorderLayout(), 20, GLASS_FILL_STRONG, true);
        panel.setBorder(new EmptyBorder(19, 19, 21, 19));
        return panel;
    }

    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_HEADER);
        label.setForeground(TEXT_MAIN);
        return label;
    }
}
