package com.fams.model;

/**
 * Base Abstract User Class demonstrating Encapsulation, Abstraction, and Inheritance.
 */
public abstract class User {
    private String userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private String department;
    private Role role;
    private String profilePictureUrl;

    public User(String userId, String username, String password, String fullName, String email, String phone, String department, Role role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.department = department;
        this.role = role;
        this.profilePictureUrl = "default_avatar.png";
    }

    // Abstract methods to demonstrate Polymorphism
    public abstract String getRoleSpecificDescription();
    public abstract boolean canManageAttendance();
    public abstract boolean canManageMedicals();
    public abstract boolean canManageNotices();
    public abstract boolean canManageTimetables();

    // Getters and Setters (Encapsulation)
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getProfilePictureUrl() { return profilePictureUrl; }
    public void setProfilePictureUrl(String profilePictureUrl) { this.profilePictureUrl = profilePictureUrl; }

    @Override
    public String toString() {
        return fullName + " (" + role.getDisplayName() + ")";
    }
}
