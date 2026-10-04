package model;

import java.sql.Timestamp;

public abstract class User {
    protected int userId;
    protected String username;
    protected String passwordHash;
    protected String name;
    protected String email;
    protected String role;
    protected Timestamp createdAt;
    protected Timestamp updatedAt;

    public User() {}

    public User(int userId, String username, String passwordHash, String name, String email, String role) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    public abstract boolean updateProfile(String name, String email, String contactNo);
    public abstract String getDashboardTitle();
}