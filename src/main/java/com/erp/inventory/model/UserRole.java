package com.erp.inventory.model;

/**
 * Enumeration for user roles in the system
 */
public enum UserRole {
    ADMIN("Administrator", "Full system access and user management"),
    MANAGER("Manager", "Inventory management and reporting access"),
    EMPLOYEE("Employee", "Basic inventory operations");

    private final String displayName;
    private final String description;

    UserRole(String displayName, String description) {
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