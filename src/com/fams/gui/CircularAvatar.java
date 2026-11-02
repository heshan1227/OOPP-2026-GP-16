package com.fams.gui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Reusable anti-aliased circular profile avatar component.
 * Displays user photo clipped to a circle, or a stylized initials avatar if no photo is available.
 */
public class CircularAvatar extends JComponent {
    private final int diameter;
    private BufferedImage image;
    private String fallbackInitials = "U";
    private Color borderColor = Theme.PRIMARY;
    private float borderWidth = 2.5f;

    public CircularAvatar(int diameter, String imagePath, String fallbackInitials) {
        this.diameter = diameter;
        setFallbackInitials(fallbackInitials);
        setImagePath(imagePath);

        Dimension d = new Dimension(diameter, diameter);
        setPreferredSize(d);
        setMinimumSize(d);
        setMaximumSize(d);
        setOpaque(false);
    }

    public void setBorderColor(Color color) {
        this.borderColor = color;
        repaint();
    }

    public void setBorderWidth(float width) {
        this.borderWidth = width;
        repaint();
    }

    public void setFallbackInitials(String nameOrInitials) {
        if (nameOrInitials == null || nameOrInitials.trim().isEmpty()) {
            this.fallbackInitials = "U";
            return;
        }
        String trimmed = nameOrInitials.trim();
        String[] parts = trimmed.split("\\s+");
        if (parts.length >= 2) {
            this.fallbackInitials = ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
        } else if (trimmed.length() >= 2) {
            this.fallbackInitials = trimmed.substring(0, 2).toUpperCase();
        } else {
            this.fallbackInitials = trimmed.toUpperCase();
        }
        repaint();
    }

    public void setImagePath(String path) {
        this.image = null;
        if (path != null && !path.trim().isEmpty() && !path.trim().equalsIgnoreCase("default_avatar.png")) {
            try {
                File f = new File(path.trim());
                if (f.exists() && f.isFile()) {
                    BufferedImage raw = ImageIO.read(f);
                    if (raw != null) {
                        this.image = raw;
                    }
                } else {
                    // Try classpath resource
                    var url = getClass().getResource(path.trim().startsWith("/") ? path.trim() : "/" + path.trim());
                    if (url != null) {
                        this.image = ImageIO.read(url);
                    }
                }
            } catch (Exception ignored) {
                this.image = null;
            }
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        int size = Math.min(getWidth(), getHeight());
        int strokePad = (int) Math.ceil(borderWidth);
        int d = size - (strokePad * 2);
        if (d <= 0) d = size;
        int x = (getWidth() - d) / 2;
        int y = (getHeight() - d) / 2;

        Shape circle = new Ellipse2D.Float(x, y, d, d);

        if (image != null) {
            // Fill background in case image has transparency
            g2.setColor(Color.WHITE);
            g2.fill(circle);

            // Clip image into circle
            g2.setClip(circle);

            // Scale to fill circle preserving aspect ratio
            int imgW = image.getWidth();
            int imgH = image.getHeight();
            double scale = Math.max((double) d / imgW, (double) d / imgH);
            int drawW = (int) Math.round(imgW * scale);
            int drawH = (int) Math.round(imgH * scale);
            int drawX = x + (d - drawW) / 2;
            int drawY = y + (d - drawH) / 2;

            g2.drawImage(image, drawX, drawY, drawW, drawH, null);
            g2.setClip(null);
        } else {
            // Elegant gradient with initials
            GradientPaint gp = new GradientPaint(x, y, new Color(59, 130, 246), x + d, y + d, new Color(30, 64, 175));
            g2.setPaint(gp);
            g2.fill(circle);

            g2.setColor(Color.WHITE);
            int fontSize = Math.max(12, d / 2);
            g2.setFont(new Font("Segoe UI", Font.BOLD, fontSize));
            FontMetrics fm = g2.getFontMetrics();
            int textX = x + (d - fm.stringWidth(fallbackInitials)) / 2;
            int textY = y + ((d - fm.getHeight()) / 2) + fm.getAscent();
            g2.drawString(fallbackInitials, textX, textY);
        }

        // Draw border
        if (borderWidth > 0 && borderColor != null) {
            g2.setColor(borderColor);
            g2.setStroke(new BasicStroke(borderWidth));
            g2.drawOval(x, y, d, d);
        }

        g2.dispose();
    }
}
