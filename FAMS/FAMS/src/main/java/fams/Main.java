package fams;

import fams.TG_2024_2133.DatabaseInitializer;
import fams.TG_2024_2133.LoginFrame;
import fams.TG_2024_2133.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set Look and Feel to System Default for native look
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        fams.TG_2024_2133.Theme.installButtonDefaults();

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
