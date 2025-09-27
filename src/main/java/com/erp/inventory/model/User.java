package com.erp.inventory.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * User entity for authentication and access control
 */
public class User extends BaseEntity {
    private Integer userId;
    private String username;
    private String password; // Should be hashed
    private String fullName;
    private String email;
    private UserRole role;
    private LocalDateTime lastLogin;

    // Default constructor
    public User() {
        super();
        this.role = UserRole.EMPLOYEE;
    }

    // Constructor with required fields
    public User(String username, String password, String fullName) {
        super();
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = UserRole.EMPLOYEE;
    }

    // Full constructor
    public User(String username, String password, String fullName, 
               String email, UserRole role) {
        super();
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role != null ? role : UserRole.EMPLOYEE;
    }

    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    // Business methods
    public boolean hasEmail() {
        return email != null && !email.trim().isEmpty();
    }

    public String getDisplayName() {
        return fullName != null ? fullName : username;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isManager() {
        return role == UserRole.MANAGER || role == UserRole.ADMIN;
    }

    public boolean canManageUsers() {
        return role == UserRole.ADMIN;
    }

    public boolean canManageInventory() {
        return role == UserRole.ADMIN || role == UserRole.MANAGER;
    }

    public boolean canViewReports() {
        return role == UserRole.ADMIN || role == UserRole.MANAGER;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) &&
               Objects.equals(username, user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", lastLogin=" + lastLogin +
                ", isActive=" + isActive +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                '}';
    }
}