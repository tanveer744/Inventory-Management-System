-- ===================================================================
-- Inventory Management System - Database Creation Script
-- Database: MySQL/PostgreSQL Compatible
-- Purpose: Create normalized schema for ERP Inventory Module
-- ===================================================================

-- Create Database (MySQL syntax - comment out for PostgreSQL)
-- CREATE DATABASE IF NOT EXISTS inventory_management;
-- USE inventory_management;

-- ===================================================================
-- 1. SUPPLIERS TABLE
-- ===================================================================
CREATE TABLE suppliers (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    company_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100) UNIQUE,
    address TEXT,
    rating DECIMAL(2,1) CHECK (rating >= 1.0 AND rating <= 5.0),
    is_active BOOLEAN DEFAULT TRUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ===================================================================
-- 2. PRODUCTS TABLE
-- ===================================================================
CREATE TABLE products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    product_code VARCHAR(50) UNIQUE,
    category VARCHAR(50) NOT NULL,
    description TEXT,
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0),
    stock_quantity INT NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    reorder_level INT DEFAULT 10,
    supplier_id INT NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) ON DELETE RESTRICT
);

-- ===================================================================
-- 3. USERS TABLE (Optional - for authentication)
-- ===================================================================
CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL, -- Should store hashed passwords
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    role ENUM('ADMIN', 'MANAGER', 'EMPLOYEE') DEFAULT 'EMPLOYEE',
    is_active BOOLEAN DEFAULT TRUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

-- ===================================================================
-- 4. TRANSACTIONS TABLE
-- ===================================================================
CREATE TABLE transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    transaction_type ENUM('PURCHASE', 'SALE', 'RETURN_IN', 'RETURN_OUT', 'ADJUSTMENT') NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL CHECK (unit_price >= 0),
    total_amount DECIMAL(12,2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INT NULL,
    reference_number VARCHAR(50),
    notes TEXT,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT,
    FOREIGN KEY (created_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- ===================================================================
-- PERFORMANCE INDEXES
-- ===================================================================

-- Suppliers indexes
CREATE INDEX idx_suppliers_name ON suppliers(company_name);
CREATE INDEX idx_suppliers_rating ON suppliers(rating);

-- Products indexes
CREATE INDEX idx_products_supplier ON products(supplier_id);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_name ON products(product_name);
CREATE INDEX idx_products_code ON products(product_code);
CREATE INDEX idx_products_stock ON products(stock_quantity);

-- Transactions indexes
CREATE INDEX idx_transactions_product ON transactions(product_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
CREATE INDEX idx_transactions_reference ON transactions(reference_number);

-- ===================================================================
-- VIEWS FOR REPORTING
-- ===================================================================

-- Current Stock Summary View
CREATE VIEW v_stock_summary AS
SELECT 
    p.product_id,
    p.product_name,
    p.product_code,
    p.category,
    p.stock_quantity,
    p.unit_price,
    (p.stock_quantity * p.unit_price) AS stock_value,
    p.reorder_level,
    CASE 
        WHEN p.stock_quantity <= p.reorder_level THEN 'LOW_STOCK'
        WHEN p.stock_quantity = 0 THEN 'OUT_OF_STOCK'
        ELSE 'NORMAL'
    END AS stock_status,
    s.company_name AS supplier_name,
    s.rating AS supplier_rating
FROM products p
JOIN suppliers s ON p.supplier_id = s.supplier_id
WHERE p.is_active = TRUE;

-- Transaction Summary View
CREATE VIEW v_transaction_summary AS
SELECT 
    t.transaction_id,
    t.transaction_type,
    t.transaction_date,
    p.product_name,
    p.product_code,
    p.category,
    t.quantity,
    t.unit_price,
    t.total_amount,
    s.company_name AS supplier_name,
    u.full_name AS created_by_name,
    t.reference_number,
    t.notes
FROM transactions t
JOIN products p ON t.product_id = p.product_id
JOIN suppliers s ON p.supplier_id = s.supplier_id
LEFT JOIN users u ON t.created_by = u.user_id
ORDER BY t.transaction_date DESC;

-- Low Stock Alert View
CREATE VIEW v_low_stock_alert AS
SELECT 
    p.product_id,
    p.product_name,
    p.product_code,
    p.category,
    p.stock_quantity,
    p.reorder_level,
    (p.reorder_level - p.stock_quantity) AS shortage,
    s.company_name AS supplier_name,
    s.phone AS supplier_phone,
    s.email AS supplier_email
FROM products p
JOIN suppliers s ON p.supplier_id = s.supplier_id
WHERE p.stock_quantity <= p.reorder_level
  AND p.is_active = TRUE
ORDER BY shortage DESC;

-- ===================================================================
-- TRIGGERS FOR BUSINESS LOGIC
-- ===================================================================

-- Update product stock on transaction insert
DELIMITER //
CREATE TRIGGER tr_update_stock_on_transaction
AFTER INSERT ON transactions
FOR EACH ROW
BEGIN
    CASE NEW.transaction_type
        WHEN 'PURCHASE' THEN
            UPDATE products 
            SET stock_quantity = stock_quantity + NEW.quantity,
                updated_date = CURRENT_TIMESTAMP
            WHERE product_id = NEW.product_id;
        WHEN 'SALE' THEN
            UPDATE products 
            SET stock_quantity = stock_quantity - NEW.quantity,
                updated_date = CURRENT_TIMESTAMP
            WHERE product_id = NEW.product_id;
        WHEN 'RETURN_IN' THEN
            UPDATE products 
            SET stock_quantity = stock_quantity + NEW.quantity,
                updated_date = CURRENT_TIMESTAMP
            WHERE product_id = NEW.product_id;
        WHEN 'RETURN_OUT' THEN
            UPDATE products 
            SET stock_quantity = stock_quantity - NEW.quantity,
                updated_date = CURRENT_TIMESTAMP
            WHERE product_id = NEW.product_id;
        WHEN 'ADJUSTMENT' THEN
            UPDATE products 
            SET stock_quantity = NEW.quantity,
                updated_date = CURRENT_TIMESTAMP
            WHERE product_id = NEW.product_id;
    END CASE;
END//
DELIMITER ;

-- Prevent negative stock on sales
DELIMITER //
CREATE TRIGGER tr_check_stock_before_sale
BEFORE INSERT ON transactions
FOR EACH ROW
BEGIN
    DECLARE current_stock INT;
    
    IF NEW.transaction_type IN ('SALE', 'RETURN_OUT') THEN
        SELECT stock_quantity INTO current_stock
        FROM products 
        WHERE product_id = NEW.product_id;
        
        IF current_stock < NEW.quantity THEN
            SIGNAL SQLSTATE '45000' 
            SET MESSAGE_TEXT = 'Insufficient stock for this transaction';
        END IF;
    END IF;
END//
DELIMITER ;

-- ===================================================================
-- SAMPLE DATA INSERTION (Optional)
-- ===================================================================

-- Insert sample suppliers
INSERT INTO suppliers (company_name, contact_person, phone, email, address, rating) VALUES
('TechSupply Co.', 'John Smith', '+1-555-0101', 'john@techsupply.com', '123 Tech Street, Silicon Valley', 4.5),
('Global Parts Ltd.', 'Sarah Johnson', '+1-555-0102', 'sarah@globalparts.com', '456 Industrial Ave, Detroit', 4.2),
('Quality Components', 'Mike Wilson', '+1-555-0103', 'mike@qualitycomp.com', '789 Component Blvd, Austin', 4.8),
('Reliable Suppliers', 'Anna Davis', '+1-555-0104', 'anna@reliable.com', '321 Supply Chain Rd, Chicago', 4.0);

-- Insert sample users
INSERT INTO users (username, password, full_name, email, role) VALUES
('admin', '$2b$10$encrypted_password_hash', 'System Administrator', 'admin@company.com', 'ADMIN'),
('manager1', '$2b$10$encrypted_password_hash', 'Inventory Manager', 'manager@company.com', 'MANAGER'),
('employee1', '$2b$10$encrypted_password_hash', 'John Employee', 'john.emp@company.com', 'EMPLOYEE');

-- Insert sample products
INSERT INTO products (product_name, product_code, category, description, unit_price, stock_quantity, reorder_level, supplier_id) VALUES
('Laptop Computer', 'LAPTOP001', 'Electronics', 'High-performance business laptop', 1299.99, 25, 5, 1),
('Office Chair', 'CHAIR001', 'Furniture', 'Ergonomic office chair with lumbar support', 299.99, 15, 3, 2),
('Printer Paper', 'PAPER001', 'Office Supplies', 'A4 white printer paper, 500 sheets', 12.99, 100, 20, 3),
('USB Flash Drive', 'USB001', 'Electronics', '32GB USB 3.0 flash drive', 19.99, 50, 10, 1),
('Desk Lamp', 'LAMP001', 'Furniture', 'LED desk lamp with adjustable brightness', 89.99, 8, 5, 4);

-- Insert sample transactions
INSERT INTO transactions (transaction_type, product_id, quantity, unit_price, created_by, reference_number, notes) VALUES
('PURCHASE', 1, 10, 1299.99, 1, 'PO-2024-001', 'Initial stock purchase'),
('PURCHASE', 2, 20, 299.99, 1, 'PO-2024-002', 'Office furniture order'),
('SALE', 1, 2, 1299.99, 2, 'INV-2024-001', 'Sale to corporate client'),
('PURCHASE', 3, 200, 12.99, 1, 'PO-2024-003', 'Office supplies restock'),
('SALE', 3, 50, 12.99, 2, 'INV-2024-002', 'Bulk paper sale');

-- ===================================================================
-- END OF SCRIPT
-- ===================================================================

COMMIT;