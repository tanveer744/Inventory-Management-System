package com.erp.inventory.dao.impl;

import com.erp.inventory.dao.SupplierDAO;
import com.erp.inventory.model.Supplier;
import com.erp.inventory.util.DatabaseUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of SupplierDAO for database operations
 */
public class SupplierDAOImpl implements SupplierDAO {
    private static final Logger logger = LoggerFactory.getLogger(SupplierDAOImpl.class);
    
    // SQL Queries
    private static final String INSERT_SQL = 
        "INSERT INTO suppliers (company_name, contact_person, phone, email, address, rating) " +
        "VALUES (?, ?, ?, ?, ?, ?)";
    
    private static final String UPDATE_SQL = 
        "UPDATE suppliers SET company_name = ?, contact_person = ?, phone = ?, email = ?, " +
        "address = ?, rating = ?, updated_date = CURRENT_TIMESTAMP WHERE supplier_id = ?";
    
    private static final String FIND_BY_ID_SQL = 
        "SELECT * FROM suppliers WHERE supplier_id = ? AND is_active = TRUE";
    
    private static final String FIND_ALL_SQL = 
        "SELECT * FROM suppliers WHERE is_active = TRUE ORDER BY company_name";
    
    private static final String SOFT_DELETE_SQL = 
        "UPDATE suppliers SET is_active = FALSE, updated_date = CURRENT_TIMESTAMP WHERE supplier_id = ?";
    
    private static final String EXISTS_SQL = 
        "SELECT COUNT(*) FROM suppliers WHERE supplier_id = ? AND is_active = TRUE";
    
    private static final String COUNT_SQL = 
        "SELECT COUNT(*) FROM suppliers WHERE is_active = TRUE";
    
    private static final String FIND_BY_NAME_SQL = 
        "SELECT * FROM suppliers WHERE company_name LIKE ? AND is_active = TRUE ORDER BY company_name";
    
    private static final String FIND_BY_EMAIL_SQL = 
        "SELECT * FROM suppliers WHERE email = ? AND is_active = TRUE";
    
    private static final String FIND_BY_RATING_RANGE_SQL = 
        "SELECT * FROM suppliers WHERE rating BETWEEN ? AND ? AND is_active = TRUE ORDER BY rating DESC";
    
    private static final String GET_TOP_SUPPLIERS_SQL = 
        "SELECT * FROM suppliers WHERE is_active = TRUE ORDER BY rating DESC, company_name LIMIT ?";
    
    private static final String HAS_PRODUCTS_SQL = 
        "SELECT COUNT(*) FROM products WHERE supplier_id = ? AND is_active = TRUE";

    @Override
    public Supplier save(Supplier supplier) throws SQLException {
        logger.debug("Saving new supplier: {}", supplier.getCompanyName());
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            setSupplierParameters(stmt, supplier);
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating supplier failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    supplier.setSupplierId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating supplier failed, no ID obtained.");
                }
            }
            
            logger.info("Supplier saved successfully with ID: {}", supplier.getSupplierId());
            return supplier;
            
        } catch (SQLException e) {
            logger.error("Error saving supplier: {}", supplier.getCompanyName(), e);
            throw e;
        }
    }

    @Override
    public Supplier update(Supplier supplier) throws SQLException {
        logger.debug("Updating supplier: {}", supplier.getSupplierId());
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(UPDATE_SQL)) {
            
            setSupplierParameters(stmt, supplier);
            stmt.setInt(7, supplier.getSupplierId());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Updating supplier failed, no rows affected.");
            }
            
            supplier.setUpdatedDate(LocalDateTime.now());
            logger.info("Supplier updated successfully: {}", supplier.getSupplierId());
            return supplier;
            
        } catch (SQLException e) {
            logger.error("Error updating supplier: {}", supplier.getSupplierId(), e);
            throw e;
        }
    }

    @Override
    public Optional<Supplier> findById(Integer id) throws SQLException {
        logger.debug("Finding supplier by ID: {}", id);
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(FIND_BY_ID_SQL)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Supplier supplier = mapResultSetToSupplier(rs);
                    logger.debug("Found supplier: {}", supplier.getCompanyName());
                    return Optional.of(supplier);
                }
            }
            
            logger.debug("Supplier not found with ID: {}", id);
            return Optional.empty();
            
        } catch (SQLException e) {
            logger.error("Error finding supplier by ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public List<Supplier> findAll() throws SQLException {
        logger.debug("Finding all suppliers");
        
        List<Supplier> suppliers = new ArrayList<>();
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(FIND_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                suppliers.add(mapResultSetToSupplier(rs));
            }
            
            logger.debug("Found {} suppliers", suppliers.size());
            return suppliers;
            
        } catch (SQLException e) {
            logger.error("Error finding all suppliers", e);
            throw e;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        logger.debug("Soft deleting supplier: {}", id);
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(SOFT_DELETE_SQL)) {
            
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            boolean deleted = affectedRows > 0;
            
            if (deleted) {
                logger.info("Supplier soft deleted successfully: {}", id);
            } else {
                logger.warn("Supplier not found for deletion: {}", id);
            }
            
            return deleted;
            
        } catch (SQLException e) {
            logger.error("Error deleting supplier: {}", id, e);
            throw e;
        }
    }

    @Override
    public boolean exists(Integer id) throws SQLException {
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(EXISTS_SQL)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Error checking if supplier exists: {}", id, e);
            throw e;
        }
    }

    @Override
    public long count() throws SQLException {
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(COUNT_SQL);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            
            return 0;
            
        } catch (SQLException e) {
            logger.error("Error counting suppliers", e);
            throw e;
        }
    }

    @Override
    public List<Supplier> findByName(String name) throws SQLException {
        logger.debug("Finding suppliers by name: {}", name);
        
        List<Supplier> suppliers = new ArrayList<>();
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(FIND_BY_NAME_SQL)) {
            
            stmt.setString(1, "%" + name + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    suppliers.add(mapResultSetToSupplier(rs));
                }
            }
            
            logger.debug("Found {} suppliers matching name: {}", suppliers.size(), name);
            return suppliers;
            
        } catch (SQLException e) {
            logger.error("Error finding suppliers by name: {}", name, e);
            throw e;
        }
    }

    @Override
    public Supplier findByEmail(String email) throws SQLException {
        logger.debug("Finding supplier by email: {}", email);
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(FIND_BY_EMAIL_SQL)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Supplier supplier = mapResultSetToSupplier(rs);
                    logger.debug("Found supplier by email: {}", supplier.getCompanyName());
                    return supplier;
                }
            }
            
            logger.debug("No supplier found with email: {}", email);
            return null;
            
        } catch (SQLException e) {
            logger.error("Error finding supplier by email: {}", email, e);
            throw e;
        }
    }

    @Override
    public List<Supplier> findByRatingRange(double minRating, double maxRating) throws SQLException {
        logger.debug("Finding suppliers by rating range: {} - {}", minRating, maxRating);
        
        List<Supplier> suppliers = new ArrayList<>();
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(FIND_BY_RATING_RANGE_SQL)) {
            
            stmt.setDouble(1, minRating);
            stmt.setDouble(2, maxRating);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    suppliers.add(mapResultSetToSupplier(rs));
                }
            }
            
            logger.debug("Found {} suppliers in rating range {} - {}", 
                        suppliers.size(), minRating, maxRating);
            return suppliers;
            
        } catch (SQLException e) {
            logger.error("Error finding suppliers by rating range: {} - {}", 
                        minRating, maxRating, e);
            throw e;
        }
    }

    @Override
    public List<Supplier> getTopSuppliers(int limit) throws SQLException {
        logger.debug("Getting top {} suppliers", limit);
        
        List<Supplier> suppliers = new ArrayList<>();
        
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(GET_TOP_SUPPLIERS_SQL)) {
            
            stmt.setInt(1, limit);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    suppliers.add(mapResultSetToSupplier(rs));
                }
            }
            
            logger.debug("Found {} top suppliers", suppliers.size());
            return suppliers;
            
        } catch (SQLException e) {
            logger.error("Error getting top suppliers", e);
            throw e;
        }
    }

    @Override
    public boolean hasProducts(Integer supplierId) throws SQLException {
        try (Connection connection = DatabaseUtil.getConnection();
             PreparedStatement stmt = connection.prepareStatement(HAS_PRODUCTS_SQL)) {
            
            stmt.setInt(1, supplierId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Error checking if supplier has products: {}", supplierId, e);
            throw e;
        }
    }
    
    // Helper methods
    private void setSupplierParameters(PreparedStatement stmt, Supplier supplier) throws SQLException {
        stmt.setString(1, supplier.getCompanyName());
        stmt.setString(2, supplier.getContactPerson());
        stmt.setString(3, supplier.getPhone());
        stmt.setString(4, supplier.getEmail());
        stmt.setString(5, supplier.getAddress());
        
        if (supplier.getRating() != null) {
            stmt.setBigDecimal(6, supplier.getRating());
        } else {
            stmt.setNull(6, Types.DECIMAL);
        }
    }
    
    private Supplier mapResultSetToSupplier(ResultSet rs) throws SQLException {
        Supplier supplier = new Supplier();
        
        supplier.setSupplierId(rs.getInt("supplier_id"));
        supplier.setCompanyName(rs.getString("company_name"));
        supplier.setContactPerson(rs.getString("contact_person"));
        supplier.setPhone(rs.getString("phone"));
        supplier.setEmail(rs.getString("email"));
        supplier.setAddress(rs.getString("address"));
        
        BigDecimal rating = rs.getBigDecimal("rating");
        supplier.setRating(rating);
        
        supplier.setActive(rs.getBoolean("is_active"));
        
        Timestamp createdDate = rs.getTimestamp("created_date");
        if (createdDate != null) {
            supplier.setCreatedDate(createdDate.toLocalDateTime());
        }
        
        Timestamp updatedDate = rs.getTimestamp("updated_date");
        if (updatedDate != null) {
            supplier.setUpdatedDate(updatedDate.toLocalDateTime());
        }
        
        return supplier;
    }
}