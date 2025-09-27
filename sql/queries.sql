-- ===================================================================
-- Inventory Management System - SQL Queries
-- Purpose: CRUD operations, reports, and advanced queries
-- ===================================================================

-- ===================================================================
-- BASIC CRUD OPERATIONS
-- ===================================================================

-- ============= SUPPLIERS CRUD =============

-- CREATE: Insert new supplier
INSERT INTO suppliers (company_name, contact_person, phone, email, address, rating) 
VALUES (?, ?, ?, ?, ?, ?);

-- READ: Get all suppliers
SELECT supplier_id, company_name, contact_person, phone, email, address, rating, 
       is_active, created_date, updated_date
FROM suppliers 
WHERE is_active = TRUE
ORDER BY company_name;

-- READ: Get supplier by ID
SELECT * FROM suppliers WHERE supplier_id = ? AND is_active = TRUE;

-- READ: Search suppliers by name
SELECT * FROM suppliers 
WHERE company_name LIKE ? AND is_active = TRUE
ORDER BY company_name;

-- UPDATE: Update supplier information
UPDATE suppliers 
SET company_name = ?, contact_person = ?, phone = ?, email = ?, 
    address = ?, rating = ?, updated_date = CURRENT_TIMESTAMP
WHERE supplier_id = ?;

-- SOFT DELETE: Deactivate supplier
UPDATE suppliers 
SET is_active = FALSE, updated_date = CURRENT_TIMESTAMP 
WHERE supplier_id = ?;

-- ============= PRODUCTS CRUD =============

-- CREATE: Insert new product
INSERT INTO products (product_name, product_code, category, description, 
                     unit_price, stock_quantity, reorder_level, supplier_id) 
VALUES (?, ?, ?, ?, ?, ?, ?, ?);

-- READ: Get all products with supplier information
SELECT p.product_id, p.product_name, p.product_code, p.category, 
       p.description, p.unit_price, p.stock_quantity, p.reorder_level,
       p.is_active, p.created_date, p.updated_date,
       s.company_name AS supplier_name, s.rating AS supplier_rating
FROM products p
JOIN suppliers s ON p.supplier_id = s.supplier_id
WHERE p.is_active = TRUE
ORDER BY p.product_name;

-- READ: Get product by ID
SELECT p.*, s.company_name AS supplier_name
FROM products p
JOIN suppliers s ON p.supplier_id = s.supplier_id
WHERE p.product_id = ? AND p.is_active = TRUE;

-- READ: Search products by name or category
SELECT p.*, s.company_name AS supplier_name
FROM products p
JOIN suppliers s ON p.supplier_id = s.supplier_id
WHERE (p.product_name LIKE ? OR p.category LIKE ?) 
  AND p.is_active = TRUE
ORDER BY p.product_name;

-- READ: Get products by supplier
SELECT * FROM products 
WHERE supplier_id = ? AND is_active = TRUE
ORDER BY product_name;

-- UPDATE: Update product information
UPDATE products 
SET product_name = ?, product_code = ?, category = ?, description = ?,
    unit_price = ?, reorder_level = ?, supplier_id = ?,
    updated_date = CURRENT_TIMESTAMP
WHERE product_id = ?;

-- UPDATE: Update stock quantity (direct)
UPDATE products 
SET stock_quantity = ?, updated_date = CURRENT_TIMESTAMP 
WHERE product_id = ?;

-- SOFT DELETE: Deactivate product
UPDATE products 
SET is_active = FALSE, updated_date = CURRENT_TIMESTAMP 
WHERE product_id = ?;

-- ============= USERS CRUD =============

-- CREATE: Insert new user
INSERT INTO users (username, password, full_name, email, role) 
VALUES (?, ?, ?, ?, ?);

-- READ: Get user for authentication
SELECT user_id, username, password, full_name, email, role, is_active
FROM users 
WHERE username = ? AND is_active = TRUE;

-- READ: Get all active users
SELECT user_id, username, full_name, email, role, is_active, created_date
FROM users 
WHERE is_active = TRUE
ORDER BY full_name;

-- UPDATE: Update user information
UPDATE users 
SET full_name = ?, email = ?, role = ?
WHERE user_id = ?;

-- UPDATE: Update last login
UPDATE users 
SET last_login = CURRENT_TIMESTAMP 
WHERE user_id = ?;

-- UPDATE: Change password
UPDATE users 
SET password = ? 
WHERE user_id = ?;

-- SOFT DELETE: Deactivate user
UPDATE users 
SET is_active = FALSE 
WHERE user_id = ?;

-- ============= TRANSACTIONS CRUD =============

-- CREATE: Insert new transaction
INSERT INTO transactions (transaction_type, product_id, quantity, unit_price, 
                         created_by, reference_number, notes) 
VALUES (?, ?, ?, ?, ?, ?, ?);

-- READ: Get all transactions with product and supplier details
SELECT t.transaction_id, t.transaction_type, t.transaction_date,
       t.quantity, t.unit_price, t.total_amount, t.reference_number, t.notes,
       p.product_name, p.product_code, p.category,
       s.company_name AS supplier_name,
       u.full_name AS created_by_name
FROM transactions t
JOIN products p ON t.product_id = p.product_id
JOIN suppliers s ON p.supplier_id = s.supplier_id
LEFT JOIN users u ON t.created_by = u.user_id
ORDER BY t.transaction_date DESC;

-- READ: Get transactions by product
SELECT t.*, p.product_name, u.full_name AS created_by_name
FROM transactions t
JOIN products p ON t.product_id = p.product_id
LEFT JOIN users u ON t.created_by = u.user_id
WHERE t.product_id = ?
ORDER BY t.transaction_date DESC;

-- READ: Get transactions by date range
SELECT t.*, p.product_name, s.company_name AS supplier_name
FROM transactions t
JOIN products p ON t.product_id = p.product_id
JOIN suppliers s ON p.supplier_id = s.supplier_id
WHERE t.transaction_date BETWEEN ? AND ?
ORDER BY t.transaction_date DESC;

-- READ: Get transaction by ID
SELECT t.*, p.product_name, s.company_name AS supplier_name, u.full_name AS created_by_name
FROM transactions t
JOIN products p ON t.product_id = p.product_id
JOIN suppliers s ON p.supplier_id = s.supplier_id
LEFT JOIN users u ON t.created_by = u.user_id
WHERE t.transaction_id = ?;

-- ===================================================================
-- ADVANCED QUERIES & REPORTS
-- ===================================================================

-- ============= JOIN QUERIES =============

-- Products with their suppliers and current stock status
SELECT p.product_id, p.product_name, p.product_code, p.category,
       p.stock_quantity, p.unit_price, (p.stock_quantity * p.unit_price) AS stock_value,
       p.reorder_level,
       CASE 
           WHEN p.stock_quantity = 0 THEN 'OUT_OF_STOCK'
           WHEN p.stock_quantity <= p.reorder_level THEN 'LOW_STOCK'
           ELSE 'NORMAL'
       END AS stock_status,
       s.company_name AS supplier_name, s.contact_person, s.phone, s.email, s.rating
FROM products p
JOIN suppliers s ON p.supplier_id = s.supplier_id
WHERE p.is_active = TRUE
ORDER BY stock_status DESC, p.product_name;

-- Transaction history with complete details
SELECT t.transaction_id, t.transaction_type, t.transaction_date,
       p.product_name, p.product_code, p.category,
       t.quantity, t.unit_price, t.total_amount,
       s.company_name AS supplier_name,
       u.full_name AS processed_by,
       t.reference_number, t.notes
FROM transactions t
JOIN products p ON t.product_id = p.product_id
JOIN suppliers s ON p.supplier_id = s.supplier_id
LEFT JOIN users u ON t.created_by = u.user_id
ORDER BY t.transaction_date DESC;

-- ============= GROUP BY & AGGREGATION QUERIES =============

-- Sales summary by product (monthly)
SELECT p.product_name, p.product_code, p.category,
       DATE_FORMAT(t.transaction_date, '%Y-%m') AS month,
       SUM(CASE WHEN t.transaction_type = 'SALE' THEN t.quantity ELSE 0 END) AS units_sold,
       SUM(CASE WHEN t.transaction_type = 'SALE' THEN t.total_amount ELSE 0 END) AS total_sales,
       AVG(CASE WHEN t.transaction_type = 'SALE' THEN t.unit_price ELSE NULL END) AS avg_sale_price
FROM transactions t
JOIN products p ON t.product_id = p.product_id
WHERE t.transaction_date >= DATE_SUB(CURRENT_DATE, INTERVAL 12 MONTH)
GROUP BY p.product_id, p.product_name, p.product_code, p.category, 
         DATE_FORMAT(t.transaction_date, '%Y-%m')
ORDER BY month DESC, total_sales DESC;

-- Supplier performance summary
SELECT s.supplier_id, s.company_name, s.rating,
       COUNT(DISTINCT p.product_id) AS products_supplied,
       SUM(p.stock_quantity * p.unit_price) AS current_inventory_value,
       COUNT(t.transaction_id) AS total_transactions,
       SUM(CASE WHEN t.transaction_type = 'PURCHASE' THEN t.total_amount ELSE 0 END) AS total_purchases,
       SUM(CASE WHEN t.transaction_type = 'RETURN_OUT' THEN t.total_amount ELSE 0 END) AS total_returns,
       AVG(CASE WHEN t.transaction_type = 'PURCHASE' THEN t.unit_price ELSE NULL END) AS avg_purchase_price
FROM suppliers s
LEFT JOIN products p ON s.supplier_id = p.supplier_id AND p.is_active = TRUE
LEFT JOIN transactions t ON p.product_id = t.product_id
WHERE s.is_active = TRUE
GROUP BY s.supplier_id, s.company_name, s.rating
ORDER BY total_purchases DESC;

-- Category-wise inventory summary
SELECT p.category,
       COUNT(*) AS product_count,
       SUM(p.stock_quantity) AS total_units,
       SUM(p.stock_quantity * p.unit_price) AS total_value,
       AVG(p.unit_price) AS avg_unit_price,
       MIN(p.unit_price) AS min_price,
       MAX(p.unit_price) AS max_price
FROM products p
WHERE p.is_active = TRUE
GROUP BY p.category
ORDER BY total_value DESC;

-- Monthly transaction summary
SELECT DATE_FORMAT(transaction_date, '%Y-%m') AS month,
       transaction_type,
       COUNT(*) AS transaction_count,
       SUM(quantity) AS total_quantity,
       SUM(total_amount) AS total_amount,
       AVG(total_amount) AS avg_transaction_value
FROM transactions
WHERE transaction_date >= DATE_SUB(CURRENT_DATE, INTERVAL 12 MONTH)
GROUP BY DATE_FORMAT(transaction_date, '%Y-%m'), transaction_type
ORDER BY month DESC, transaction_type;

-- ============= SUBQUERIES =============

-- Find top supplier by total purchase volume
SELECT s.supplier_id, s.company_name, s.rating,
       supplier_purchases.total_purchase_amount
FROM suppliers s
JOIN (
    SELECT p.supplier_id, SUM(t.total_amount) as total_purchase_amount
    FROM products p
    JOIN transactions t ON p.product_id = t.product_id
    WHERE t.transaction_type = 'PURCHASE'
    GROUP BY p.supplier_id
    ORDER BY total_purchase_amount DESC
    LIMIT 1
) supplier_purchases ON s.supplier_id = supplier_purchases.supplier_id;

-- Find most sold product in last 3 months
SELECT p.product_id, p.product_name, p.category,
       product_sales.units_sold, product_sales.total_sales
FROM products p
JOIN (
    SELECT product_id, 
           SUM(quantity) as units_sold,
           SUM(total_amount) as total_sales
    FROM transactions
    WHERE transaction_type = 'SALE' 
      AND transaction_date >= DATE_SUB(CURRENT_DATE, INTERVAL 3 MONTH)
    GROUP BY product_id
    ORDER BY units_sold DESC
    LIMIT 1
) product_sales ON p.product_id = product_sales.product_id;

-- Products with above-average stock value
SELECT p.product_id, p.product_name, p.stock_quantity, p.unit_price,
       (p.stock_quantity * p.unit_price) AS stock_value
FROM products p
WHERE (p.stock_quantity * p.unit_price) > (
    SELECT AVG(stock_quantity * unit_price)
    FROM products
    WHERE is_active = TRUE
)
AND p.is_active = TRUE
ORDER BY stock_value DESC;

-- Suppliers with products below reorder level
SELECT DISTINCT s.supplier_id, s.company_name, s.contact_person, s.phone, s.email
FROM suppliers s
WHERE s.supplier_id IN (
    SELECT DISTINCT supplier_id
    FROM products
    WHERE stock_quantity <= reorder_level 
      AND is_active = TRUE
)
ORDER BY s.company_name;

-- ===================================================================
-- REPORTING QUERIES
-- ===================================================================

-- ============= CURRENT STOCK REPORT =============
SELECT p.product_id, p.product_name, p.product_code, p.category,
       p.stock_quantity, p.unit_price, 
       (p.stock_quantity * p.unit_price) AS stock_value,
       p.reorder_level,
       CASE 
           WHEN p.stock_quantity = 0 THEN 'OUT_OF_STOCK'
           WHEN p.stock_quantity <= p.reorder_level THEN 'LOW_STOCK'
           ELSE 'NORMAL'
       END AS status,
       s.company_name AS supplier
FROM products p
JOIN suppliers s ON p.supplier_id = s.supplier_id
WHERE p.is_active = TRUE
ORDER BY 
    CASE 
        WHEN p.stock_quantity = 0 THEN 1
        WHEN p.stock_quantity <= p.reorder_level THEN 2
        ELSE 3
    END,
    p.product_name;

-- ============= LOW STOCK ALERT REPORT =============
SELECT p.product_id, p.product_name, p.product_code, p.category,
       p.stock_quantity, p.reorder_level,
       (p.reorder_level - p.stock_quantity) AS shortage,
       p.unit_price, s.company_name AS supplier,
       s.contact_person, s.phone, s.email
FROM products p
JOIN suppliers s ON p.supplier_id = s.supplier_id
WHERE p.stock_quantity <= p.reorder_level 
  AND p.is_active = TRUE
ORDER BY shortage DESC, p.product_name;

-- ============= SUPPLIER PERFORMANCE REPORT =============
SELECT s.supplier_id, s.company_name, s.rating,
       COUNT(DISTINCT p.product_id) AS products_count,
       SUM(CASE WHEN t.transaction_type = 'PURCHASE' THEN t.total_amount ELSE 0 END) AS total_purchases,
       COUNT(CASE WHEN t.transaction_type = 'PURCHASE' THEN 1 END) AS purchase_transactions,
       SUM(CASE WHEN t.transaction_type = 'RETURN_OUT' THEN t.total_amount ELSE 0 END) AS total_returns,
       COUNT(CASE WHEN t.transaction_type = 'RETURN_OUT' THEN 1 END) AS return_transactions,
       ROUND(
           (SUM(CASE WHEN t.transaction_type = 'RETURN_OUT' THEN t.total_amount ELSE 0 END) / 
            NULLIF(SUM(CASE WHEN t.transaction_type = 'PURCHASE' THEN t.total_amount ELSE 0 END), 0)) * 100, 
           2
       ) AS return_percentage
FROM suppliers s
LEFT JOIN products p ON s.supplier_id = p.supplier_id
LEFT JOIN transactions t ON p.product_id = t.product_id
WHERE s.is_active = TRUE
GROUP BY s.supplier_id, s.company_name, s.rating
ORDER BY total_purchases DESC;

-- ============= SALES TREND REPORT (Last 12 months) =============
SELECT DATE_FORMAT(t.transaction_date, '%Y-%m') AS month,
       COUNT(CASE WHEN t.transaction_type = 'SALE' THEN 1 END) AS sales_count,
       SUM(CASE WHEN t.transaction_type = 'SALE' THEN t.quantity ELSE 0 END) AS units_sold,
       SUM(CASE WHEN t.transaction_type = 'SALE' THEN t.total_amount ELSE 0 END) AS sales_revenue,
       COUNT(CASE WHEN t.transaction_type = 'PURCHASE' THEN 1 END) AS purchase_count,
       SUM(CASE WHEN t.transaction_type = 'PURCHASE' THEN t.total_amount ELSE 0 END) AS purchase_cost
FROM transactions t
WHERE t.transaction_date >= DATE_SUB(CURRENT_DATE, INTERVAL 12 MONTH)
GROUP BY DATE_FORMAT(t.transaction_date, '%Y-%m')
ORDER BY month DESC;

-- ============= INVENTORY VALUATION REPORT =============
SELECT p.category,
       COUNT(*) AS product_count,
       SUM(p.stock_quantity) AS total_units,
       SUM(p.stock_quantity * p.unit_price) AS inventory_value,
       AVG(p.stock_quantity * p.unit_price) AS avg_product_value,
       SUM(CASE WHEN p.stock_quantity <= p.reorder_level THEN 1 ELSE 0 END) AS low_stock_products
FROM products p
WHERE p.is_active = TRUE
GROUP BY p.category
UNION ALL
SELECT 'TOTAL' AS category,
       COUNT(*) AS product_count,
       SUM(p.stock_quantity) AS total_units,
       SUM(p.stock_quantity * p.unit_price) AS inventory_value,
       AVG(p.stock_quantity * p.unit_price) AS avg_product_value,
       SUM(CASE WHEN p.stock_quantity <= p.reorder_level THEN 1 ELSE 0 END) AS low_stock_products
FROM products p
WHERE p.is_active = TRUE
ORDER BY inventory_value DESC;

-- ===================================================================
-- UTILITY QUERIES
-- ===================================================================

-- Get current stock level for a specific product
SELECT stock_quantity FROM products WHERE product_id = ?;

-- Check if product exists and is active
SELECT COUNT(*) FROM products WHERE product_id = ? AND is_active = TRUE;

-- Get last transaction for a product
SELECT * FROM transactions 
WHERE product_id = ? 
ORDER BY transaction_date DESC, transaction_id DESC 
LIMIT 1;

-- Count total products by supplier
SELECT s.supplier_id, s.company_name, COUNT(p.product_id) AS product_count
FROM suppliers s
LEFT JOIN products p ON s.supplier_id = p.supplier_id AND p.is_active = TRUE
WHERE s.is_active = TRUE
GROUP BY s.supplier_id, s.company_name
ORDER BY product_count DESC;

-- Get products that need reordering
SELECT COUNT(*) as low_stock_count
FROM products 
WHERE stock_quantity <= reorder_level AND is_active = TRUE;

-- ===================================================================
-- BUSINESS INTELLIGENCE QUERIES
-- ===================================================================

-- ABC Analysis (Products by revenue contribution)
SELECT p.product_id, p.product_name, p.category,
       sales_data.total_revenue,
       sales_data.revenue_percentage,
       CASE 
           WHEN sales_data.cumulative_percentage <= 80 THEN 'A'
           WHEN sales_data.cumulative_percentage <= 95 THEN 'B'
           ELSE 'C'
       END AS abc_category
FROM products p
JOIN (
    SELECT p.product_id,
           SUM(t.total_amount) AS total_revenue,
           ROUND(SUM(t.total_amount) * 100.0 / total_sales.total, 2) AS revenue_percentage,
           ROUND(SUM(SUM(t.total_amount)) OVER (ORDER BY SUM(t.total_amount) DESC) * 100.0 / total_sales.total, 2) AS cumulative_percentage
    FROM transactions t
    JOIN products p ON t.product_id = p.product_id
    CROSS JOIN (
        SELECT SUM(total_amount) AS total
        FROM transactions 
        WHERE transaction_type = 'SALE'
    ) total_sales
    WHERE t.transaction_type = 'SALE'
    GROUP BY p.product_id
) sales_data ON p.product_id = sales_data.product_id
ORDER BY sales_data.total_revenue DESC;

-- ===================================================================
-- END OF QUERIES
-- ===================================================================