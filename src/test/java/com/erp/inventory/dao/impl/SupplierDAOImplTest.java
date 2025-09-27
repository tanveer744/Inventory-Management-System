package com.erp.inventory.dao.impl;

import com.erp.inventory.dao.SupplierDAO;
import com.erp.inventory.model.Supplier;
import com.erp.inventory.util.DatabaseUtil;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SupplierDAOImpl
 * Uses H2 in-memory database for testing
 */
@ExtendWith(MockitoExtension.class)
class SupplierDAOImplTest {
    
    private SupplierDAO supplierDAO;
    
    @BeforeAll
    static void setupDatabase() {
        // Configure H2 in-memory database for testing
        DatabaseUtil.setTestConfiguration(
            "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
            "sa",
            ""
        );
        
        // Create tables for testing
        try (Connection connection = DatabaseUtil.getConnection();
             Statement stmt = connection.createStatement()) {
            
            // Create suppliers table
            stmt.execute("CREATE TABLE suppliers (" +
                        "supplier_id INT AUTO_INCREMENT PRIMARY KEY," +
                        "company_name VARCHAR(100) NOT NULL," +
                        "contact_person VARCHAR(100)," +
                        "phone VARCHAR(20)," +
                        "email VARCHAR(100) UNIQUE," +
                        "address TEXT," +
                        "rating DECIMAL(2,1) CHECK (rating >= 1.0 AND rating <= 5.0)," +
                        "is_active BOOLEAN DEFAULT TRUE," +
                        "created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                        "updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            // Create products table for relationship testing
            stmt.execute("CREATE TABLE products (" +
                        "product_id INT AUTO_INCREMENT PRIMARY KEY," +
                        "product_name VARCHAR(100) NOT NULL," +
                        "supplier_id INT NOT NULL," +
                        "is_active BOOLEAN DEFAULT TRUE," +
                        "FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id))");
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set up test database", e);
        }
    }
    
    @BeforeEach
    void setUp() {
        supplierDAO = new SupplierDAOImpl();
        
        // Clean up tables before each test
        try (Connection connection = DatabaseUtil.getConnection();
             Statement stmt = connection.createStatement()) {
            
            stmt.execute("DELETE FROM products");
            stmt.execute("DELETE FROM suppliers");
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clean up test database", e);
        }
    }
    
    @Test
    @DisplayName("Should save supplier successfully")
    void testSaveSupplier() throws SQLException {
        // Given
        Supplier supplier = new Supplier("Test Company", "John Doe", 
                                        "+1-555-0123", "john@test.com", 
                                        "123 Test St", new BigDecimal("4.5"));
        
        // When
        Supplier savedSupplier = supplierDAO.save(supplier);
        
        // Then
        assertNotNull(savedSupplier);
        assertNotNull(savedSupplier.getSupplierId());
        assertEquals("Test Company", savedSupplier.getCompanyName());
        assertEquals("John Doe", savedSupplier.getContactPerson());
        assertEquals("+1-555-0123", savedSupplier.getPhone());
        assertEquals("john@test.com", savedSupplier.getEmail());
        assertEquals(new BigDecimal("4.5"), savedSupplier.getRating());
        assertTrue(savedSupplier.isActive());
    }
    
    @Test
    @DisplayName("Should find supplier by ID")
    void testFindSupplierById() throws SQLException {
        // Given
        Supplier supplier = new Supplier("Test Company", "John Doe", 
                                        "+1-555-0123", "john@test.com", 
                                        "123 Test St", new BigDecimal("4.5"));
        Supplier savedSupplier = supplierDAO.save(supplier);
        
        // When
        Optional<Supplier> foundSupplier = supplierDAO.findById(savedSupplier.getSupplierId());
        
        // Then
        assertTrue(foundSupplier.isPresent());
        assertEquals(savedSupplier.getSupplierId(), foundSupplier.get().getSupplierId());
        assertEquals("Test Company", foundSupplier.get().getCompanyName());
    }
    
    @Test
    @DisplayName("Should return empty when supplier not found by ID")
    void testFindSupplierByIdNotFound() throws SQLException {
        // When
        Optional<Supplier> foundSupplier = supplierDAO.findById(999);
        
        // Then
        assertFalse(foundSupplier.isPresent());
    }
    
    @Test
    @DisplayName("Should find all active suppliers")
    void testFindAllSuppliers() throws SQLException {
        // Given
        supplierDAO.save(new Supplier("Company A", "Person A", null, "a@test.com", null, new BigDecimal("4.0")));
        supplierDAO.save(new Supplier("Company B", "Person B", null, "b@test.com", null, new BigDecimal("3.5")));
        supplierDAO.save(new Supplier("Company C", "Person C", null, "c@test.com", null, new BigDecimal("4.8")));
        
        // When
        List<Supplier> suppliers = supplierDAO.findAll();
        
        // Then
        assertEquals(3, suppliers.size());
        assertEquals("Company A", suppliers.get(0).getCompanyName());
        assertEquals("Company B", suppliers.get(1).getCompanyName());
        assertEquals("Company C", suppliers.get(2).getCompanyName());
    }
    
    @Test
    @DisplayName("Should update supplier successfully")
    void testUpdateSupplier() throws SQLException {
        // Given
        Supplier supplier = supplierDAO.save(new Supplier("Original Company"));
        
        // When
        supplier.setCompanyName("Updated Company");
        supplier.setContactPerson("Updated Person");
        supplier.setRating(new BigDecimal("5.0"));
        
        Supplier updatedSupplier = supplierDAO.update(supplier);
        
        // Then
        assertNotNull(updatedSupplier);
        assertEquals("Updated Company", updatedSupplier.getCompanyName());
        assertEquals("Updated Person", updatedSupplier.getContactPerson());
        assertEquals(new BigDecimal("5.0"), updatedSupplier.getRating());
        
        // Verify in database
        Optional<Supplier> foundSupplier = supplierDAO.findById(supplier.getSupplierId());
        assertTrue(foundSupplier.isPresent());
        assertEquals("Updated Company", foundSupplier.get().getCompanyName());
    }
    
    @Test
    @DisplayName("Should soft delete supplier")
    void testDeleteSupplier() throws SQLException {
        // Given
        Supplier supplier = supplierDAO.save(new Supplier("To Delete Company"));
        Integer supplierId = supplier.getSupplierId();
        
        // When
        boolean deleted = supplierDAO.delete(supplierId);
        
        // Then
        assertTrue(deleted);
        
        // Verify supplier is not found (soft deleted)
        Optional<Supplier> foundSupplier = supplierDAO.findById(supplierId);
        assertFalse(foundSupplier.isPresent());
    }
    
    @Test
    @DisplayName("Should find suppliers by name")
    void testFindSuppliersByName() throws SQLException {
        // Given
        supplierDAO.save(new Supplier("Tech Company"));
        supplierDAO.save(new Supplier("Tech Solutions"));
        supplierDAO.save(new Supplier("Global Parts"));
        
        // When
        List<Supplier> techSuppliers = supplierDAO.findByName("Tech");
        
        // Then
        assertEquals(2, techSuppliers.size());
        assertTrue(techSuppliers.stream().allMatch(s -> s.getCompanyName().contains("Tech")));
    }
    
    @Test
    @DisplayName("Should find supplier by email")
    void testFindSupplierByEmail() throws SQLException {
        // Given
        supplierDAO.save(new Supplier("Test Company", "John", null, "unique@test.com", null, null));
        
        // When
        Supplier foundSupplier = supplierDAO.findByEmail("unique@test.com");
        
        // Then
        assertNotNull(foundSupplier);
        assertEquals("Test Company", foundSupplier.getCompanyName());
        assertEquals("unique@test.com", foundSupplier.getEmail());
    }
    
    @Test
    @DisplayName("Should find suppliers by rating range")
    void testFindSuppliersByRatingRange() throws SQLException {
        // Given
        supplierDAO.save(new Supplier("Low Rating", null, null, null, null, new BigDecimal("2.0")));
        supplierDAO.save(new Supplier("Medium Rating", null, null, null, null, new BigDecimal("3.5")));
        supplierDAO.save(new Supplier("High Rating", null, null, null, null, new BigDecimal("4.8")));
        
        // When
        List<Supplier> mediumToHighSuppliers = supplierDAO.findByRatingRange(3.0, 5.0);
        
        // Then
        assertEquals(2, mediumToHighSuppliers.size());
        assertTrue(mediumToHighSuppliers.stream()
                   .allMatch(s -> s.getRating().doubleValue() >= 3.0 && s.getRating().doubleValue() <= 5.0));
    }
    
    @Test
    @DisplayName("Should get top suppliers by rating")
    void testGetTopSuppliers() throws SQLException {
        // Given
        supplierDAO.save(new Supplier("Good Supplier", null, null, null, null, new BigDecimal("4.0")));
        supplierDAO.save(new Supplier("Best Supplier", null, null, null, null, new BigDecimal("4.9")));
        supplierDAO.save(new Supplier("Average Supplier", null, null, null, null, new BigDecimal("3.0")));
        
        // When
        List<Supplier> topSuppliers = supplierDAO.getTopSuppliers(2);
        
        // Then
        assertEquals(2, topSuppliers.size());
        assertEquals("Best Supplier", topSuppliers.get(0).getCompanyName());
        assertEquals("Good Supplier", topSuppliers.get(1).getCompanyName());
        assertTrue(topSuppliers.get(0).getRating().compareTo(topSuppliers.get(1).getRating()) >= 0);
    }
    
    @Test
    @DisplayName("Should check if supplier exists")
    void testSupplierExists() throws SQLException {
        // Given
        Supplier supplier = supplierDAO.save(new Supplier("Existing Company"));
        
        // When & Then
        assertTrue(supplierDAO.exists(supplier.getSupplierId()));
        assertFalse(supplierDAO.exists(999));
    }
    
    @Test
    @DisplayName("Should count suppliers")
    void testCountSuppliers() throws SQLException {
        // Given
        assertEquals(0, supplierDAO.count());
        
        supplierDAO.save(new Supplier("Company 1"));
        supplierDAO.save(new Supplier("Company 2"));
        
        // When & Then
        assertEquals(2, supplierDAO.count());
    }
    
    @Test
    @DisplayName("Should check if supplier has products")
    void testSupplierHasProducts() throws SQLException {
        // Given
        Supplier supplier = supplierDAO.save(new Supplier("Supplier with Products"));
        
        // Add a product for this supplier
        try (Connection connection = DatabaseUtil.getConnection();
             Statement stmt = connection.createStatement()) {
            
            stmt.execute(String.format(
                "INSERT INTO products (product_name, supplier_id) VALUES ('Test Product', %d)",
                supplier.getSupplierId()));
        }
        
        // When & Then
        assertTrue(supplierDAO.hasProducts(supplier.getSupplierId()));
        
        // Test supplier without products
        Supplier supplierNoProducts = supplierDAO.save(new Supplier("Supplier without Products"));
        assertFalse(supplierDAO.hasProducts(supplierNoProducts.getSupplierId()));
    }
    
    @AfterAll
    static void tearDown() {
        // Clean up H2 database
        try (Connection connection = DatabaseUtil.getConnection();
             Statement stmt = connection.createStatement()) {
            
            stmt.execute("DROP ALL OBJECTS");
            
        } catch (SQLException e) {
            // Ignore cleanup errors
        }
    }
}