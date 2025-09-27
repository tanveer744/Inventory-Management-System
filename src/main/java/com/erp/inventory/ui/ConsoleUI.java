package com.erp.inventory.ui;

import com.erp.inventory.dao.SupplierDAO;
import com.erp.inventory.dao.impl.SupplierDAOImpl;
import com.erp.inventory.model.Supplier;
import com.erp.inventory.model.Product;
import com.erp.inventory.model.TransactionType;
import com.erp.inventory.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Console-based User Interface for the Inventory Management System
 * Provides a menu-driven interface for inventory operations
 */
public class ConsoleUI {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleUI.class);
    private Scanner scanner;
    private boolean running;
    
    // DAO instances and Services
    private final SupplierDAO supplierDAO;
    private final ProductService productService;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.running = true;
        this.supplierDAO = new SupplierDAOImpl();
        this.productService = new ProductService();
    }

    /**
     * Start the console UI main loop
     */
    public void start() {
        logger.info("Starting Console UI");
        
        while (running) {
            try {
                displayMainMenu();
                int choice = getMenuChoice();
                handleMenuChoice(choice);
            } catch (Exception e) {
                logger.error("Error in UI loop", e);
                System.out.println("An error occurred: " + e.getMessage());
            }
        }
        
        cleanup();
        logger.info("Console UI stopped");
    }

    private void displayMainMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("           INVENTORY MANAGEMENT MENU");
        System.out.println("=".repeat(50));
        System.out.println("1. Manage Suppliers");
        System.out.println("2. Manage Products");
        System.out.println("3. Record Transactions");
        System.out.println("4. View Reports");
        System.out.println("5. Stock Management");
        System.out.println("6. System Settings");
        System.out.println("0. Exit");
        System.out.println("=".repeat(50));
        System.out.print("Enter your choice (0-6): ");
    }

    private int getMenuChoice() {
        try {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number between 0-6.");
            return -1;
        }
    }

    private void handleMenuChoice(int choice) {
        switch (choice) {
            case 1:
                manageSuppliers();
                break;
            case 2:
                manageProducts();
                break;
            case 3:
                recordTransactions();
                break;
            case 4:
                viewReports();
                break;
            case 5:
                stockManagement();
                break;
            case 6:
                systemSettings();
                break;
            case 0:
                exitApplication();
                break;
            default:
                System.out.println("Invalid choice. Please select a number between 0-6.");
        }
    }

    private void manageSuppliers() {
        boolean supplierMenuRunning = true;
        
        while (supplierMenuRunning) {
            displaySupplierMenu();
            int choice = getMenuChoice();
            
            switch (choice) {
                case 1:
                    addSupplier();
                    break;
                case 2:
                    viewAllSuppliers();
                    break;
                case 3:
                    searchSuppliers();
                    break;
                case 4:
                    updateSupplier();
                    break;
                case 5:
                    deleteSupplier();
                    break;
                case 0:
                    supplierMenuRunning = false;
                    break;
                default:
                    System.out.println("Invalid choice. Please select a number between 0-5.");
            }
        }
    }
    
    private void displaySupplierMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("           SUPPLIER MANAGEMENT");
        System.out.println("=".repeat(50));
        System.out.println("1. Add New Supplier");
        System.out.println("2. View All Suppliers");
        System.out.println("3. Search Suppliers");
        System.out.println("4. Update Supplier");
        System.out.println("5. Delete Supplier");
        System.out.println("0. Back to Main Menu");
        System.out.println("=".repeat(50));
        System.out.print("Enter your choice (0-5): ");
    }
    
    private void addSupplier() {
        try {
            System.out.println("\n--- Add New Supplier ---");
            
            System.out.print("Company Name: ");
            String companyName = scanner.nextLine().trim();
            if (companyName.isEmpty()) {
                System.out.println("Company name cannot be empty!");
                return;
            }
            
            System.out.print("Contact Person: ");
            String contactPerson = scanner.nextLine().trim();
            
            System.out.print("Phone: ");
            String phone = scanner.nextLine().trim();
            
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();
            
            System.out.print("Address: ");
            String address = scanner.nextLine().trim();
            
            System.out.print("Rating (1.0 - 5.0): ");
            double rating = 0.0;
            try {
                rating = Double.parseDouble(scanner.nextLine().trim());
                if (rating < 1.0 || rating > 5.0) {
                    System.out.println("Rating must be between 1.0 and 5.0!");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid rating format!");
                return;
            }
            
            Supplier supplier = new Supplier(companyName, contactPerson, phone, email, address, BigDecimal.valueOf(rating));
            
            Supplier savedSupplier = supplierDAO.save(supplier);
            System.out.println("✅ Supplier added successfully!");
            System.out.println("Supplier ID: " + savedSupplier.getSupplierId());
            System.out.println("Company: " + savedSupplier.getCompanyName());
            
        } catch (SQLException e) {
            logger.error("Error adding supplier", e);
            System.out.println("❌ Error adding supplier: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error adding supplier", e);
            System.out.println("❌ Unexpected error occurred!");
        }
        
        pauseForUser();
    }
    
    private void viewAllSuppliers() {
        try {
            System.out.println("\n--- All Suppliers ---");
            
            List<Supplier> suppliers = supplierDAO.findAll();
            
            if (suppliers.isEmpty()) {
                System.out.println("No suppliers found.");
                pauseForUser();
                return;
            }
            
            System.out.println("\n" + "=".repeat(120));
            System.out.printf("%-5s %-25s %-20s %-15s %-30s %-8s%n", 
                            "ID", "Company Name", "Contact Person", "Phone", "Email", "Rating");
            System.out.println("=".repeat(120));
            
            for (Supplier supplier : suppliers) {
                System.out.printf("%-5d %-25s %-20s %-15s %-30s %-8.1f%n",
                                supplier.getSupplierId(),
                                truncateString(supplier.getCompanyName(), 25),
                                truncateString(supplier.getContactPerson(), 20),
                                truncateString(supplier.getPhone(), 15),
                                truncateString(supplier.getEmail(), 30),
                                supplier.getRating() != null ? supplier.getRating().doubleValue() : 0.0);
            }
            System.out.println("=".repeat(120));
            System.out.println("Total suppliers: " + suppliers.size());
            
        } catch (SQLException e) {
            logger.error("Error viewing suppliers", e);
            System.out.println("❌ Error retrieving suppliers: " + e.getMessage());
        }
        
        pauseForUser();
    }
    
    private void searchSuppliers() {
        System.out.println("\n--- Search Suppliers ---");
        System.out.println("1. Search by name");
        System.out.println("2. Search by rating range");
        System.out.print("Choose search type (1-2): ");
        
        int searchType = getMenuChoice();
        
        try {
            switch (searchType) {
                case 1:
                    searchSuppliersByName();
                    break;
                case 2:
                    searchSuppliersByRating();
                    break;
                default:
                    System.out.println("Invalid search type!");
            }
        } catch (SQLException e) {
            logger.error("Error searching suppliers", e);
            System.out.println("❌ Error searching suppliers: " + e.getMessage());
        }
        
        pauseForUser();
    }
    
    private void searchSuppliersByName() throws SQLException {
        System.out.print("Enter company name (partial match allowed): ");
        String searchName = scanner.nextLine().trim();
        
        if (searchName.isEmpty()) {
            System.out.println("Search name cannot be empty!");
            return;
        }
        
        List<Supplier> suppliers = supplierDAO.findByName(searchName);
        
        if (suppliers.isEmpty()) {
            System.out.println("No suppliers found matching: " + searchName);
            return;
        }
        
        System.out.println("\nSearch Results:");
        displaySuppliersTable(suppliers);
    }
    
    private void searchSuppliersByRating() throws SQLException {
        System.out.print("Enter minimum rating (1.0-5.0): ");
        try {
            double minRating = Double.parseDouble(scanner.nextLine().trim());
            System.out.print("Enter maximum rating (1.0-5.0): ");
            double maxRating = Double.parseDouble(scanner.nextLine().trim());
            
            if (minRating < 1.0 || minRating > 5.0 || maxRating < 1.0 || maxRating > 5.0) {
                System.out.println("Rating must be between 1.0 and 5.0!");
                return;
            }
            
            if (minRating > maxRating) {
                System.out.println("Minimum rating cannot be greater than maximum rating!");
                return;
            }
            
            List<Supplier> suppliers = supplierDAO.findByRatingRange(minRating, maxRating);
            
            if (suppliers.isEmpty()) {
                System.out.println("No suppliers found in rating range " + minRating + " - " + maxRating);
                return;
            }
            
            System.out.println("\nSuppliers with rating between " + minRating + " and " + maxRating + ":");
            displaySuppliersTable(suppliers);
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid rating format!");
        }
    }
    
    private void updateSupplier() {
        try {
            System.out.print("Enter Supplier ID to update: ");
            int supplierId;
            try {
                supplierId = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid supplier ID!");
                return;
            }
            
            Optional<Supplier> optionalSupplier = supplierDAO.findById(supplierId);
            
            if (optionalSupplier.isEmpty()) {
                System.out.println("Supplier not found with ID: " + supplierId);
                return;
            }
            
            Supplier supplier = optionalSupplier.get();
            System.out.println("\nCurrent Supplier Details:");
            displaySupplierDetails(supplier);
            
            System.out.println("\nEnter new values (press Enter to keep current value):");
            
            System.out.print("Company Name [" + supplier.getCompanyName() + "]: ");
            String companyName = scanner.nextLine().trim();
            if (!companyName.isEmpty()) {
                supplier.setCompanyName(companyName);
            }
            
            System.out.print("Contact Person [" + supplier.getContactPerson() + "]: ");
            String contactPerson = scanner.nextLine().trim();
            if (!contactPerson.isEmpty()) {
                supplier.setContactPerson(contactPerson);
            }
            
            System.out.print("Phone [" + supplier.getPhone() + "]: ");
            String phone = scanner.nextLine().trim();
            if (!phone.isEmpty()) {
                supplier.setPhone(phone);
            }
            
            System.out.print("Email [" + supplier.getEmail() + "]: ");
            String email = scanner.nextLine().trim();
            if (!email.isEmpty()) {
                supplier.setEmail(email);
            }
            
            System.out.print("Address [" + supplier.getAddress() + "]: ");
            String address = scanner.nextLine().trim();
            if (!address.isEmpty()) {
                supplier.setAddress(address);
            }
            
            System.out.print("Rating [" + supplier.getRating() + "]: ");
            String ratingStr = scanner.nextLine().trim();
            if (!ratingStr.isEmpty()) {
                try {
                    double rating = Double.parseDouble(ratingStr);
                    if (rating >= 1.0 && rating <= 5.0) {
                        supplier.setRating(BigDecimal.valueOf(rating));
                    } else {
                        System.out.println("Rating must be between 1.0 and 5.0. Keeping current value.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid rating format. Keeping current value.");
                }
            }
            
            Supplier updatedSupplier = supplierDAO.update(supplier);
            System.out.println("✅ Supplier updated successfully!");
            displaySupplierDetails(updatedSupplier);
            
        } catch (SQLException e) {
            logger.error("Error updating supplier", e);
            System.out.println("❌ Error updating supplier: " + e.getMessage());
        }
        
        pauseForUser();
    }
    
    private void deleteSupplier() {
        try {
            System.out.print("Enter Supplier ID to delete: ");
            int supplierId;
            try {
                supplierId = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid supplier ID!");
                return;
            }
            
            Optional<Supplier> optionalSupplier = supplierDAO.findById(supplierId);
            
            if (optionalSupplier.isEmpty()) {
                System.out.println("Supplier not found with ID: " + supplierId);
                return;
            }
            
            Supplier supplier = optionalSupplier.get();
            System.out.println("\nSupplier to delete:");
            displaySupplierDetails(supplier);
            
            // Check if supplier has products
            if (supplierDAO.hasProducts(supplierId)) {
                System.out.println("⚠️  Warning: This supplier has products associated with it.");
                System.out.print("Are you sure you want to delete? This will deactivate the supplier (y/N): ");
            } else {
                System.out.print("Are you sure you want to delete this supplier? (y/N): ");
            }
            
            String confirmation = scanner.nextLine().trim().toLowerCase();
            
            if (confirmation.equals("y") || confirmation.equals("yes")) {
                boolean deleted = supplierDAO.delete(supplierId);
                if (deleted) {
                    System.out.println("✅ Supplier deleted successfully!");
                } else {
                    System.out.println("❌ Failed to delete supplier.");
                }
            } else {
                System.out.println("Delete operation cancelled.");
            }
            
        } catch (SQLException e) {
            logger.error("Error deleting supplier", e);
            System.out.println("❌ Error deleting supplier: " + e.getMessage());
        }
        
        pauseForUser();
    }
    
    // Helper methods
    private void displaySuppliersTable(List<Supplier> suppliers) {
        System.out.println("\n" + "=".repeat(120));
        System.out.printf("%-5s %-25s %-20s %-15s %-30s %-8s%n", 
                        "ID", "Company Name", "Contact Person", "Phone", "Email", "Rating");
        System.out.println("=".repeat(120));
        
        for (Supplier supplier : suppliers) {
            System.out.printf("%-5d %-25s %-20s %-15s %-30s %-8.1f%n",
                            supplier.getSupplierId(),
                            truncateString(supplier.getCompanyName(), 25),
                            truncateString(supplier.getContactPerson(), 20),
                            truncateString(supplier.getPhone(), 15),
                            truncateString(supplier.getEmail(), 30),
                            supplier.getRating() != null ? supplier.getRating().doubleValue() : 0.0);
        }
        System.out.println("=".repeat(120));
        System.out.println("Found " + suppliers.size() + " supplier(s)");
    }
    
    private void displaySupplierDetails(Supplier supplier) {
        System.out.println("Supplier ID: " + supplier.getSupplierId());
        System.out.println("Company Name: " + supplier.getCompanyName());
        System.out.println("Contact Person: " + supplier.getContactPerson());
        System.out.println("Phone: " + supplier.getPhone());
        System.out.println("Email: " + supplier.getEmail());
        System.out.println("Address: " + supplier.getAddress());
        System.out.println("Rating: " + (supplier.getRating() != null ? supplier.getRating().doubleValue() : "N/A"));
        System.out.println("Created: " + supplier.getCreatedDate());
        System.out.println("Updated: " + supplier.getUpdatedDate());
    }

    private void manageProducts() {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("           PRODUCT MANAGEMENT");
            System.out.println("=".repeat(50));
            System.out.println("1. Add New Product");
            System.out.println("2. View All Products");
            System.out.println("3. Search Products");
            System.out.println("4. Update Product");
            System.out.println("5. Delete Product");
            System.out.println("0. Back to Main Menu");
            System.out.println("=".repeat(50));
            
            try {
                System.out.print("Enter your choice (0-5): ");
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1:
                        addProduct();
                        break;
                    case 2:
                        viewAllProducts();
                        break;
                    case 3:
                        searchProducts();
                        break;
                    case 4:
                        updateProduct();
                        break;
                    case 5:
                        deleteProduct();
                        break;
                    case 0:
                        back = true;
                        break;
                    default:
                        System.out.println("❌ Invalid choice. Please enter a number between 0-5.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a valid number.");
            } catch (Exception e) {
                System.out.println("❌ An error occurred: " + e.getMessage());
                logger.error("Error in product management", e);
            }
        }
    }

    private void recordTransactions() {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("           TRANSACTION MANAGEMENT");
            System.out.println("=".repeat(50));
            System.out.println("1. Record Purchase");
            System.out.println("2. Record Sale");
            System.out.println("3. Record Return (In)");
            System.out.println("4. Record Return (Out)");
            System.out.println("5. Stock Adjustment");
            System.out.println("6. View Transaction History");
            System.out.println("0. Back to Main Menu");
            System.out.println("=".repeat(50));
            
            try {
                System.out.print("Enter your choice (0-6): ");
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1:
                        recordTransaction(TransactionType.PURCHASE);
                        break;
                    case 2:
                        recordTransaction(TransactionType.SALE);
                        break;
                    case 3:
                        recordTransaction(TransactionType.RETURN_IN);
                        break;
                    case 4:
                        recordTransaction(TransactionType.RETURN_OUT);
                        break;
                    case 5:
                        recordTransaction(TransactionType.ADJUSTMENT);
                        break;
                    case 6:
                        viewTransactionHistory();
                        break;
                    case 0:
                        back = true;
                        break;
                    default:
                        System.out.println("❌ Invalid choice. Please enter a number between 0-6.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a valid number.");
            } catch (Exception e) {
                System.out.println("❌ An error occurred: " + e.getMessage());
                logger.error("Error in transaction management", e);
            }
        }
    }

    private void viewReports() {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("           REPORTS & ANALYTICS");
            System.out.println("=".repeat(50));
            System.out.println("1. Stock Summary Report");
            System.out.println("2. Low Stock Alerts");
            System.out.println("3. Transaction History Report");
            System.out.println("4. Supplier Performance Report");
            System.out.println("5. Inventory Valuation Report");
            System.out.println("0. Back to Main Menu");
            System.out.println("=".repeat(50));
            
            try {
                System.out.print("Enter your choice (0-5): ");
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1:
                        generateStockSummaryReport();
                        break;
                    case 2:
                        generateLowStockReport();
                        break;
                    case 3:
                        generateTransactionReport();
                        break;
                    case 4:
                        generateSupplierReport();
                        break;
                    case 5:
                        generateValuationReport();
                        break;
                    case 0:
                        back = true;
                        break;
                    default:
                        System.out.println("❌ Invalid choice. Please enter a number between 0-5.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a valid number.");
            } catch (Exception e) {
                System.out.println("❌ An error occurred: " + e.getMessage());
                logger.error("Error in reports", e);
            }
        }
    }

    private void stockManagement() {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("           STOCK MANAGEMENT");
            System.out.println("=".repeat(50));
            System.out.println("1. View Current Stock Levels");
            System.out.println("2. Check Low Stock Alerts");
            System.out.println("3. Generate Reorder List");
            System.out.println("4. Manual Stock Adjustment");
            System.out.println("5. Stock Audit");
            System.out.println("0. Back to Main Menu");
            System.out.println("=".repeat(50));
            
            try {
                System.out.print("Enter your choice (0-5): ");
                int choice = Integer.parseInt(scanner.nextLine().trim());
                
                switch (choice) {
                    case 1:
                        viewCurrentStock();
                        break;
                    case 2:
                        checkLowStockAlerts();
                        break;
                    case 3:
                        generateReorderList();
                        break;
                    case 4:
                        performStockAdjustment();
                        break;
                    case 5:
                        performStockAudit();
                        break;
                    case 0:
                        back = true;
                        break;
                    default:
                        System.out.println("❌ Invalid choice. Please enter a number between 0-5.");
                }
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a valid number.");
            } catch (Exception e) {
                System.out.println("❌ An error occurred: " + e.getMessage());
                logger.error("Error in stock management", e);
            }
        }
    }

    private void systemSettings() {
        System.out.println("\n--- System Settings ---");
        System.out.println("This feature will provide system configuration.");
        System.out.println("Available settings:");
        System.out.println("- Database connection test");
        System.out.println("- User management (when implemented)");
        System.out.println("- Application preferences");
        System.out.println("- Backup and restore");
        pauseForUser();
    }

    private void exitApplication() {
        System.out.println("\nThank you for using the Inventory Management System!");
        System.out.println("Goodbye!");
        running = false;
    }

    private void pauseForUser() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }

    // ===== PRODUCT MANAGEMENT METHODS =====
    
    private void addProduct() {
        System.out.println("\n--- Add New Product ---");
        
        try {
            // Get all suppliers for selection
            List<Supplier> suppliers = productService.getAllSuppliers();
            if (suppliers.isEmpty()) {
                System.out.println("No suppliers available. Please add suppliers first.");
                pauseForUser();
                return;
            }
            
            // Get product details from user
            System.out.print("Enter product name: ");
            String productName = scanner.nextLine().trim();
            if (productName.isEmpty()) {
                System.out.println("Product name is required.");
                pauseForUser();
                return;
            }
            
            System.out.print("Enter product code (optional): ");
            String productCode = scanner.nextLine().trim();
            if (productCode.isEmpty()) {
                productCode = null;
            }
            
            System.out.print("Enter category: ");
            String category = scanner.nextLine().trim();
            if (category.isEmpty()) {
                System.out.println("Category is required.");
                pauseForUser();
                return;
            }
            
            System.out.print("Enter description (optional): ");
            String description = scanner.nextLine().trim();
            if (description.isEmpty()) {
                description = null;
            }
            
            // Get unit price
            BigDecimal unitPrice;
            while (true) {
                try {
                    System.out.print("Enter unit price: $");
                    String priceInput = scanner.nextLine().trim();
                    unitPrice = new BigDecimal(priceInput);
                    if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                        System.out.println("Unit price cannot be negative.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid price format. Please enter a valid number.");
                }
            }
            
            // Get stock quantity
            Integer stockQuantity;
            while (true) {
                try {
                    System.out.print("Enter initial stock quantity: ");
                    String stockInput = scanner.nextLine().trim();
                    stockQuantity = Integer.parseInt(stockInput);
                    if (stockQuantity < 0) {
                        System.out.println("Stock quantity cannot be negative.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid quantity format. Please enter a valid integer.");
                }
            }
            
            // Get reorder level
            Integer reorderLevel;
            while (true) {
                try {
                    System.out.print("Enter reorder level (default 10): ");
                    String reorderInput = scanner.nextLine().trim();
                    if (reorderInput.isEmpty()) {
                        reorderLevel = 10;
                        break;
                    }
                    reorderLevel = Integer.parseInt(reorderInput);
                    if (reorderLevel < 0) {
                        System.out.println("Reorder level cannot be negative.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid reorder level format. Please enter a valid integer.");
                }
            }
            
            // Display suppliers for selection
            System.out.println("\nAvailable Suppliers:");
            System.out.println("---------------------");
            for (int i = 0; i < suppliers.size(); i++) {
                Supplier supplier = suppliers.get(i);
                System.out.printf("%d. %s (Rating: %.1f)\n", 
                    i + 1, supplier.getCompanyName(), 
                    supplier.getRating() != null ? supplier.getRating() : 0.0);
            }
            
            // Get supplier selection
            Integer selectedSupplierId = null;
            while (selectedSupplierId == null) {
                try {
                    System.out.print("Select supplier (enter number): ");
                    String supplierInput = scanner.nextLine().trim();
                    int supplierChoice = Integer.parseInt(supplierInput);
                    if (supplierChoice < 1 || supplierChoice > suppliers.size()) {
                        System.out.println("Invalid supplier selection. Please choose a number between 1 and " + suppliers.size());
                        continue;
                    }
                    selectedSupplierId = suppliers.get(supplierChoice - 1).getSupplierId();
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a valid number.");
                }
            }
            
            // Create the product
            Product newProduct = productService.createProduct(
                productName, productCode, category, description, 
                unitPrice, stockQuantity, reorderLevel, selectedSupplierId
            );
            
            System.out.println("\n✓ Product added successfully!");
            System.out.println("Product ID: " + newProduct.getProductId());
            System.out.println("Product Name: " + newProduct.getProductName());
            if (newProduct.getProductCode() != null) {
                System.out.println("Product Code: " + newProduct.getProductCode());
            }
            System.out.println("Category: " + newProduct.getCategory());
            System.out.println("Unit Price: $" + newProduct.getUnitPrice());
            System.out.println("Stock Quantity: " + newProduct.getStockQuantity());
            System.out.println("Reorder Level: " + newProduct.getReorderLevel());
            
            // Display supplier info
            final Integer finalSupplierId = selectedSupplierId;
            Optional<Supplier> selectedSupplier = suppliers.stream()
                .filter(s -> s.getSupplierId().equals(finalSupplierId))
                .findFirst();
            if (selectedSupplier.isPresent()) {
                System.out.println("Supplier: " + selectedSupplier.get().getCompanyName());
            }
            
        } catch (ProductService.ValidationException e) {
            System.out.println("\n❌ Validation Error: " + e.getMessage());
        } catch (SQLException e) {
            logger.error("Database error adding product", e);
            System.out.println("\n❌ Database error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error adding product", e);
            System.out.println("\n❌ Unexpected error: " + e.getMessage());
        }
        
        pauseForUser();
    }

    private void viewAllProducts() {
        System.out.println("\n--- All Products ---");
        
        try {
            List<Product> products = productService.findAllProducts();
            if (products.isEmpty()) {
                System.out.println("No products found.");
            } else {
                System.out.println("\nFound " + products.size() + " product(s):\n");
                displayProductsTable(products);
            }
        } catch (SQLException e) {
            logger.error("Database error viewing products", e);
            System.out.println("\n❌ Database error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error viewing products", e);
            System.out.println("\n❌ Unexpected error: " + e.getMessage());
        }
        
        pauseForUser();
    }

    private void searchProducts() {
        System.out.println("\n--- Search Products ---");
        System.out.println("1. Search by Name");
        System.out.println("2. Search by Category");
        System.out.println("3. Search by Product Code");
        System.out.println("4. Search by Supplier");
        System.out.print("\nChoose search type (1-4): ");
        
        try {
            int searchType = Integer.parseInt(scanner.nextLine().trim());
            List<Product> products = null;
            
            switch (searchType) {
                case 1:
                    System.out.print("Enter product name (partial match): ");
                    String name = scanner.nextLine().trim();
                    products = productService.searchProductsByName(name);
                    break;
                    
                case 2:
                    System.out.print("Enter category: ");
                    String category = scanner.nextLine().trim();
                    products = productService.findProductsByCategory(category);
                    break;
                    
                case 3:
                    System.out.print("Enter product code: ");
                    String code = scanner.nextLine().trim();
                    Product product = productService.findProductByCode(code);
                    products = product != null ? List.of(product) : List.of();
                    break;
                    
                case 4:
                    // Show suppliers first
                    List<Supplier> suppliers = productService.getAllSuppliers();
                    if (suppliers.isEmpty()) {
                        System.out.println("No suppliers available.");
                        pauseForUser();
                        return;
                    }
                    
                    System.out.println("\nAvailable Suppliers:");
                    for (int i = 0; i < suppliers.size(); i++) {
                        Supplier supplier = suppliers.get(i);
                        System.out.printf("%d. %s\n", i + 1, supplier.getCompanyName());
                    }
                    
                    System.out.print("Select supplier (enter number): ");
                    int supplierChoice = Integer.parseInt(scanner.nextLine().trim());
                    if (supplierChoice < 1 || supplierChoice > suppliers.size()) {
                        System.out.println("Invalid supplier selection.");
                        pauseForUser();
                        return;
                    }
                    
                    Integer supplierId = suppliers.get(supplierChoice - 1).getSupplierId();
                    products = productService.findProductsBySupplier(supplierId);
                    break;
                    
                default:
                    System.out.println("Invalid search type.");
                    pauseForUser();
                    return;
            }
            
            if (products != null) {
                if (products.isEmpty()) {
                    System.out.println("\nNo products found matching the search criteria.");
                } else {
                    System.out.println("\nFound " + products.size() + " product(s):\n");
                    displayProductsTable(products);
                }
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a valid number.");
        } catch (SQLException e) {
            logger.error("Database error searching products", e);
            System.out.println("\n❌ Database error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error searching products", e);
            System.out.println("\n❌ Unexpected error: " + e.getMessage());
        }
        
        pauseForUser();
    }

    private void updateProduct() {
        System.out.println("\n--- Update Product ---");
        
        try {
            System.out.print("Enter Product ID to update: ");
            String productIdInput = scanner.nextLine().trim();
            Integer productId = Integer.parseInt(productIdInput);
            
            Optional<Product> productOpt = productService.findProductById(productId);
            if (!productOpt.isPresent()) {
                System.out.println("Product not found with ID: " + productId);
                pauseForUser();
                return;
            }
            
            Product existingProduct = productOpt.get();
            System.out.println("\nCurrent Product Details:");
            System.out.println("Name: " + existingProduct.getProductName());
            System.out.println("Code: " + (existingProduct.getProductCode() != null ? existingProduct.getProductCode() : "N/A"));
            System.out.println("Category: " + existingProduct.getCategory());
            System.out.println("Price: $" + existingProduct.getUnitPrice());
            System.out.println("Stock: " + existingProduct.getStockQuantity());
            System.out.println("Reorder Level: " + existingProduct.getReorderLevel());
            
            System.out.println("\nEnter new values (press Enter to keep current value):");
            
            // Update product name
            System.out.print("Product name [" + existingProduct.getProductName() + "]: ");
            String productName = scanner.nextLine().trim();
            if (productName.isEmpty()) {
                productName = existingProduct.getProductName();
            }
            
            // Update product code
            System.out.print("Product code [" + (existingProduct.getProductCode() != null ? existingProduct.getProductCode() : "N/A") + "]: ");
            String productCode = scanner.nextLine().trim();
            if (productCode.isEmpty()) {
                productCode = existingProduct.getProductCode();
            }
            
            // Update category
            System.out.print("Category [" + existingProduct.getCategory() + "]: ");
            String category = scanner.nextLine().trim();
            if (category.isEmpty()) {
                category = existingProduct.getCategory();
            }
            
            // Update description
            System.out.print("Description [" + (existingProduct.getDescription() != null ? existingProduct.getDescription() : "N/A") + "]: ");
            String description = scanner.nextLine().trim();
            if (description.isEmpty()) {
                description = existingProduct.getDescription();
            }
            
            // Update unit price
            BigDecimal unitPrice;
            while (true) {
                System.out.print("Unit price [$" + existingProduct.getUnitPrice() + "]: ");
                String priceInput = scanner.nextLine().trim();
                if (priceInput.isEmpty()) {
                    unitPrice = existingProduct.getUnitPrice();
                    break;
                }
                try {
                    unitPrice = new BigDecimal(priceInput);
                    if (unitPrice.compareTo(BigDecimal.ZERO) < 0) {
                        System.out.println("Unit price cannot be negative.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid price format. Please enter a valid number.");
                }
            }
            
            // Update stock quantity
            Integer stockQuantity;
            while (true) {
                System.out.print("Stock quantity [" + existingProduct.getStockQuantity() + "]: ");
                String stockInput = scanner.nextLine().trim();
                if (stockInput.isEmpty()) {
                    stockQuantity = existingProduct.getStockQuantity();
                    break;
                }
                try {
                    stockQuantity = Integer.parseInt(stockInput);
                    if (stockQuantity < 0) {
                        System.out.println("Stock quantity cannot be negative.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid quantity format. Please enter a valid integer.");
                }
            }
            
            // Update reorder level
            Integer reorderLevel;
            while (true) {
                System.out.print("Reorder level [" + existingProduct.getReorderLevel() + "]: ");
                String reorderInput = scanner.nextLine().trim();
                if (reorderInput.isEmpty()) {
                    reorderLevel = existingProduct.getReorderLevel();
                    break;
                }
                try {
                    reorderLevel = Integer.parseInt(reorderInput);
                    if (reorderLevel < 0) {
                        System.out.println("Reorder level cannot be negative.");
                        continue;
                    }
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid reorder level format. Please enter a valid integer.");
                }
            }
            
            // Show suppliers and allow change
            List<Supplier> suppliers = productService.getAllSuppliers();
            Integer supplierId = existingProduct.getSupplierId();
            
            System.out.println("\nCurrent supplier: " + existingProduct.getSupplierName());
            System.out.print("Change supplier? (y/N): ");
            String changeSupplier = scanner.nextLine().trim().toLowerCase();
            
            if ("y".equals(changeSupplier) || "yes".equals(changeSupplier)) {
                System.out.println("\nAvailable Suppliers:");
                for (int i = 0; i < suppliers.size(); i++) {
                    Supplier supplier = suppliers.get(i);
                    System.out.printf("%d. %s\n", i + 1, supplier.getCompanyName());
                }
                
                while (true) {
                    try {
                        System.out.print("Select supplier (enter number): ");
                        String supplierInput = scanner.nextLine().trim();
                        int supplierChoice = Integer.parseInt(supplierInput);
                        if (supplierChoice < 1 || supplierChoice > suppliers.size()) {
                            System.out.println("Invalid supplier selection.");
                            continue;
                        }
                        supplierId = suppliers.get(supplierChoice - 1).getSupplierId();
                        break;
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a valid number.");
                    }
                }
            }
            
            // Update the product
            Product updatedProduct = productService.updateProduct(
                productId, productName, productCode, category, description,
                unitPrice, stockQuantity, reorderLevel, supplierId
            );
            
            System.out.println("\n✓ Product updated successfully!");
            System.out.println("Product ID: " + updatedProduct.getProductId());
            System.out.println("Product Name: " + updatedProduct.getProductName());
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid Product ID format. Please enter a valid number.");
        } catch (ProductService.ValidationException e) {
            System.out.println("\n❌ Validation Error: " + e.getMessage());
        } catch (SQLException e) {
            logger.error("Database error updating product", e);
            System.out.println("\n❌ Database error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating product", e);
            System.out.println("\n❌ Unexpected error: " + e.getMessage());
        }
        
        pauseForUser();
    }

    private void deleteProduct() {
        System.out.println("\n--- Delete Product ---");
        
        try {
            System.out.print("Enter Product ID to delete: ");
            String productIdInput = scanner.nextLine().trim();
            Integer productId = Integer.parseInt(productIdInput);
            
            Optional<Product> productOpt = productService.findProductById(productId);
            if (!productOpt.isPresent()) {
                System.out.println("Product not found with ID: " + productId);
                pauseForUser();
                return;
            }
            
            Product product = productOpt.get();
            System.out.println("\nProduct Details:");
            System.out.println("Name: " + product.getProductName());
            System.out.println("Code: " + (product.getProductCode() != null ? product.getProductCode() : "N/A"));
            System.out.println("Category: " + product.getCategory());
            System.out.println("Stock Quantity: " + product.getStockQuantity());
            
            System.out.print("\nAre you sure you want to delete this product? (y/N): ");
            String confirmation = scanner.nextLine().trim().toLowerCase();
            
            if ("y".equals(confirmation) || "yes".equals(confirmation)) {
                boolean deleted = productService.deleteProduct(productId);
                if (deleted) {
                    System.out.println("\n✓ Product deleted successfully!");
                } else {
                    System.out.println("\n❌ Failed to delete product.");
                }
            } else {
                System.out.println("\nProduct deletion cancelled.");
            }
            
        } catch (NumberFormatException e) {
            System.out.println("Invalid Product ID format. Please enter a valid number.");
        } catch (ProductService.ValidationException e) {
            System.out.println("\n❌ Validation Error: " + e.getMessage());
        } catch (SQLException e) {
            logger.error("Database error deleting product", e);
            System.out.println("\n❌ Database error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error deleting product", e);
            System.out.println("\n❌ Unexpected error: " + e.getMessage());
        }
        
        pauseForUser();
    }

    // ===== TRANSACTION MANAGEMENT METHODS =====
    
    private void recordTransaction(TransactionType type) {
        System.out.println("\n--- Record " + type.getDisplayName() + " ---");
        System.out.println("Note: Transaction DAO not yet implemented.");
        System.out.println("This will record a " + type.getDisplayName().toLowerCase() + " transaction.");
        System.out.println("Description: " + type.getDescription());
        pauseForUser();
    }

    private void viewTransactionHistory() {
        System.out.println("\n--- Transaction History ---");
        System.out.println("Note: Transaction DAO not yet implemented.");
        System.out.println("This will show all transactions with filtering options.");
        pauseForUser();
    }

    // ===== REPORTS METHODS =====
    
    private void generateStockSummaryReport() {
        System.out.println("\n--- Stock Summary Report ---");
        
        try {
            List<Product> products = productService.findAllProducts();
            if (products.isEmpty()) {
                System.out.println("No products found.");
            } else {
                System.out.println("\nStock Summary for " + products.size() + " product(s):\n");
                
                BigDecimal totalStockValue = BigDecimal.ZERO;
                displayProductsTable(products);
                
                // Calculate total stock value
                for (Product product : products) {
                    BigDecimal productValue = product.getUnitPrice().multiply(new BigDecimal(product.getStockQuantity()));
                    totalStockValue = totalStockValue.add(productValue);
                }
                
                System.out.println("\n" + "=".repeat(100));
                System.out.printf("Total Stock Value: $%.2f%n", totalStockValue);
                System.out.println("Total Products: " + products.size());
            }
        } catch (SQLException e) {
            logger.error("Database error generating stock summary report", e);
            System.out.println("\n❌ Database error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error generating stock summary report", e);
            System.out.println("\n❌ Unexpected error: " + e.getMessage());
        }
        
        pauseForUser();
    }

    private void generateLowStockReport() {
        System.out.println("\n--- Low Stock Alert Report ---");
        
        try {
            List<Product> lowStockProducts = productService.getLowStockProducts();
            if (lowStockProducts.isEmpty()) {
                System.out.println("\n✓ No products are below reorder level.");
            } else {
                System.out.println("\n⚠️  Found " + lowStockProducts.size() + " product(s) below reorder level:\n");
                
                // Display low stock products with additional warning info
                System.out.printf("%-5s %-25s %-15s %-10s %-10s %-15s %-20s%n",
                    "ID", "Product Name", "Code", "Stock", "Reorder", "Shortage", "Supplier");
                System.out.println("=".repeat(100));
                
                for (Product product : lowStockProducts) {
                    int shortage = Math.max(0, product.getReorderLevel() - product.getStockQuantity());
                    String status = product.getStockQuantity() == 0 ? "OUT OF STOCK" : "LOW STOCK";
                    
                    System.out.printf("%-5d %-25s %-15s %-10d %-10d %-15s %-20s%n",
                        product.getProductId(),
                        truncateString(product.getProductName(), 24),
                        product.getProductCode() != null ? truncateString(product.getProductCode(), 14) : "N/A",
                        product.getStockQuantity(),
                        product.getReorderLevel(),
                        shortage > 0 ? shortage + " units" : status,
                        product.getSupplierName() != null ? truncateString(product.getSupplierName(), 19) : "N/A");
                }
                
                System.out.println("\n⚠️  Immediate action required for these products!");
            }
        } catch (SQLException e) {
            logger.error("Database error generating low stock report", e);
            System.out.println("\n❌ Database error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error generating low stock report", e);
            System.out.println("\n❌ Unexpected error: " + e.getMessage());
        }
        
        pauseForUser();
    }

    private void generateTransactionReport() {
        System.out.println("\n--- Transaction History Report ---");
        System.out.println("Note: Transaction DAO not yet implemented.");
        System.out.println("This will show detailed transaction history with filters.");
        pauseForUser();
    }

    private void generateSupplierReport() {
        System.out.println("\n--- Supplier Performance Report ---");
        try {
            List<Supplier> suppliers = supplierDAO.findAll();
            if (suppliers.isEmpty()) {
                System.out.println("No suppliers found.");
                return;
            }
            
            System.out.println("\n📊 SUPPLIER PERFORMANCE SUMMARY");
            System.out.println("=" + "=".repeat(80));
            System.out.printf("%-25s %-20s %-15s %-10s%n", "Company Name", "Contact Person", "Phone", "Rating");
            System.out.println("-".repeat(80));
            
            for (Supplier supplier : suppliers) {
                System.out.printf("%-25s %-20s %-15s ⭐ %.1f%n",
                    truncateString(supplier.getCompanyName(), 24),
                    truncateString(supplier.getContactPerson(), 19),
                    truncateString(supplier.getPhone(), 14),
                    supplier.getRating()
                );
            }
            
            // Calculate average rating
            double avgRating = suppliers.stream()
                .mapToDouble(s -> s.getRating().doubleValue())
                .average()
                .orElse(0.0);
            
            System.out.println("=" + "=".repeat(80));
            System.out.printf("Total Suppliers: %d | Average Rating: ⭐ %.2f%n", suppliers.size(), avgRating);
            
        } catch (SQLException e) {
            System.out.println("❌ Error generating supplier report: " + e.getMessage());
            logger.error("Error generating supplier report", e);
        }
        pauseForUser();
    }

    private void generateValuationReport() {
        System.out.println("\n--- Inventory Valuation Report ---");
        
        try {
            List<Product> products = productService.findAllProducts();
            if (products.isEmpty()) {
                System.out.println("No products found.");
            } else {
                System.out.println("\nInventory Valuation for " + products.size() + " product(s):\n");
                
                // Group by category and calculate values
                java.util.Map<String, java.util.List<Product>> productsByCategory = new java.util.HashMap<>();
                for (Product product : products) {
                    productsByCategory.computeIfAbsent(product.getCategory(), k -> new java.util.ArrayList<>()).add(product);
                }
                
                BigDecimal totalInventoryValue = BigDecimal.ZERO;
                
                System.out.printf("%-20s %-10s %-15s %-15s%n", "Category", "Items", "Total Units", "Total Value");
                System.out.println("=".repeat(65));
                
                for (java.util.Map.Entry<String, java.util.List<Product>> entry : productsByCategory.entrySet()) {
                    String category = entry.getKey();
                    java.util.List<Product> categoryProducts = entry.getValue();
                    
                    int totalUnits = 0;
                    BigDecimal categoryValue = BigDecimal.ZERO;
                    
                    for (Product product : categoryProducts) {
                        totalUnits += product.getStockQuantity();
                        BigDecimal productValue = product.getUnitPrice().multiply(new BigDecimal(product.getStockQuantity()));
                        categoryValue = categoryValue.add(productValue);
                    }
                    
                    totalInventoryValue = totalInventoryValue.add(categoryValue);
                    
                    System.out.printf("%-20s %-10d %-15d $%-14.2f%n",
                        truncateString(category, 19),
                        categoryProducts.size(),
                        totalUnits,
                        categoryValue);
                }
                
                System.out.println("=".repeat(65));
                System.out.printf("Total Inventory Value: $%.2f%n", totalInventoryValue);
                System.out.println("Total Product Types: " + products.size());
                System.out.println("Categories: " + productsByCategory.size());
            }
        } catch (SQLException e) {
            logger.error("Database error generating valuation report", e);
            System.out.println("\n❌ Database error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error generating valuation report", e);
            System.out.println("\n❌ Unexpected error: " + e.getMessage());
        }
        
        pauseForUser();
    }

    // ===== STOCK MANAGEMENT METHODS =====
    
    private void viewCurrentStock() {
        System.out.println("\n--- Current Stock Levels ---");
        
        try {
            List<Product> products = productService.findAllProducts();
            if (products.isEmpty()) {
                System.out.println("No products found.");
            } else {
                System.out.println("\nCurrent stock levels for " + products.size() + " product(s):\n");
                displayProductsTable(products);
                
                // Show summary statistics
                int totalProducts = products.size();
                long totalUnits = products.stream().mapToInt(Product::getStockQuantity).sum();
                long lowStockCount = products.stream().mapToInt(p -> p.getStockQuantity() <= p.getReorderLevel() ? 1 : 0).sum();
                long outOfStockCount = products.stream().mapToInt(p -> p.getStockQuantity() == 0 ? 1 : 0).sum();
                
                System.out.println("\n" + "=".repeat(115));
                System.out.println("Summary:");
                System.out.println("Total Products: " + totalProducts);
                System.out.println("Total Units in Stock: " + totalUnits);
                System.out.println("Low Stock Items: " + lowStockCount);
                System.out.println("Out of Stock Items: " + outOfStockCount);
                
                if (lowStockCount > 0) {
                    System.out.println("\n⚠️  Warning: " + lowStockCount + " product(s) need restocking!");
                }
                if (outOfStockCount > 0) {
                    System.out.println("❌ Critical: " + outOfStockCount + " product(s) are out of stock!");
                }
            }
        } catch (SQLException e) {
            logger.error("Database error viewing current stock", e);
            System.out.println("\n❌ Database error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error viewing current stock", e);
            System.out.println("\n❌ Unexpected error: " + e.getMessage());
        }
        
        pauseForUser();
    }

    private void checkLowStockAlerts() {
        System.out.println("\n--- Low Stock Alerts ---");
        
        try {
            List<Product> lowStockProducts = productService.getLowStockProducts();
            if (lowStockProducts.isEmpty()) {
                System.out.println("\n✓ No low stock alerts. All products are adequately stocked.");
            } else {
                System.out.println("\n⚠️  ALERT: " + lowStockProducts.size() + " product(s) require immediate attention!\n");
                
                // Separate critical (out of stock) from low stock
                java.util.List<Product> outOfStock = new java.util.ArrayList<>();
                java.util.List<Product> lowStock = new java.util.ArrayList<>();
                
                for (Product product : lowStockProducts) {
                    if (product.getStockQuantity() == 0) {
                        outOfStock.add(product);
                    } else {
                        lowStock.add(product);
                    }
                }
                
                if (!outOfStock.isEmpty()) {
                    System.out.println("❌ CRITICAL - OUT OF STOCK (" + outOfStock.size() + " items):");
                    System.out.println("-".repeat(60));
                    for (Product product : outOfStock) {
                        System.out.printf("• %s [Code: %s] - Supplier: %s\n",
                            product.getProductName(),
                            product.getProductCode() != null ? product.getProductCode() : "N/A",
                            product.getSupplierName() != null ? product.getSupplierName() : "N/A");
                    }
                    System.out.println();
                }
                
                if (!lowStock.isEmpty()) {
                    System.out.println("⚠️  LOW STOCK (" + lowStock.size() + " items):");
                    System.out.println("-".repeat(60));
                    for (Product product : lowStock) {
                        int shortage = product.getReorderLevel() - product.getStockQuantity();
                        System.out.printf("• %s [Code: %s] - Stock: %d, Reorder: %d (Need: %d) - Supplier: %s\n",
                            product.getProductName(),
                            product.getProductCode() != null ? product.getProductCode() : "N/A",
                            product.getStockQuantity(),
                            product.getReorderLevel(),
                            shortage,
                            product.getSupplierName() != null ? product.getSupplierName() : "N/A");
                    }
                }
                
                System.out.println("\nℹ️  Recommendation: Contact suppliers and place orders for the above products.");
            }
        } catch (SQLException e) {
            logger.error("Database error checking low stock alerts", e);
            System.out.println("\n❌ Database error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error checking low stock alerts", e);
            System.out.println("\n❌ Unexpected error: " + e.getMessage());
        }
        
        pauseForUser();
    }

    private void generateReorderList() {
        System.out.println("\n--- Generate Reorder List ---");
        
        try {
            List<Product> lowStockProducts = productService.getLowStockProducts();
            if (lowStockProducts.isEmpty()) {
                System.out.println("\n✓ No products need reordering at this time.");
            } else {
                System.out.println("\n📋 Reorder List for " + lowStockProducts.size() + " product(s):\n");
                
                // Group by supplier for easier ordering
                java.util.Map<String, java.util.List<Product>> productsBySupplier = new java.util.LinkedHashMap<>();
                for (Product product : lowStockProducts) {
                    String supplierName = product.getSupplierName() != null ? product.getSupplierName() : "Unknown Supplier";
                    productsBySupplier.computeIfAbsent(supplierName, k -> new java.util.ArrayList<>()).add(product);
                }
                
                BigDecimal totalOrderValue = BigDecimal.ZERO;
                
                for (java.util.Map.Entry<String, java.util.List<Product>> entry : productsBySupplier.entrySet()) {
                    String supplierName = entry.getKey();
                    java.util.List<Product> supplierProducts = entry.getValue();
                    
                    System.out.println("★ SUPPLIER: " + supplierName);
                    System.out.println("=".repeat(80));
                    System.out.printf("%-30s %-15s %-10s %-12s %-15s%n",
                        "Product Name", "Code", "Current", "Suggested", "Est. Cost");
                    System.out.println("-".repeat(80));
                    
                    BigDecimal supplierOrderValue = BigDecimal.ZERO;
                    
                    for (Product product : supplierProducts) {
                        // Calculate suggested order quantity (bring to 150% of reorder level)
                        int suggestedQuantity = Math.max(
                            product.getReorderLevel() - product.getStockQuantity(),
                            (int)(product.getReorderLevel() * 1.5) - product.getStockQuantity()
                        );
                        
                        BigDecimal estimatedCost = product.getUnitPrice().multiply(new BigDecimal(suggestedQuantity));
                        supplierOrderValue = supplierOrderValue.add(estimatedCost);
                        
                        System.out.printf("%-30s %-15s %-10d %-12d $%-14.2f%n",
                            truncateString(product.getProductName(), 29),
                            product.getProductCode() != null ? truncateString(product.getProductCode(), 14) : "N/A",
                            product.getStockQuantity(),
                            suggestedQuantity,
                            estimatedCost);
                    }
                    
                    System.out.printf("\nSubtotal for %s: $%.2f\n\n", supplierName, supplierOrderValue);
                    totalOrderValue = totalOrderValue.add(supplierOrderValue);
                }
                
                System.out.println("=".repeat(80));
                System.out.printf("TOTAL ESTIMATED ORDER VALUE: $%.2f%n", totalOrderValue);
                System.out.println("Total Products to Reorder: " + lowStockProducts.size());
                System.out.println("Suppliers to Contact: " + productsBySupplier.size());
                
                System.out.println("\nℹ️  Note: Suggested quantities bring stock to 150% of reorder level.");
                System.out.println("ℹ️  Adjust quantities based on supplier minimums and business needs.");
            }
        } catch (SQLException e) {
            logger.error("Database error generating reorder list", e);
            System.out.println("\n❌ Database error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error generating reorder list", e);
            System.out.println("\n❌ Unexpected error: " + e.getMessage());
        }
        
        pauseForUser();
    }

    private void performStockAdjustment() {
        System.out.println("\n--- Manual Stock Adjustment ---");
        System.out.println("Note: Product and Transaction DAO not yet implemented.");
        System.out.println("This will allow manual adjustment of stock quantities with audit trail.");
        pauseForUser();
    }

    private void performStockAudit() {
        System.out.println("\n--- Stock Audit ---");
        System.out.println("Note: Product DAO not yet implemented.");
        System.out.println("This will perform a comprehensive stock audit and variance analysis.");
        pauseForUser();
    }

    /**
     * Display products in a formatted table
     */
    private void displayProductsTable(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("No products to display.");
            return;
        }
        
        // Print header
        System.out.printf("%-5s %-25s %-15s %-15s %-10s %-10s %-10s %-20s%n",
            "ID", "Product Name", "Code", "Category", "Price", "Stock", "Reorder", "Supplier");
        System.out.println("=".repeat(115));
        
        // Print product data
        for (Product product : products) {
            System.out.printf("%-5d %-25s %-15s %-15s $%-9.2f %-10d %-10d %-20s%n",
                product.getProductId(),
                truncateString(product.getProductName(), 24),
                product.getProductCode() != null ? truncateString(product.getProductCode(), 14) : "N/A",
                truncateString(product.getCategory(), 14),
                product.getUnitPrice(),
                product.getStockQuantity(),
                product.getReorderLevel(),
                product.getSupplierName() != null ? truncateString(product.getSupplierName(), 19) : "N/A");
        }
    }
    
    /**
     * Truncate string to specified length for table display
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) return "N/A";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }

    private void cleanup() {
        if (scanner != null) {
            scanner.close();
        }
    }
}