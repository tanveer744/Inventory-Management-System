package com.erp.inventory.dao;

import com.erp.inventory.model.Product;

import java.sql.SQLException;
import java.util.List;

/**
 * DAO interface for Product entity operations
 */
public interface ProductDAO extends BaseDAO<Product, Integer> {
    
    /**
     * Find products by name (partial match)
     * @param name Product name or partial name
     * @return List of matching products
     * @throws SQLException if database operation fails
     */
    List<Product> findByName(String name) throws SQLException;
    
    /**
     * Find products by category
     * @param category Product category
     * @return List of products in category
     * @throws SQLException if database operation fails
     */
    List<Product> findByCategory(String category) throws SQLException;
    
    /**
     * Find products by supplier
     * @param supplierId Supplier ID
     * @return List of products from supplier
     * @throws SQLException if database operation fails
     */
    List<Product> findBySupplier(Integer supplierId) throws SQLException;
    
    /**
     * Find product by product code
     * @param productCode Product code
     * @return Product if found, null otherwise
     * @throws SQLException if database operation fails
     */
    Product findByProductCode(String productCode) throws SQLException;
    
    /**
     * Get low stock products
     * @return List of products with stock below or equal to reorder level
     * @throws SQLException if database operation fails
     */
    List<Product> getLowStockProducts() throws SQLException;
    
    /**
     * Get out of stock products
     * @return List of products with zero stock
     * @throws SQLException if database operation fails
     */
    List<Product> getOutOfStockProducts() throws SQLException;
    
    /**
     * Get products with stock summary (including supplier info)
     * @return List of products with complete stock information
     * @throws SQLException if database operation fails
     */
    List<Product> getStockSummary() throws SQLException;
    
    /**
     * Get all categories
     * @return List of distinct categories
     * @throws SQLException if database operation fails
     */
    List<String> getAllCategories() throws SQLException;
    
    /**
     * Update stock quantity directly
     * @param productId Product ID
     * @param newQuantity New stock quantity
     * @return true if updated successfully, false otherwise
     * @throws SQLException if database operation fails
     */
    boolean updateStockQuantity(Integer productId, Integer newQuantity) throws SQLException;
}