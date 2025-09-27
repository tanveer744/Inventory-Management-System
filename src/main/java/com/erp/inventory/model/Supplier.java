package com.erp.inventory.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Supplier entity representing suppliers/vendors in the inventory system
 */
public class Supplier extends BaseEntity {
    private Integer supplierId;
    private String companyName;
    private String contactPerson;
    private String phone;
    private String email;
    private String address;
    private BigDecimal rating; // 1.0 to 5.0

    // Default constructor
    public Supplier() {
        super();
    }

    // Constructor with required fields
    public Supplier(String companyName) {
        super();
        this.companyName = companyName;
    }

    // Full constructor
    public Supplier(String companyName, String contactPerson, String phone, 
                   String email, String address, BigDecimal rating) {
        super();
        this.companyName = companyName;
        this.contactPerson = contactPerson;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.rating = rating;
    }

    // Getters and Setters
    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    // Business methods
    public boolean hasContact() {
        return contactPerson != null && !contactPerson.trim().isEmpty();
    }

    public boolean hasEmail() {
        return email != null && !email.trim().isEmpty();
    }

    public boolean hasPhone() {
        return phone != null && !phone.trim().isEmpty();
    }

    public String getDisplayName() {
        if (hasContact()) {
            return companyName + " (" + contactPerson + ")";
        }
        return companyName;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Supplier supplier = (Supplier) o;
        return Objects.equals(supplierId, supplier.supplierId) &&
               Objects.equals(companyName, supplier.companyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(supplierId, companyName);
    }

    @Override
    public String toString() {
        return "Supplier{" +
                "supplierId=" + supplierId +
                ", companyName='" + companyName + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", rating=" + rating +
                ", isActive=" + isActive +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                '}';
    }
}