package com.erp.inventory.dao;

import com.erp.inventory.model.Supplier;

import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for Supplier entity operations
 */
public interface SupplierDAO extends BaseDAO<Supplier, Integer> {
    
    /**
     * Find suppliers by company name (partial match)
     * @param name Company name or partial name
     * @return List of matching suppliers
     * @throws SQLException if database operation fails
     */
    List<Supplier> findByName(String name) throws SQLException;
    
    /**
     * Find supplier by email
     * @param email Supplier email
     * @return Supplier if found, null otherwise
     * @throws SQLException if database operation fails
     */
    Supplier findByEmail(String email) throws SQLException;
    
    /**
     * Find suppliers by rating range
     * @param minRating Minimum rating (inclusive)
     * @param maxRating Maximum rating (inclusive)
     * @return List of suppliers within rating range
     * @throws SQLException if database operation fails
     */
    List<Supplier> findByRatingRange(double minRating, double maxRating) throws SQLException;
    
    /**
     * Get top suppliers by rating
     * @param limit Number of top suppliers to return
     * @return List of top-rated suppliers
     * @throws SQLException if database operation fails
     */
    List<Supplier> getTopSuppliers(int limit) throws SQLException;
    
    /**
     * Check if supplier has any products
     * @param supplierId Supplier ID
     * @return true if supplier has products, false otherwise
     * @throws SQLException if database operation fails
     */
    boolean hasProducts(Integer supplierId) throws SQLException;
}