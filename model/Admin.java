package model;

public class Admin extends User {
    private int adminId;
    private String department;
    private String contactNo;

    public Admin() {
        super();
    }

    public Admin(int userId, String username, String passwordHash, String name, String email,
                 int adminId, String department, String contactNo) {
        super(userId, username, passwordHash, name, email, "ADMIN");
        this.adminId = adminId;
        this.department = department;
        this.contactNo = contactNo;
    }

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    @Override
    public boolean updateProfile(String name, String email, String contactNo) {
        this.setName(name);
        this.setEmail(email);
        this.setContactNo(contactNo);
        return true;
    }

    @Override
    public String getDashboardTitle() {
        return "System Administrator Control Panel";
    }
}