# Database Design - Inventory Management System

## Entity-Relationship (ER) Diagram

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│    SUPPLIERS    │     │    PRODUCTS     │     │  TRANSACTIONS   │
├─────────────────┤     ├─────────────────┤     ├─────────────────┤
│ supplier_id(PK) │────▶│ supplier_id(FK) │     │ transaction_id  │
│ company_name    │     │ product_id(PK)  │◄────│ product_id(FK)  │
│ contact_person  │     │ product_name    │     │ transaction_type│
│ phone           │     │ category        │     │ quantity        │
│ email           │     │ unit_price      │     │ unit_price      │
│ address         │     │ stock_quantity  │     │ total_amount    │
│ rating          │     │ reorder_level   │     │ transaction_date│
│ created_date    │     │ created_date    │     │ created_by      │
└─────────────────┘     │ updated_date    │     │ notes           │
                        └─────────────────┘     └─────────────────┘

┌─────────────────┐
│     USERS       │
├─────────────────┤
│ user_id(PK)     │
│ username        │
│ password        │
│ full_name       │
│ email           │
│ role            │
│ is_active       │
│ created_date    │
└─────────────────┘
```

## Table Specifications (3NF Normalized)

### 1. Suppliers Table
- **Primary Key**: supplier_id
- **Purpose**: Store supplier/vendor information
- **Relationships**: One-to-Many with Products

### 2. Products Table
- **Primary Key**: product_id
- **Foreign Key**: supplier_id → suppliers(supplier_id)
- **Purpose**: Store product catalog information
- **Relationships**: Many-to-One with Suppliers, One-to-Many with Transactions

### 3. Transactions Table
- **Primary Key**: transaction_id
- **Foreign Key**: product_id → products(product_id)
- **Purpose**: Record all inventory transactions (purchase, sale, return)
- **Relationships**: Many-to-One with Products

### 4. Users Table (Optional)
- **Primary Key**: user_id
- **Purpose**: User authentication and role management
- **Relationships**: Referenced in transactions.created_by

## Business Rules

1. **Stock Management**:
   - Stock quantity cannot be negative
   - Sales transactions require sufficient stock
   - Purchase transactions increase stock
   - Return transactions can increase or decrease stock

2. **Data Integrity**:
   - All foreign keys must reference valid records
   - Transaction amounts must be positive
   - Supplier ratings must be between 1-5

3. **Audit Trail**:
   - All transactions must be recorded with timestamp
   - Product stock updates must be atomic
   - User actions should be logged

## Indexes for Performance

```sql
-- Primary key indexes (automatic)
-- Additional indexes for performance:
CREATE INDEX idx_products_supplier ON products(supplier_id);
CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_transactions_product ON transactions(product_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
```

## Constraints

1. **Check Constraints**:
   - supplier_rating BETWEEN 1 AND 5
   - stock_quantity >= 0
   - transaction_quantity > 0
   - unit_price >= 0

2. **Foreign Key Constraints**:
   - products.supplier_id → suppliers.supplier_id
   - transactions.product_id → products.product_id

3. **Unique Constraints**:
   - suppliers.email (if provided)
   - users.username
   - users.email