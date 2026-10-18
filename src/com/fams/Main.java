package com.fams;

import com.fams.database.DatabaseInitializer;
import com.fams.gui.LoginFrame;
import com.fams.gui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set Look and Feel to System Default for native look
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Pre-initialize dataset and fallbacks
        DatabaseInitializer.initializeSampleData();

        // Launch Login UI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(() -> {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            });
            loginFrame.setVisible(true);
        });
    }
}
