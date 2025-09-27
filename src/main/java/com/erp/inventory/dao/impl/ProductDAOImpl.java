package com.erp.inventory.dao.impl;

import com.erp.inventory.dao.ProductDAO;
import com.erp.inventory.model.Product;
import com.erp.inventory.util.DatabaseUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ProductDAO for database operations
 */
public class ProductDAOImpl implements ProductDAO {
    private static final Logger logger = LoggerFactory.getLogger(ProductDAOImpl.class);
    
    // SQL Queries
    private static final String INSERT_SQL = 
        "INSERT INTO products (product_name, product_code, category, description, unit_price, " +
        "stock_quantity, reorder_level, supplier_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
        "UPDATE products SET product_name = ?, product_code = ?, category = ?, description = ?, " +
        "unit_price = ?, stock_quantity = ?, reorder_level = ?, supplier_id = ?, " +
        "updated_date = CURRENT_TIMESTAMP WHERE product_id = ?";
    
    private static final String FIND_BY_ID_SQL = 
        "SELECT p.*, s.company_name as supplier_name, s.rating as supplier_rating " +
        "FROM products p LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
        "WHERE p.product_id = ? AND p.is_active = TRUE";
    
    private static final String FIND_ALL_SQL = 
        "SELECT p.*, s.company_name as supplier_name, s.rating as supplier_rating " +
        "FROM products p LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
        "WHERE p.is_active = TRUE ORDER BY p.product_name";
    
    private static final String SOFT_DELETE_SQL = 
        "UPDATE products SET is_active = FALSE, updated_date = CURRENT_TIMESTAMP WHERE product_id = ?";
    
    private static final String EXISTS_SQL = 
        "SELECT COUNT(*) FROM products WHERE product_id = ? AND is_active = TRUE";
    
    private static final String COUNT_SQL = 
        "SELECT COUNT(*) FROM products WHERE is_active = TRUE";
    
    private static final String FIND_BY_NAME_SQL = 
        "SELECT p.*, s.company_name as supplier_name, s.rating as supplier_rating " +
        "FROM products p LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
        "WHERE p.product_name LIKE ? AND p.is_active = TRUE ORDER BY p.product_name";
    
    private static final String FIND_BY_CATEGORY_SQL = 
        "SELECT p.*, s.company_name as supplier_name, s.rating as supplier_rating " +
        "FROM products p LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
        "WHERE p.category = ? AND p.is_active = TRUE ORDER BY p.product_name";
    
    private static final String FIND_BY_SUPPLIER_SQL = 
        "SELECT p.*, s.company_name as supplier_name, s.rating as supplier_rating " +
        "FROM products p LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
        "WHERE p.supplier_id = ? AND p.is_active = TRUE ORDER BY p.product_name";
    
    private static final String FIND_BY_PRODUCT_CODE_SQL = 
        "SELECT p.*, s.company_name as supplier_name, s.rating as supplier_rating " +
        "FROM products p LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
        "WHERE p.product_code = ? AND p.is_active = TRUE";
    
    private static final String LOW_STOCK_SQL = 
        "SELECT p.*, s.company_name as supplier_name, s.rating as supplier_rating " +
        "FROM products p LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
        "WHERE p.stock_quantity <= p.reorder_level AND p.is_active = TRUE " +
        "ORDER BY (p.reorder_level - p.stock_quantity) DESC";
    
    private static final String GET_CATEGORIES_SQL = 
        "SELECT DISTINCT category FROM products WHERE is_active = TRUE ORDER BY category";
    
    private static final String UPDATE_STOCK_SQL = 
        "UPDATE products SET stock_quantity = ?, updated_date = CURRENT_TIMESTAMP WHERE product_id = ?";

    @Override
    public Product save(Product product) throws SQLException {
        logger.debug("Saving new product: {}", product.getProductName());
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            setProductParameters(stmt, product);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating product failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setProductId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating product failed, no ID obtained.");
                }
            }
            
            logger.info("Product saved successfully with ID: {}", product.getProductId());
            return product;
            
        } catch (SQLException e) {
            logger.error("Error saving product: {}", product.getProductName(), e);
            throw e;
        }
    }

    @Override
    public Product update(Product product) throws SQLException {
        logger.debug("Updating product: {}", product.getProductId());
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(UPDATE_SQL)) {
            
            setProductParameters(stmt, product);
            stmt.setInt(9, product.getProductId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating product failed, product not found: " + product.getProductId());
            }
            
            logger.info("Product updated successfully: {}", product.getProductId());
            return product;
            
        } catch (SQLException e) {
            logger.error("Error updating product: {}", product.getProductId(), e);
            throw e;
        }
    }

    @Override
    public Optional<Product> findById(Integer id) throws SQLException {
        logger.debug("Finding product by ID: {}", id);
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID_SQL)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product product = mapResultSetToProduct(rs);
                    logger.debug("Product found: {}", product.getProductName());
                    return Optional.of(product);
                }
            }
            
            logger.debug("No product found with ID: {}", id);
            return Optional.empty();
            
        } catch (SQLException e) {
            logger.error("Error finding product by ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public List<Product> findAll() throws SQLException {
        logger.debug("Finding all products");
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            List<Product> products = new ArrayList<>();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
            
            logger.info("Found {} products", products.size());
            return products;
            
        } catch (SQLException e) {
            logger.error("Error finding all products", e);
            throw e;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        logger.debug("Soft deleting product: {}", id);
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SOFT_DELETE_SQL)) {
            
            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            
            boolean deleted = affectedRows > 0;
            if (deleted) {
                logger.info("Product soft deleted successfully: {}", id);
            } else {
                logger.warn("No product found to delete with ID: {}", id);
            }
            
            return deleted;
            
        } catch (SQLException e) {
            logger.error("Error deleting product: {}", id, e);
            throw e;
        }
    }

    @Override
    public boolean deleteById(Integer id) throws SQLException {
        return delete(id);
    }

    @Override
    public boolean exists(Integer id) throws SQLException {
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(EXISTS_SQL)) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public boolean existsById(Integer id) throws SQLException {
        return exists(id);
    }

    @Override
    public long count() throws SQLException {
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(COUNT_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    @Override
    public List<Product> findByName(String name) throws SQLException {
        logger.debug("Finding products by name: {}", name);
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(FIND_BY_NAME_SQL)) {
            
            stmt.setString(1, "%" + name + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
                
                logger.info("Found {} products matching name: {}", products.size(), name);
                return products;
            }
            
        } catch (SQLException e) {
            logger.error("Error finding products by name: {}", name, e);
            throw e;
        }
    }

    @Override
    public List<Product> findByCategory(String category) throws SQLException {
        logger.debug("Finding products by category: {}", category);
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(FIND_BY_CATEGORY_SQL)) {
            
            stmt.setString(1, category);
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
                
                logger.info("Found {} products in category: {}", products.size(), category);
                return products;
            }
            
        } catch (SQLException e) {
            logger.error("Error finding products by category: {}", category, e);
            throw e;
        }
    }

    @Override
    public List<Product> findBySupplier(Integer supplierId) throws SQLException {
        logger.debug("Finding products by supplier: {}", supplierId);
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(FIND_BY_SUPPLIER_SQL)) {
            
            stmt.setInt(1, supplierId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                List<Product> products = new ArrayList<>();
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
                
                logger.info("Found {} products for supplier: {}", products.size(), supplierId);
                return products;
            }
            
        } catch (SQLException e) {
            logger.error("Error finding products by supplier: {}", supplierId, e);
            throw e;
        }
    }

    @Override
    public Product findByProductCode(String productCode) throws SQLException {
        logger.debug("Finding product by code: {}", productCode);
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(FIND_BY_PRODUCT_CODE_SQL)) {
            
            stmt.setString(1, productCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Product product = mapResultSetToProduct(rs);
                    logger.debug("Product found by code: {}", product.getProductName());
                    return product;
                }
            }
            
            logger.debug("No product found with code: {}", productCode);
            return null;
            
        } catch (SQLException e) {
            logger.error("Error finding product by code: {}", productCode, e);
            throw e;
        }
    }

    @Override
    public List<Product> getLowStockProducts() throws SQLException {
        logger.debug("Finding low stock products");
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(LOW_STOCK_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            List<Product> products = new ArrayList<>();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
            
            logger.info("Found {} low stock products", products.size());
            return products;
            
        } catch (SQLException e) {
            logger.error("Error finding low stock products", e);
            throw e;
        }
    }

    @Override
    public List<String> getAllCategories() throws SQLException {
        return getDistinctCategories();
    }

    @Override
    public List<String> getDistinctCategories() throws SQLException {
        logger.debug("Getting distinct categories");
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(GET_CATEGORIES_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            List<String> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(rs.getString("category"));
            }
            
            logger.info("Found {} distinct categories", categories.size());
            return categories;
            
        } catch (SQLException e) {
            logger.error("Error getting distinct categories", e);
            throw e;
        }
    }

    @Override
    public List<Product> getOutOfStockProducts() throws SQLException {
        logger.debug("Finding out of stock products");
        
        String OUT_OF_STOCK_SQL = 
            "SELECT p.*, s.company_name as supplier_name, s.rating as supplier_rating " +
            "FROM products p LEFT JOIN suppliers s ON p.supplier_id = s.supplier_id " +
            "WHERE p.stock_quantity = 0 AND p.is_active = TRUE " +
            "ORDER BY p.product_name";
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(OUT_OF_STOCK_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            List<Product> products = new ArrayList<>();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
            
            logger.info("Found {} out of stock products", products.size());
            return products;
            
        } catch (SQLException e) {
            logger.error("Error finding out of stock products", e);
            throw e;
        }
    }

    @Override
    public List<Product> getStockSummary() throws SQLException {
        logger.debug("Getting stock summary");
        return findAll(); // Stock summary is essentially all products with supplier info
    }

    @Override
    public boolean updateStockQuantity(Integer productId, Integer newQuantity) throws SQLException {
        logger.debug("Updating stock quantity for product {}: {}", productId, newQuantity);
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(UPDATE_STOCK_SQL)) {
            
            stmt.setInt(1, newQuantity);
            stmt.setInt(2, productId);
            
            int affectedRows = stmt.executeUpdate();
            boolean updated = affectedRows > 0;
            
            if (updated) {
                logger.info("Stock quantity updated for product {}: {}", productId, newQuantity);
            } else {
                logger.warn("No product found to update stock for ID: {}", productId);
            }
            
            return updated;
            
        } catch (SQLException e) {
            logger.error("Error updating stock quantity for product: {}", productId, e);
            throw e;
        }
    }

    /**
     * Helper method to set product parameters in PreparedStatement
     */
    private void setProductParameters(PreparedStatement stmt, Product product) throws SQLException {
        stmt.setString(1, product.getProductName());
        stmt.setString(2, product.getProductCode());
        stmt.setString(3, product.getCategory());
        stmt.setString(4, product.getDescription());
        stmt.setBigDecimal(5, product.getUnitPrice());
        stmt.setInt(6, product.getStockQuantity());
        stmt.setInt(7, product.getReorderLevel());
        stmt.setInt(8, product.getSupplierId());
    }

    /**
     * Helper method to map ResultSet to Product entity
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        
        product.setProductId(rs.getInt("product_id"));
        product.setProductName(rs.getString("product_name"));
        product.setProductCode(rs.getString("product_code"));
        product.setCategory(rs.getString("category"));
        product.setDescription(rs.getString("description"));
        product.setUnitPrice(rs.getBigDecimal("unit_price"));
        product.setStockQuantity(rs.getInt("stock_quantity"));
        product.setReorderLevel(rs.getInt("reorder_level"));
        product.setSupplierId(rs.getInt("supplier_id"));
        
        // Set transient fields if available
        product.setSupplierName(rs.getString("supplier_name"));
        BigDecimal supplierRating = rs.getBigDecimal("supplier_rating");
        if (supplierRating != null) {
            product.setSupplierRating(supplierRating);
        }
        
        // Set base entity fields
        product.setActive(rs.getBoolean("is_active"));
        
        Timestamp createdDate = rs.getTimestamp("created_date");
        if (createdDate != null) {
            product.setCreatedDate(createdDate.toLocalDateTime());
        }
        
        Timestamp updatedDate = rs.getTimestamp("updated_date");
        if (updatedDate != null) {
            product.setUpdatedDate(updatedDate.toLocalDateTime());
        }
        
        return product;
    }
}