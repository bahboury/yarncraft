package com.swe2project.yarncraft.modules.inventory.repository;

import com.swe2project.yarncraft.modules.inventory.entity.InventoryItem;
import com.swe2project.yarncraft.modules.inventory.entity.InventoryItem.StockStatus;
import com.swe2project.yarncraft.modules.user.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryItem, Long> {

    // ==================== BASIC QUERIES ====================

    /**
     * Find inventory by product ID
     * Used when checking/updating stock for a specific product
     */
    Optional<InventoryItem> findByProductId(Long productId);

    /**
     * Find all inventory items for a specific vendor
     * Used when vendor wants to see their inventory dashboard
     */
    List<InventoryItem> findByVendor(User vendor);

    /**
     * Find inventory by vendor ID
     * Alternative way to get vendor's inventory
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.vendor.id = :vendorId")
    List<InventoryItem> findByVendorId(@Param("vendorId") Long vendorId);

    /**
     * Find all active inventory items
     * Used for displaying available products to customers
     */
    List<InventoryItem> findByIsActiveTrue();

    /**
     * Find inventory by status
     * Used for filtering (e.g., show only IN_STOCK items)
     */
    List<InventoryItem> findByStatus(StockStatus status);

    /**
     * Find inventory by category
     * Used for browsing by product type (bags, purses, etc.)
     */
    List<InventoryItem> findByProductCategory(String category);

    // ==================== ADVANCED QUERIES ====================

    /**
     * Find items with low stock (at or below reorder level)
     * Used for alerting vendors to restock
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.stockQuantity <= i.reorderLevel AND i.isActive = true")
    List<InventoryItem> findLowStockItems();

    /**
     * Find low stock items for a specific vendor
     * Used in vendor dashboard to show what needs restocking
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.vendor.id = :vendorId " +
            "AND i.stockQuantity <= i.reorderLevel AND i.isActive = true")
    List<InventoryItem> findLowStockItemsByVendor(@Param("vendorId") Long vendorId);

    /**
     * Find out of stock items
     * Used for reporting and alerting
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.stockQuantity = 0 AND i.isActive = true")
    List<InventoryItem> findOutOfStockItems();

    /**
     * Check if product has sufficient stock available
     * Used before adding items to cart or processing orders
     */
    @Query("SELECT CASE WHEN (i.stockQuantity - i.reservedQuantity) >= :quantity " +
            "THEN true ELSE false END " +
            "FROM InventoryItem i WHERE i.productId = :productId")
    boolean hasAvailableStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Get available stock for a product
     * Returns actual available quantity (stock - reserved)
     */
    @Query("SELECT (i.stockQuantity - i.reservedQuantity) " +
            "FROM InventoryItem i WHERE i.productId = :productId")
    Integer getAvailableStock(@Param("productId") Long productId);

    // ==================== STOCK MODIFICATION QUERIES ====================

    /**
     * Decrease stock quantity (when order is placed)
     *
     * @Modifying means this query changes data
     */
    @Modifying
    @Query("UPDATE InventoryItem i SET i.stockQuantity = i.stockQuantity - :quantity, " +
            "i.soldQuantity = i.soldQuantity + :quantity, " +
            "i.lastSoldAt = CURRENT_TIMESTAMP, " +
            "i.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE i.productId = :productId AND i.stockQuantity >= :quantity")
    int decreaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Increase stock quantity (when restocking or order cancelled)
     */
    @Modifying
    @Query("UPDATE InventoryItem i SET i.stockQuantity = i.stockQuantity + :quantity, " +
            "i.lastRestockedAt = CURRENT_TIMESTAMP, " +
            "i.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE i.productId = :productId")
    int increaseStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Reserve stock (when item added to cart)
     * Moves stock from available to reserved
     */
    @Modifying
    @Query("UPDATE InventoryItem i SET i.stockQuantity = i.stockQuantity - :quantity, " +
            "i.reservedQuantity = i.reservedQuantity + :quantity, " +
            "i.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE i.productId = :productId AND (i.stockQuantity - i.reservedQuantity) >= :quantity")
    int reserveStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Release reserved stock (when cart item removed or cart expires)
     * Moves stock from reserved back to available
     */
    @Modifying
    @Query("UPDATE InventoryItem i SET i.stockQuantity = i.stockQuantity + :quantity, " +
            "i.reservedQuantity = i.reservedQuantity - :quantity, " +
            "i.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE i.productId = :productId AND i.reservedQuantity >= :quantity")
    int releaseReservedStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    /**
     * Confirm reservation (when order is completed)
     * Moves from reserved to sold
     */
    @Modifying
    @Query("UPDATE InventoryItem i SET i.reservedQuantity = i.reservedQuantity - :quantity, " +
            "i.soldQuantity = i.soldQuantity + :quantity, " +
            "i.lastSoldAt = CURRENT_TIMESTAMP, " +
            "i.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE i.productId = :productId AND i.reservedQuantity >= :quantity")
    int confirmReservation(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    // ==================== ANALYTICS QUERIES ====================

    /**
     * Get total inventory value for a vendor
     * Calculates: SUM(stockQuantity * unitCost)
     */
    @Query("SELECT SUM(i.stockQuantity * i.unitCost) " +
            "FROM InventoryItem i WHERE i.vendor.id = :vendorId AND i.isActive = true")
    Double getTotalInventoryValueByVendor(@Param("vendorId") Long vendorId);

    /**
     * Get count of active products by vendor
     */
    @Query("SELECT COUNT(i) FROM InventoryItem i WHERE i.vendor.id = :vendorId AND i.isActive = true")
    Long countActiveProductsByVendor(@Param("vendorId") Long vendorId);

    /**
     * Find top selling products (by sold quantity)
     * Used for analytics dashboard
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.isActive = true ORDER BY i.soldQuantity DESC")
    List<InventoryItem> findTopSellingProducts();

    /**
     * Find products that need approval before restocking
     */
    @Query("SELECT i FROM InventoryItem i WHERE i.requiresApproval = true AND i.isActive = true")
    List<InventoryItem> findItemsRequiringApproval();

    // ==================== SEARCH QUERIES ====================

    /**
     * Search inventory by product name (case-insensitive)
     * Used for search functionality
     */
    @Query("SELECT i FROM InventoryItem i WHERE LOWER(i.productName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "AND i.isActive = true")
    List<InventoryItem> searchByProductName(@Param("searchTerm") String searchTerm);

    /**
     * Search inventory with multiple filters
     * Can search by name, category, and vendor
     */
    @Query("SELECT i FROM InventoryItem i WHERE " +
            "(:productName IS NULL OR LOWER(i.productName) LIKE LOWER(CONCAT('%', :productName, '%'))) AND " +
            "(:category IS NULL OR i.productCategory = :category) AND " +
            "(:vendorId IS NULL OR i.vendor.id = :vendorId) AND " +
            "i.isActive = true")
    List<InventoryItem> searchInventory(
            @Param("productName") String productName,
            @Param("category") String category,
            @Param("vendorId") Long vendorId
    );
}
