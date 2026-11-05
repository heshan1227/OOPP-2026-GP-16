package com.fams.model;

public enum ComponentType {
    THEORY("Theory", "Theory session (1 Credit = 2 Hours)"),
    PRACTICAL("Practical", "Practical session (1 Credit = 2 Hours)"),
    COMBINED("Combined", "Combined Theory & Practical view");

    private final String displayName;
    private final String description;

    ComponentType(String displayName, String description) {
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
