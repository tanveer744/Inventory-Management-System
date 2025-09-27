# 📦 Inventory Management System

## 🎯 Overview

A comprehensive **ERP Supply Chain Management Inventory Module** built with Java and SQL. This system provides complete inventory management capabilities including supplier management, product catalog, stock tracking, transaction processing, and comprehensive reporting.

## ✨ Features

### 🏢 Supplier Management
- ➕ Add/Update/Delete suppliers
- 🔍 Search suppliers by name, email, or rating
- ⭐ Supplier rating and performance tracking
- 📊 Supplier performance reports

### 📦 Product Catalog
- 📝 Complete product information management
- 🏷️ Category-based organization
- 📊 Stock quantity tracking
- ⚠️ Automated low stock alerts
- 💰 Inventory valuation

### 💼 Transaction Management
- 🛒 Purchase transactions (stock increases)
- 🛍️ Sales transactions (stock decreases)
- 🔄 Return management (in/out)
- ⚙️ Stock adjustments
- 📋 Complete transaction history

### 📊 Reporting & Analytics
- 📈 Current stock summary
- ⚠️ Low stock alerts
- 👥 Supplier performance analysis
- 📊 Sales trend analysis
- 💰 Inventory valuation reports
- 📦 ABC analysis for product categorization

### 🛡️ Data Integrity & Security
- 🔐 User authentication and role-based access
- ✅ Data validation and business rule enforcement
- 🔄 Automated stock updates via database triggers
- 📝 Complete audit trail

## 🏗️ Technical Architecture

### Technology Stack
- **Backend**: Java 21+ with JDBC
- **Database**: MySQL 8.0+ / PostgreSQL 12+
- **Build Tool**: Maven 3.6+
- **Testing**: JUnit 5, Mockito
- **Logging**: SLF4J with Logback
- **UI**: Console-based (Phase 1)

### Project Structure
```
ERP/
├── src/
│   ├── main/
│   │   ├── java/com/erp/inventory/
│   │   │   ├── model/          # Entity models
│   │   │   ├── dao/            # Data Access Objects
│   │   │   ├── service/        # Business logic layer
│   │   │   ├── ui/             # User interface
│   │   │   └── util/           # Utilities and helpers
│   │   └── resources/
│   │       ├── application.properties
│   │       └── logback.xml
│   └── test/
│       └── java/com/erp/inventory/  # Test classes
├── sql/
│   ├── create_database.sql          # MySQL schema
│   ├── create_database_postgresql.sql # PostgreSQL schema
│   └── queries.sql                  # All SQL queries
├── database_design.md               # Database design documentation
├── pom.xml                         # Maven configuration
└── README.md                       # This file
```

### Database Schema

#### 📊 ER Diagram
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
```

#### 🗂️ Key Tables
- **suppliers**: Vendor/supplier information
- **products**: Product catalog with stock levels
- **transactions**: All inventory movements
- **users**: Authentication and access control

## 🚀 Getting Started

### Prerequisites
- ☕ Java 21 or higher
- 🛠️ Maven 3.6 or higher
- 🗄️ MySQL 8.0+ or PostgreSQL 12+
- 🖥️ IDE (IntelliJ IDEA, Eclipse, VS Code)

### Installation

#### 1. Clone the Repository
```bash
git clone https://github.com/tanveer744/Inventory-Management-System.git
cd Inventory-Management-System
```

#### 2. Database Setup

**For MySQL:**
```bash
# Create database
mysql -u root -p
CREATE DATABASE inventory_management;
USE inventory_management;

# Run schema creation script
source sql/create_database.sql;
```

**For PostgreSQL:**
```bash
# Create database
psql -U postgres
CREATE DATABASE inventory_management;
\c inventory_management

# Run schema creation script
\i sql/create_database_postgresql.sql
```

#### 3. Configure Database Connection
Edit `src/main/resources/application.properties`:

**MySQL Configuration:**
```properties
db.url=jdbc:mysql://localhost:3306/inventory_management?useSSL=false&serverTimezone=UTC
db.username=root
db.password=your_password
db.driver=com.mysql.cj.jdbc.Driver
```

**PostgreSQL Configuration:**
```properties
db.url=jdbc:postgresql://localhost:5432/inventory_management
db.username=postgres
db.password=your_password
db.driver=org.postgresql.Driver
```

#### 4. Build and Run

**Build the project:**
```bash
mvn clean compile
```

**Run tests:**
```bash
mvn test
```

**Run the application:**
```bash
mvn exec:java -Dexec.mainClass="com.erp.inventory.InventoryManagementApplication"
```

**Create executable JAR:**
```bash
mvn clean package
java -jar target/inventory-management-system-1.0.0-jar-with-dependencies.jar
```

## 🎯 Usage Examples

### 📋 Console Menu System
```
=======================================================
           INVENTORY MANAGEMENT MENU
=======================================================
1. Manage Suppliers
2. Manage Products  
3. Record Transactions
4. View Reports
5. Stock Management
6. System Settings
0. Exit
=======================================================
Enter your choice (0-6):
```

### 🏢 Supplier Management
- Add new suppliers with contact information
- Rate suppliers (1-5 scale)
- Search suppliers by name or rating
- View supplier performance metrics

### 📦 Product Operations
- Add products with categories and pricing
- Set reorder levels for automatic alerts
- Track stock quantities in real-time
- Generate low stock alerts

### 💼 Transaction Processing
- **Purchase**: Increase stock from suppliers
- **Sale**: Decrease stock for customer sales
- **Return In**: Handle customer returns
- **Return Out**: Return defective items to suppliers
- **Adjustment**: Manual stock corrections

### 📊 Advanced Reporting
- **Stock Summary**: Current inventory levels
- **Low Stock Alert**: Products needing reorder
- **Supplier Performance**: Purchase history and return rates
- **Sales Trends**: Monthly sales analytics
- **ABC Analysis**: Product categorization by value

## 🧪 Testing

### Unit Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=SupplierDAOTest

# Generate test coverage report
mvn jacoco:report
```

### Integration Tests
- Database connectivity tests
- DAO integration tests
- Business logic validation
- Error handling scenarios

## 📊 Business Rules

### Stock Management
- ✅ Stock quantities cannot be negative
- ✅ Sales require sufficient stock availability
- ✅ Automatic stock updates via database triggers
- ✅ Low stock alerts when quantity ≤ reorder level

### Data Validation
- ✅ Required field validation
- ✅ Email format validation
- ✅ Rating range validation (1.0 - 5.0)
- ✅ Positive price and quantity validation

### Transaction Integrity
- ✅ Atomic transaction processing
- ✅ Foreign key constraint enforcement
- ✅ Audit trail maintenance
- ✅ Business rule validation

## 🔧 Configuration

### Application Properties
```properties
# Database Configuration
db.url=jdbc:mysql://localhost:3306/inventory_management
db.username=root
db.password=password

# Business Rules
inventory.default.reorder.level=10
inventory.low.stock.threshold=5

# UI Configuration
ui.page.size=20
ui.date.format=dd/MM/yyyy
```

### Logging Configuration
- **INFO**: General application flow
- **DEBUG**: Detailed operation logs
- **ERROR**: Error conditions and exceptions
- **WARN**: Potential issues

## 🔮 Future Enhancements

### Phase 2 - GUI Application
- 🖥️ JavaFX/Swing desktop application
- 📊 Interactive charts and graphs
- 🖨️ PDF report generation
- 📤 CSV import/export functionality

### Phase 3 - Web Application
- 🌐 REST API with Spring Boot
- 🔐 JWT authentication
- 📱 Responsive web interface
- ☁️ Cloud deployment ready

### Phase 4 - Advanced Features
- 🤖 Predictive analytics for reordering
- 📊 Advanced reporting dashboard
- 🔄 Integration with external systems
- 📈 Real-time inventory tracking

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Java coding standards
- Write comprehensive tests
- Document all public methods
- Use meaningful commit messages
- Maintain database integrity

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **Your Name** - *Initial work* - [YourGitHub](https://github.com/yourusername)

## 🙏 Acknowledgments

- Spring Framework team for inspiration
- Apache Commons for utility libraries
- MySQL and PostgreSQL communities
- Open source community for tools and libraries

## 📞 Support

If you encounter any issues or have questions:

1. 📚 Check the [Documentation](database_design.md)
2. 🐛 Submit an [Issue](https://github.com/tanveer744/Inventory-Management-System/issues)
3. 💬 Join our community discussions

## 🤝 Contributing

We welcome contributions! Please see our contributing guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

⭐ **Star this repository if it helped you!**

🔗 **Links:**
- [Repository](https://github.com/tanveer744/Inventory-Management-System)
- [Database Design](database_design.md)
- [Issues](https://github.com/tanveer744/Inventory-Management-System/issues)
- [Pull Requests](https://github.com/tanveer744/Inventory-Management-System/pulls)