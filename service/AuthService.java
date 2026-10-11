package service;

import dao.UserDAO;
import model.User;

public class AuthService {
    private final UserDAO userDAO;
    private static User currentUser;

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public boolean login(String username, String password) {
        User user = userDAO.authenticate(username, password);
        if (user != null) {
            currentUser = user;
            return true;
        }
        return false;
    }

    public void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean hasRole(String role) {
        return currentUser != null && currentUser.getRole().equalsIgnoreCase(role);
    }
}