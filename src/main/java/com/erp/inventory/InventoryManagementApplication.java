package com.erp.inventory;

import com.erp.inventory.ui.ConsoleUI;
import com.erp.inventory.util.DatabaseUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main application class for Inventory Management System
 * Entry point for the ERP Supply Chain Management Inventory Module
 */
public class InventoryManagementApplication {
    private static final Logger logger = LoggerFactory.getLogger(InventoryManagementApplication.class);
    
    public static void main(String[] args) {
        logger.info("Starting Inventory Management System...");
        
        try {
            // Test database connection
            if (!DatabaseUtil.testConnection()) {
                logger.error("Failed to connect to database. Please check your database configuration.");
                System.err.println("Database connection failed. Please check your configuration in application.properties");
                System.exit(1);
            }
            
            logger.info("Database connection successful");
            System.out.println("=".repeat(60));
            System.out.println("    INVENTORY MANAGEMENT SYSTEM");
            System.out.println("    ERP Supply Chain Management Module");
            System.out.println("    Version 1.0.0");
            System.out.println("=".repeat(60));
            
            // Start console UI
            ConsoleUI ui = new ConsoleUI();
            ui.start();
            
        } catch (Exception e) {
            logger.error("Application startup failed", e);
            System.err.println("Application failed to start: " + e.getMessage());
            System.exit(1);
        }
        
        logger.info("Inventory Management System shutdown completed");
    }
}