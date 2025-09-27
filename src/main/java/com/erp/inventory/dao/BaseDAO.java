package com.erp.inventory.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Generic DAO interface providing common CRUD operations
 * @param <T> Entity type
 * @param <ID> Primary key type
 */
public interface BaseDAO<T, ID> {
    
    /**
     * Save a new entity
     * @param entity Entity to save
     * @return Saved entity with generated ID
     * @throws SQLException if database operation fails
     */
    T save(T entity) throws SQLException;
    
    /**
     * Update an existing entity
     * @param entity Entity to update
     * @return Updated entity
     * @throws SQLException if database operation fails
     */
    T update(T entity) throws SQLException;
    
    /**
     * Find entity by ID
     * @param id Primary key
     * @return Optional containing entity if found, empty otherwise
     * @throws SQLException if database operation fails
     */
    Optional<T> findById(ID id) throws SQLException;
    
    /**
     * Find all active entities
     * @return List of all active entities
     * @throws SQLException if database operation fails
     */
    List<T> findAll() throws SQLException;
    
    /**
     * Soft delete entity by ID
     * @param id Primary key
     * @return true if deleted successfully, false otherwise
     * @throws SQLException if database operation fails
     */
    boolean delete(ID id) throws SQLException;
    
    /**
     * Check if entity exists by ID
     * @param id Primary key
     * @return true if entity exists, false otherwise
     * @throws SQLException if database operation fails
     */
    boolean exists(ID id) throws SQLException;
    
    /**
     * Count total number of active entities
     * @return Number of active entities
     * @throws SQLException if database operation fails
     */
    long count() throws SQLException;
}