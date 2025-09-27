package com.erp.inventory.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Transaction entity representing inventory transactions
 */
public class Transaction {
    private Integer transactionId;
    private TransactionType transactionType;
    private Integer productId;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalAmount; // Calculated field (quantity * unitPrice)
    private LocalDateTime transactionDate;
    private Integer createdBy;
    private String referenceNumber;
    private String notes;
    
    // Additional fields for display purposes (not stored in database)
    private transient String productName;
    private transient String productCode;
    private transient String category;
    private transient String supplierName;
    private transient String createdByName;

    // Default constructor
    public Transaction() {
        this.transactionDate = LocalDateTime.now();
    }

    // Constructor with required fields
    public Transaction(TransactionType transactionType, Integer productId, 
                      Integer quantity, BigDecimal unitPrice) {
        this();
        this.transactionType = transactionType;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = calculateTotalAmount();
    }

    // Full constructor
    public Transaction(TransactionType transactionType, Integer productId, Integer quantity,
                      BigDecimal unitPrice, Integer createdBy, String referenceNumber, String notes) {
        this();
        this.transactionType = transactionType;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.createdBy = createdBy;
        this.referenceNumber = referenceNumber;
        this.notes = notes;
        this.totalAmount = calculateTotalAmount();
    }

    // Getters and Setters
    public Integer getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        this.totalAmount = calculateTotalAmount();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        this.totalAmount = calculateTotalAmount();
    }

    public BigDecimal getTotalAmount() {
        if (totalAmount == null) {
            totalAmount = calculateTotalAmount();
        }
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public Integer getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Integer createdBy) {
        this.createdBy = createdBy;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    // Business methods
    private BigDecimal calculateTotalAmount() {
        if (quantity != null && unitPrice != null) {
            return unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
        return BigDecimal.ZERO;
    }

    public String getDisplayProductName() {
        if (productCode != null && !productCode.trim().isEmpty()) {
            return (productName != null ? productName : "Unknown Product") + " (" + productCode + ")";
        }
        return productName != null ? productName : "Unknown Product";
    }

    public String getFormattedReferenceNumber() {
        if (referenceNumber != null && !referenceNumber.trim().isEmpty()) {
            return referenceNumber;
        }
        return "TXN-" + String.format("%06d", transactionId != null ? transactionId : 0);
    }

    public boolean hasNotes() {
        return notes != null && !notes.trim().isEmpty();
    }

    public String getDisplayNotes() {
        return hasNotes() ? notes : "No notes";
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(transactionId, that.transactionId) &&
               transactionType == that.transactionType &&
               Objects.equals(productId, that.productId) &&
               Objects.equals(transactionDate, that.transactionDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionId, transactionType, productId, transactionDate);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", transactionType=" + transactionType +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", totalAmount=" + totalAmount +
                ", transactionDate=" + transactionDate +
                ", createdBy=" + createdBy +
                ", referenceNumber='" + referenceNumber + '\'' +
                ", notes='" + notes + '\'' +
                ", productName='" + productName + '\'' +
                '}';
    }
}