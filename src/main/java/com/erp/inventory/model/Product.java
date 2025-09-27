package com.erp.inventory.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Product entity representing products in the inventory system
 */
public class Product extends BaseEntity {
    private Integer productId;
    private String productName;
    private String productCode;
    private String category;
    private String description;
    private BigDecimal unitPrice;
    private Integer stockQuantity;
    private Integer reorderLevel;
    private Integer supplierId;
    
    // Additional fields for display purposes (not stored in database)
    private transient String supplierName;
    private transient BigDecimal supplierRating;

    // Default constructor
    public Product() {
        super();
        this.stockQuantity = 0;
        this.reorderLevel = 10;
    }

    // Constructor with required fields
    public Product(String productName, String category, BigDecimal unitPrice, Integer supplierId) {
        super();
        this.productName = productName;
        this.category = category;
        this.unitPrice = unitPrice;
        this.supplierId = supplierId;
        this.stockQuantity = 0;
        this.reorderLevel = 10;
    }

    // Full constructor
    public Product(String productName, String productCode, String category, String description,
                   BigDecimal unitPrice, Integer stockQuantity, Integer reorderLevel, Integer supplierId) {
        super();
        this.productName = productName;
        this.productCode = productCode;
        this.category = category;
        this.description = description;
        this.unitPrice = unitPrice;
        this.stockQuantity = stockQuantity != null ? stockQuantity : 0;
        this.reorderLevel = reorderLevel != null ? reorderLevel : 10;
        this.supplierId = supplierId;
    }

    // Getters and Setters
    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public Integer getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(Integer reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public Integer getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public BigDecimal getSupplierRating() {
        return supplierRating;
    }

    public void setSupplierRating(BigDecimal supplierRating) {
        this.supplierRating = supplierRating;
    }

    // Business methods
    public BigDecimal getStockValue() {
        if (stockQuantity != null && unitPrice != null) {
            return unitPrice.multiply(BigDecimal.valueOf(stockQuantity));
        }
        return BigDecimal.ZERO;
    }

    public boolean isLowStock() {
        return stockQuantity != null && reorderLevel != null && 
               stockQuantity <= reorderLevel;
    }

    public boolean isOutOfStock() {
        return stockQuantity == null || stockQuantity == 0;
    }

    public String getStockStatus() {
        if (isOutOfStock()) {
            return "OUT_OF_STOCK";
        } else if (isLowStock()) {
            return "LOW_STOCK";
        }
        return "NORMAL";
    }

    public Integer getShortage() {
        if (reorderLevel != null && stockQuantity != null) {
            int shortage = reorderLevel - stockQuantity;
            return shortage > 0 ? shortage : 0;
        }
        return 0;
    }

    public boolean hasSufficientStock(int requestedQuantity) {
        return stockQuantity != null && stockQuantity >= requestedQuantity;
    }

    public String getDisplayName() {
        if (productCode != null && !productCode.trim().isEmpty()) {
            return productName + " (" + productCode + ")";
        }
        return productName;
    }

    // Stock manipulation methods (should be used carefully)
    public void increaseStock(int quantity) {
        if (quantity > 0) {
            this.stockQuantity = (this.stockQuantity != null ? this.stockQuantity : 0) + quantity;
        }
    }

    public boolean decreaseStock(int quantity) {
        if (quantity > 0 && hasSufficientStock(quantity)) {
            this.stockQuantity -= quantity;
            return true;
        }
        return false;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(productId, product.productId) &&
               Objects.equals(productName, product.productName) &&
               Objects.equals(productCode, product.productCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, productName, productCode);
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", productCode='" + productCode + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", unitPrice=" + unitPrice +
                ", stockQuantity=" + stockQuantity +
                ", reorderLevel=" + reorderLevel +
                ", supplierId=" + supplierId +
                ", supplierName='" + supplierName + '\'' +
                ", isActive=" + isActive +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                '}';
    }
}