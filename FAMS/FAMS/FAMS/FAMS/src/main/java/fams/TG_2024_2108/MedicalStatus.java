<<<<<<< HEAD:src/com/fams/model/MedicalStatus.java
package com.fams.model;
=======
package fams.TG_2024_2108;
>>>>>>> 7d3243976a0642af534ee34d8a74c88cda0d97fa:FAMS/FAMS/src/main/java/fams/TG_2024_2108/MedicalStatus.java

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
