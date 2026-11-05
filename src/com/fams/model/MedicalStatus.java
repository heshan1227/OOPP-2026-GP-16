package com.fams.model;

public enum MedicalStatus {
    PENDING("Pending", "Awaiting Technical Officer / Department Approval"),
    APPROVED("Approved", "Medical certificate verified and approved"),
    REJECTED("Rejected", "Medical certificate invalid or rejected");

    private final String displayName;
    private final String description;

    MedicalStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
