import dao.UserDAO;
import dao.UserDAOImpl;
import gui.LoginFrame;
import service.AuthService;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set Look and Feel to System Default for a modern desktop look
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            UserDAO userDAO = new UserDAOImpl();
            AuthService authService = new AuthService(userDAO);

            LoginFrame loginFrame = new LoginFrame(authService);
            loginFrame.setVisible(true);
        });
    }
}