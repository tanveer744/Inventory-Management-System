package com.erp.inventory.ui;

import com.erp.inventory.dao.SupplierDAO;
import com.erp.inventory.dao.impl.SupplierDAOImpl;
import com.erp.inventory.model.Supplier;
import com.erp.inventory.model.Product;
import com.erp.inventory.model.Transaction;
import com.erp.inventory.model.TransactionType;
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
    
    // DAO instances
    private final SupplierDAO supplierDAO;

    public ConsoleUI() {
        this.scanner = new Scanner(System.in);
        this.running = true;
        this.supplierDAO = new SupplierDAOImpl();
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
    
    private String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
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
        System.out.println("Note: Product DAO not yet implemented.");
        System.out.println("This will add a product with supplier linkage.");
        pauseForUser();
    }

    private void viewAllProducts() {
        System.out.println("\n--- All Products ---");
        System.out.println("Note: Product DAO not yet implemented.");
        System.out.println("This will show all products with supplier information.");
        pauseForUser();
    }

    private void searchProducts() {
        System.out.println("\n--- Search Products ---");
        System.out.println("Note: Product DAO not yet implemented.");
        System.out.println("This will search products by name, code, or category.");
        pauseForUser();
    }

    private void updateProduct() {
        System.out.println("\n--- Update Product ---");
        System.out.println("Note: Product DAO not yet implemented.");
        System.out.println("This will update product information and pricing.");
        pauseForUser();
    }

    private void deleteProduct() {
        System.out.println("\n--- Delete Product ---");
        System.out.println("Note: Product DAO not yet implemented.");
        System.out.println("This will safely delete products (soft delete).");
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
        System.out.println("Note: Product DAO not yet implemented.");
        System.out.println("This will show current stock levels and values for all products.");
        pauseForUser();
    }

    private void generateLowStockReport() {
        System.out.println("\n--- Low Stock Alert Report ---");
        System.out.println("Note: Product DAO not yet implemented.");
        System.out.println("This will show products that are below reorder level.");
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
        System.out.println("Note: Product DAO not yet implemented.");
        System.out.println("This will calculate total inventory value and breakdown by category.");
        pauseForUser();
    }

    // ===== STOCK MANAGEMENT METHODS =====
    
    private void viewCurrentStock() {
        System.out.println("\n--- Current Stock Levels ---");
        System.out.println("Note: Product DAO not yet implemented.");
        System.out.println("This will show current stock quantities for all products.");
        pauseForUser();
    }

    private void checkLowStockAlerts() {
        System.out.println("\n--- Low Stock Alerts ---");
        System.out.println("Note: Product DAO not yet implemented.");
        System.out.println("This will identify products that need immediate restocking.");
        pauseForUser();
    }

    private void generateReorderList() {
        System.out.println("\n--- Generate Reorder List ---");
        System.out.println("Note: Product DAO not yet implemented.");
        System.out.println("This will create a purchase order list for low stock items.");
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

    private void cleanup() {
        if (scanner != null) {
            scanner.close();
        }
    }
}