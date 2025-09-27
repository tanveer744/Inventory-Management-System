package com.erp.inventory.model;

/**
 * Enumeration for different transaction types in the inventory system
 */
public enum TransactionType {
    PURCHASE("Purchase", "Stock increase from supplier"),
    SALE("Sale", "Stock decrease due to sale"),
    RETURN_IN("Return In", "Stock increase due to return from customer"),
    RETURN_OUT("Return Out", "Stock decrease due to return to supplier"),
    ADJUSTMENT("Adjustment", "Stock adjustment for correction");

    private final String displayName;
    private final String description;

    TransactionType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public boolean increasesStock() {
        return this == PURCHASE || this == RETURN_IN;
    }

    public boolean decreasesStock() {
        return this == SALE || this == RETURN_OUT;
    }

    public boolean isAdjustment() {
        return this == ADJUSTMENT;
    }

    @Override
    public String toString() {
        return displayName;
    }
}