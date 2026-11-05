package fams.TG_2024_2133;


public class UserManagement {
    private static UserManagement instance;
    private User currentUser;
    private final UserDAO userDAO;

    private UserManagement() {
        this.userDAO = new UserDAO();
    }

    public static synchronized UserManagement getInstance() {
        if (instance == null) {
            instance = new UserManagement();
        }
        return instance;
    }

    public User login(String username, String password) throws ValidationException {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            throw new ValidationException("Username and Password cannot be empty.");
        }

        User user = userDAO.authenticate(username.trim(), password);
        if (user == null) {
            throw new ValidationException("Invalid username or password. Please try again.");
        }

        this.currentUser = user;
        return currentUser;
    }

    public void logout() {
        this.currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void requireRole(Role... allowedRoles) throws UnauthorizedAccessException {
        if (currentUser == null) {
            throw new UnauthorizedAccessException("Session expired. Please log in first.");
        }

        for (Role role : allowedRoles) {
            if (currentUser.getRole() == role) {
                return;
            }
        }
        throw new UnauthorizedAccessException("Access Denied: Your role (" + currentUser.getRole().getDisplayName() + ") does not have permission for this action.");
    }
}
