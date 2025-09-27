package com.erp.inventory.service;

import com.erp.inventory.dao.ProductDAO;
import com.erp.inventory.dao.SupplierDAO;
import com.erp.inventory.dao.impl.ProductDAOImpl;
import com.erp.inventory.dao.impl.SupplierDAOImpl;
import com.erp.inventory.model.Product;
import com.erp.inventory.model.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Product business logic operations
 */
public class ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    private final ProductDAO productDAO;
    private final SupplierDAO supplierDAO;

    /**
     * Default constructor using DAO implementations
     */
    public ProductService() {
        this.productDAO = new ProductDAOImpl();
        this.supplierDAO = new SupplierDAOImpl();
    }

    /**
     * Constructor for dependency injection (useful for testing)
     */
    public ProductService(ProductDAO productDAO, SupplierDAO supplierDAO) {
        this.productDAO = productDAO;
        this.supplierDAO = supplierDAO;
    }

    /**
     * Create a new product with validation
     */
    public Product createProduct(String productName, String productCode, String category, 
                               String description, BigDecimal unitPrice, Integer stockQuantity, 
                               Integer reorderLevel, Integer supplierId) throws SQLException, ValidationException {
        logger.info("Creating new product: {}", productName);
        
        // Validate input
        validateProductData(productName, productCode, category, unitPrice, stockQuantity, reorderLevel, supplierId);
        
        // Check if product code already exists
        if (productCode != null && !productCode.trim().isEmpty()) {
            Product existingProduct = productDAO.findByProductCode(productCode);
            if (existingProduct != null) {
                throw new ValidationException("Product code already exists: " + productCode);
            }
        }
        
        // Verify supplier exists
        Optional<Supplier> supplier = supplierDAO.findById(supplierId);
        if (!supplier.isPresent()) {
            throw new ValidationException("Supplier not found with ID: " + supplierId);
        }
        
        // Create product
        Product product = new Product(productName, productCode, category, description, 
                                    unitPrice, stockQuantity, reorderLevel, supplierId);
        
        Product savedProduct = productDAO.save(product);
        logger.info("Product created successfully with ID: {}", savedProduct.getProductId());
        
        return savedProduct;
    }

    /**
     * Update an existing product with validation
     */
    public Product updateProduct(Integer productId, String productName, String productCode, 
                               String category, String description, BigDecimal unitPrice, 
                               Integer stockQuantity, Integer reorderLevel, Integer supplierId) 
                               throws SQLException, ValidationException {
        logger.info("Updating product: {}", productId);
        
        // Validate input
        validateProductData(productName, productCode, category, unitPrice, stockQuantity, reorderLevel, supplierId);
        
        // Check if product exists
        Optional<Product> existingProductOpt = productDAO.findById(productId);
        if (!existingProductOpt.isPresent()) {
            throw new ValidationException("Product not found with ID: " + productId);
        }
        
        // Check if product code is unique (if changed)
        if (productCode != null && !productCode.trim().isEmpty()) {
            Product productWithCode = productDAO.findByProductCode(productCode);
            if (productWithCode != null && !productWithCode.getProductId().equals(productId)) {
                throw new ValidationException("Product code already exists: " + productCode);
            }
        }
        
        // Verify supplier exists
        Optional<Supplier> supplier = supplierDAO.findById(supplierId);
        if (!supplier.isPresent()) {
            throw new ValidationException("Supplier not found with ID: " + supplierId);
        }
        
        // Update product
        Product existingProduct = existingProductOpt.get();
        existingProduct.setProductName(productName);
        existingProduct.setProductCode(productCode);
        existingProduct.setCategory(category);
        existingProduct.setDescription(description);
        existingProduct.setUnitPrice(unitPrice);
        existingProduct.setStockQuantity(stockQuantity);
        existingProduct.setReorderLevel(reorderLevel);
        existingProduct.setSupplierId(supplierId);
        
        Product updatedProduct = productDAO.update(existingProduct);
        logger.info("Product updated successfully: {}", productId);
        
        return updatedProduct;
    }

    /**
     * Find product by ID
     */
    public Optional<Product> findProductById(Integer id) throws SQLException {
        logger.debug("Finding product by ID: {}", id);
        return productDAO.findById(id);
    }

    /**
     * Find all products
     */
    public List<Product> findAllProducts() throws SQLException {
        logger.debug("Finding all products");
        return productDAO.findAll();
    }

    /**
     * Search products by name
     */
    public List<Product> searchProductsByName(String name) throws SQLException {
        logger.debug("Searching products by name: {}", name);
        if (name == null || name.trim().isEmpty()) {
            return findAllProducts();
        }
        return productDAO.findByName(name.trim());
    }

    /**
     * Find products by category
     */
    public List<Product> findProductsByCategory(String category) throws SQLException {
        logger.debug("Finding products by category: {}", category);
        return productDAO.findByCategory(category);
    }

    /**
     * Find products by supplier
     */
    public List<Product> findProductsBySupplier(Integer supplierId) throws SQLException {
        logger.debug("Finding products by supplier: {}", supplierId);
        return productDAO.findBySupplier(supplierId);
    }

    /**
     * Find product by product code
     */
    public Product findProductByCode(String productCode) throws SQLException {
        logger.debug("Finding product by code: {}", productCode);
        return productDAO.findByProductCode(productCode);
    }

    /**
     * Get low stock products
     */
    public List<Product> getLowStockProducts() throws SQLException {
        logger.debug("Getting low stock products");
        return productDAO.getLowStockProducts();
    }

    /**
     * Get distinct categories
     */
    public List<String> getDistinctCategories() throws SQLException {
        logger.debug("Getting distinct categories");
        return productDAO.getDistinctCategories();
    }

    /**
     * Update stock quantity
     */
    public boolean updateStockQuantity(Integer productId, Integer newQuantity) throws SQLException, ValidationException {
        logger.info("Updating stock quantity for product {}: {}", productId, newQuantity);
        
        if (newQuantity < 0) {
            throw new ValidationException("Stock quantity cannot be negative");
        }
        
        boolean updated = productDAO.updateStockQuantity(productId, newQuantity);
        if (updated) {
            logger.info("Stock quantity updated successfully for product: {}", productId);
        } else {
            logger.warn("Product not found for stock update: {}", productId);
        }
        
        return updated;
    }

    /**
     * Delete product (soft delete)
     */
    public boolean deleteProduct(Integer productId) throws SQLException, ValidationException {
        logger.info("Deleting product: {}", productId);
        
        // Check if product exists
        Optional<Product> product = productDAO.findById(productId);
        if (!product.isPresent()) {
            throw new ValidationException("Product not found with ID: " + productId);
        }
        
        boolean deleted = productDAO.delete(productId);
        if (deleted) {
            logger.info("Product deleted successfully: {}", productId);
        }
        
        return deleted;
    }

    /**
     * Get product count
     */
    public long getProductCount() throws SQLException {
        return productDAO.count();
    }

    /**
     * Check if product exists
     */
    public boolean productExists(Integer productId) throws SQLException {
        return productDAO.exists(productId);
    }

    /**
     * Get all suppliers for product creation/editing
     */
    public List<Supplier> getAllSuppliers() throws SQLException {
        logger.debug("Getting all suppliers for product operations");
        return supplierDAO.findAll();
    }

    /**
     * Validate product data
     */
    private void validateProductData(String productName, String productCode, String category, 
                                   BigDecimal unitPrice, Integer stockQuantity, Integer reorderLevel, 
                                   Integer supplierId) throws ValidationException {
        if (productName == null || productName.trim().isEmpty()) {
            throw new ValidationException("Product name is required");
        }
        
        if (productName.trim().length() > 100) {
            throw new ValidationException("Product name cannot exceed 100 characters");
        }
        
        if (productCode != null && productCode.length() > 50) {
            throw new ValidationException("Product code cannot exceed 50 characters");
        }
        
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Category is required");
        }
        
        if (category.trim().length() > 50) {
            throw new ValidationException("Category cannot exceed 50 characters");
        }
        
        if (unitPrice == null) {
            throw new ValidationException("Unit price is required");
        }
        
        if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Unit price cannot be negative");
        }
        
        if (stockQuantity == null) {
            throw new ValidationException("Stock quantity is required");
        }
        
        if (stockQuantity < 0) {
            throw new ValidationException("Stock quantity cannot be negative");
        }
        
        if (reorderLevel == null) {
            throw new ValidationException("Reorder level is required");
        }
        
        if (reorderLevel < 0) {
            throw new ValidationException("Reorder level cannot be negative");
        }
        
        if (supplierId == null) {
            throw new ValidationException("Supplier ID is required");
        }
    }

    /**
     * Custom validation exception
     */
    public static class ValidationException extends Exception {
        public ValidationException(String message) {
            super(message);
        }
        
        public ValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}