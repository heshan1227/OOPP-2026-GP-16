package dao;

import model.User;
import java.util.List;

public interface UserDAO {
    User authenticate(String username, String password);
    boolean createUser(User user, String specificRoleDetails);
    User getUserById(int userId);
    User getUserByUsername(String username);
    List<User> getAllUsers();
    boolean updateUser(User user);
    boolean deleteUser(int userId);
    boolean updatePassword(int userId, String newPasswordHash);
    boolean updateProfileDetails(User user);
}