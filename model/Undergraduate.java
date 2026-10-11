package model;

public class Undergraduate extends User {
    private int undergraduateId;
    private String regNumber;
    private int batchId;
    private String contactNo;
    private String address;
    private String profilePicPath;

    public Undergraduate() {
        super();
    }

    public Undergraduate(int userId, String username, String passwordHash, String name, String email,
                         int undergraduateId, String regNumber, int batchId, String contactNo, String address, String profilePicPath) {
        super(userId, username, passwordHash, name, email, "UNDERGRADUATE");
        this.undergraduateId = undergraduateId;
        this.regNumber = regNumber;
        this.batchId = batchId;
        this.contactNo = contactNo;
        this.address = address;
        this.profilePicPath = profilePicPath;
    }

    public int getUndergraduateId() { return undergraduateId; }
    public void setUndergraduateId(int undergraduateId) { this.undergraduateId = undergraduateId; }

    public String getRegNumber() { return regNumber; }
    public void setRegNumber(String regNumber) { this.regNumber = regNumber; }

    public int getBatchId() { return batchId; }
    public void setBatchId(int batchId) { this.batchId = batchId; }

    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getProfilePicPath() { return profilePicPath; }
    public void setProfilePicPath(String profilePicPath) { this.profilePicPath = profilePicPath; }

    @Override
    public boolean updateProfile(String name, String email, String contactNo) {
        this.setContactNo(contactNo);
        return true;
    }

    @Override
    public String getDashboardTitle() {
        return "Undergraduate Student Portal - " + (regNumber != null ? regNumber : "");
    }
}