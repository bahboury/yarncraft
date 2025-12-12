package com.swe2project.yarncraft.modules.inventory.service;

import com.swe2project.yarncraft.modules.inventory.entity.InventoryItem;
import com.swe2project.yarncraft.modules.inventory.entity.InventoryItem.StockStatus;
import com.swe2project.yarncraft.modules.inventory.repository.InventoryRepository;
import com.swe2project.yarncraft.modules.user.entity.User;
import com.swe2project.yarncraft.modules.user.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;

    // ==================== CREATE & UPDATE OPERATIONS ====================

    /**
     * Create new inventory item
     * Only Admin or Approved Vendor can create inventory
     */
    @Transactional
    public InventoryItem createInventory(InventoryItem inventoryItem, User currentUser) {
        log.info("Creating inventory for product: {} by user: {}",
                inventoryItem.getProductId(), currentUser.getEmail());

        // Validation: Check if user can create inventory
        if (!currentUser.canManageInventory()) {
            log.error("User {} does not have permission to create inventory", currentUser.getEmail());
            throw new SecurityException("You don't have permission to create inventory");
        }

        // Validation: Check if product ID already exists
        if (inventoryRepository.findByProductId(inventoryItem.getProductId()).isPresent()) {
            log.error("Inventory already exists for product: {}", inventoryItem.getProductId());
            throw new IllegalArgumentException("Inventory already exists for this product");
        }

        // For vendors, set them as the owner
        if (currentUser.isVendor()) {
            inventoryItem.setVendor(currentUser);
        } else if (currentUser.isAdmin() && inventoryItem.getVendor() == null) {
            // Admin must specify a vendor
            throw new IllegalArgumentException("Admin must specify a vendor for the inventory");
        }

        // Set audit fields
        inventoryItem.setCreatedBy(currentUser);
        inventoryItem.setLastModifiedBy(currentUser);

        // Set initial status
        inventoryItem.updateStockStatus();

        // Save and return
        InventoryItem saved = inventoryRepository.save(inventoryItem);
        log.info("Successfully created inventory with ID: {}", saved.getId());

        return saved;
    }

    /**
     * Update existing inventory item
     * Only owner vendor or admin can update
     */
    @Transactional
    public InventoryItem updateInventory(Long id, InventoryItem updatedItem, User currentUser) {
        log.info("Updating inventory ID: {} by user: {}", id, currentUser.getEmail());

        // Find existing inventory
        InventoryItem existing = getInventoryById(id);

        // Permission check
        if (!existing.canBeModifiedBy(currentUser)) {
            log.error("User {} does not have permission to update inventory ID: {}",
                    currentUser.getEmail(), id);
            throw new SecurityException("You don't have permission to modify this inventory");
        }

        // Update fields (only non-null values)
        if (updatedItem.getProductName() != null) {
            existing.setProductName(updatedItem.getProductName());
        }
        if (updatedItem.getProductSku() != null) {
            existing.setProductSku(updatedItem.getProductSku());
        }
        if (updatedItem.getProductCategory() != null) {
            existing.setProductCategory(updatedItem.getProductCategory());
        }
        if (updatedItem.getReorderLevel() != null) {
            existing.setReorderLevel(updatedItem.getReorderLevel());
        }
        if (updatedItem.getMaxStockLevel() != null) {
            existing.setMaxStockLevel(updatedItem.getMaxStockLevel());
        }
        if (updatedItem.getUnitCost() != null) {
            existing.setUnitCost(updatedItem.getUnitCost());
        }
        if (updatedItem.getUnitPrice() != null) {
            existing.setUnitPrice(updatedItem.getUnitPrice());
        }
        if (updatedItem.getWarehouseLocation() != null) {
            existing.setWarehouseLocation(updatedItem.getWarehouseLocation());
        }
        if (updatedItem.getDescription() != null) {
            existing.setDescription(updatedItem.getDescription());
        }
        if (updatedItem.getNotes() != null) {
            existing.setNotes(updatedItem.getNotes());
        }
        if (updatedItem.getIsActive() != null) {
            existing.setIsActive(updatedItem.getIsActive());
        }

        // Update audit fields
        existing.setLastModifiedBy(currentUser);
        existing.updateStockStatus();

        InventoryItem saved = inventoryRepository.save(existing);
        log.info("Successfully updated inventory ID: {}", id);

        return saved;
    }

    // ==================== READ OPERATIONS ====================

    /**
     * Get inventory by ID
     */
    public InventoryItem getInventoryById(Long id) {
        log.info("Fetching inventory by ID: {}", id);
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory not found with ID: " + id));
    }

    /**
     * Get inventory by product ID
     */
    public InventoryItem getInventoryByProductId(Long productId) {
        log.info("Fetching inventory for product: {}", productId);
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
    }

    /**
     * Get all inventory items
     * Admin sees all, vendors see only their own
     */
    public List<InventoryItem> getAllInventory(User currentUser) {
        log.info("Fetching all inventory for user: {}", currentUser.getEmail());

        if (currentUser.isAdmin()) {
            // Admin sees everything
            return inventoryRepository.findAll();
        } else if (currentUser.isApprovedVendor()) {
            // Vendor sees only their inventory
            return inventoryRepository.findByVendor(currentUser);
        } else {
            // Customers see only active items
            return inventoryRepository.findByIsActiveTrue();
        }
    }

    /**
     * Get inventory for a specific vendor
     * Admin can see any vendor's inventory
     * Vendor can only see their own
     */
    public List<InventoryItem> getVendorInventory(Long vendorId, User currentUser) {
        log.info("Fetching inventory for vendor: {} by user: {}", vendorId, currentUser.getEmail());

        // Permission check
        if (!currentUser.isAdmin() && !currentUser.getId().equals(vendorId)) {
            log.error("User {} attempted to access vendor {} inventory",
                    currentUser.getEmail(), vendorId);
            throw new SecurityException("You can only view your own inventory");
        }

        return inventoryRepository.findByVendorId(vendorId);
    }

    /**
     * Get active inventory items (for customer browsing)
     */
    public List<InventoryItem> getActiveInventory() {
        log.info("Fetching all active inventory items");
        return inventoryRepository.findByIsActiveTrue();
    }

    /**
     * Get inventory by category
     */
    public List<InventoryItem> getInventoryByCategory(String category) {
        log.info("Fetching inventory for category: {}", category);
        return inventoryRepository.findByProductCategory(category);
    }

    /**
     * Search inventory by product name
     */
    public List<InventoryItem> searchInventory(String searchTerm) {
        log.info("Searching inventory with term: {}", searchTerm);
        return inventoryRepository.searchByProductName(searchTerm);
    }

    /**
     * Advanced search with multiple filters
     */
    public List<InventoryItem> searchInventoryAdvanced(
            String productName,
            String category,
            Long vendorId,
            User currentUser) {

        log.info("Advanced search - Name: {}, Category: {}, VendorId: {}",
                productName, category, vendorId);

        // If not admin and searching for specific vendor, must be own inventory
        if (vendorId != null && !currentUser.isAdmin() && !currentUser.getId().equals(vendorId)) {
            throw new SecurityException("You can only search your own inventory");
        }

        return inventoryRepository.searchInventory(productName, category, vendorId);
    }

    // ==================== STOCK MANAGEMENT OPERATIONS ====================

    /**
     * Check if product has sufficient stock
     */
    public boolean checkStock(Long productId, Integer quantity) {
        log.info("Checking stock for product {} quantity {}", productId, quantity);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        return inventoryRepository.hasAvailableStock(productId, quantity);
    }

    /**
     * Get available stock quantity
     */
    public Integer getAvailableStock(Long productId) {
        log.info("Getting available stock for product: {}", productId);
        Integer stock = inventoryRepository.getAvailableStock(productId);
        return stock != null ? stock : 0;
    }

    /**
     * Reserve stock (when item added to cart)
     */
    @Transactional
    public void reserveStock(Long productId, Integer quantity) {
        log.info("Reserving {} units for product {}", quantity, productId);

        // Validation
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // Check if stock is available
        if (!checkStock(productId, quantity)) {
            throw new IllegalStateException("Insufficient stock available for product: " + productId);
        }

        // Reserve the stock
        int updated = inventoryRepository.reserveStock(productId, quantity);

        if (updated == 0) {
            throw new IllegalStateException("Failed to reserve stock. Product may not exist or insufficient stock.");
        }

        log.info("Successfully reserved {} units for product {}", quantity, productId);
    }

    /**
     * Release reserved stock (when cart item removed or cart expires)
     */
    @Transactional
    public void releaseReservedStock(Long productId, Integer quantity) {
        log.info("Releasing {} reserved units for product {}", quantity, productId);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        int updated = inventoryRepository.releaseReservedStock(productId, quantity);

        if (updated == 0) {
            log.warn("Failed to release reserved stock for product {}. May not have enough reserved.", productId);
        }

        log.info("Successfully released {} reserved units for product {}", quantity, productId);
    }

    /**
     * Confirm reservation (when order is completed)
     * Moves stock from reserved to sold
     */
    @Transactional
    public void confirmReservation(Long productId, Integer quantity) {
        log.info("Confirming reservation of {} units for product {}", quantity, productId);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        int updated = inventoryRepository.confirmReservation(productId, quantity);

        if (updated == 0) {
            throw new IllegalStateException("Failed to confirm reservation. Insufficient reserved stock.");
        }

        log.info("Successfully confirmed reservation for product {}", productId);
    }

    /**
     * Decrease stock directly (alternative to reserve + confirm flow)
     */
    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        log.info("Decreasing stock for product {} by {}", productId, quantity);

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        int updated = inventoryRepository.decreaseStock(productId, quantity);

        if (updated == 0) {
            throw new IllegalStateException("Failed to decrease stock. Insufficient quantity available.");
        }

        log.info("Successfully decreased stock for product {}", productId);
    }

    /**
     * Restock inventory (vendor adds more stock)
     */
    @Transactional
    public InventoryItem restockInventory(Long productId, Integer quantity, User currentUser) {
        log.info("Restocking product {} with {} units by user {}",
                productId, quantity, currentUser.getEmail());

        // Validation
        if (quantity <= 0) {
            throw new IllegalArgumentException("Restock quantity must be positive");
        }

        // Get inventory item
        InventoryItem inventory = getInventoryByProductId(productId);

        // Permission check
        if (!inventory.canBeModifiedBy(currentUser)) {
            throw new SecurityException("You don't have permission to restock this inventory");
        }

        // Check if approval is required
        if (inventory.getRequiresApproval() && !currentUser.isAdmin()) {
            throw new IllegalStateException("This inventory requires admin approval for restocking");
        }

        // Increase stock
        inventoryRepository.increaseStock(productId, quantity);

        // Update timestamps and audit
        inventory.setLastRestockedAt(LocalDateTime.now());
        inventory.setLastModifiedBy(currentUser);
        inventory.updateStockStatus();

        InventoryItem updated = inventoryRepository.save(inventory);
        log.info("Successfully restocked product {} with {} units", productId, quantity);

        return updated;
    }

    /**
     * Adjust stock manually (admin only - for corrections)
     */
    @Transactional
    public InventoryItem adjustStock(Long productId, Integer newQuantity, String reason, User currentUser) {
        log.info("Admin adjusting stock for product {} to {} - Reason: {}",
                productId, newQuantity, reason);

        // Only admin can do manual adjustments
        if (!currentUser.isAdmin()) {
            throw new SecurityException("Only admins can manually adjust stock");
        }

        if (newQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        InventoryItem inventory = getInventoryByProductId(productId);
        inventory.setStockQuantity(newQuantity);
        inventory.setLastModifiedBy(currentUser);
        inventory.setNotes((inventory.getNotes() != null ? inventory.getNotes() + "\n" : "") +
                "Stock adjusted by admin: " + reason + " at " + LocalDateTime.now());
        inventory.updateStockStatus();

        InventoryItem updated = inventoryRepository.save(inventory);
        log.info("Successfully adjusted stock for product {}", productId);

        return updated;
    }

    // ==================== STOCK ALERTS & MONITORING ====================

    /**
     * Get all low stock items
     */
    public List<InventoryItem> getLowStockItems(User currentUser) {
        log.info("Fetching low stock items for user: {}", currentUser.getEmail());

        List<InventoryItem> lowStockItems = inventoryRepository.findLowStockItems();

        // Filter based on user role
        if (currentUser.isAdmin()) {
            return lowStockItems;
        } else if (currentUser.isVendor()) {
            return lowStockItems.stream()
                    .filter(item -> item.getVendor().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        }

        return List.of(); // Customers don't see low stock alerts
    }

    /**
     * Get out of stock items
     */
    public List<InventoryItem> getOutOfStockItems(User currentUser) {
        log.info("Fetching out of stock items for user: {}", currentUser.getEmail());

        List<InventoryItem> outOfStockItems = inventoryRepository.findOutOfStockItems();

        // Filter based on user role
        if (currentUser.isAdmin()) {
            return outOfStockItems;
        } else if (currentUser.isVendor()) {
            return outOfStockItems.stream()
                    .filter(item -> item.getVendor().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        }

        return List.of();
    }

    /**
     * Get items by status
     */
    public List<InventoryItem> getInventoryByStatus(StockStatus status, User currentUser) {
        log.info("Fetching inventory with status: {} for user: {}", status, currentUser.getEmail());

        List<InventoryItem> items = inventoryRepository.findByStatus(status);

        // Filter based on user role
        if (currentUser.isAdmin()) {
            return items;
        } else if (currentUser.isVendor()) {
            return items.stream()
                    .filter(item -> item.getVendor().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        } else {
            // Customers only see active items
            return items.stream()
                    .filter(InventoryItem::getIsActive)
                    .collect(Collectors.toList());
        }
    }

    // ==================== ANALYTICS & REPORTING ====================

    /**
     * Get total inventory value for a vendor
     */
    public Double getTotalInventoryValue(Long vendorId, User currentUser) {
        log.info("Calculating total inventory value for vendor: {}", vendorId);

        // Permission check
        if (!currentUser.isAdmin() && !currentUser.getId().equals(vendorId)) {
            throw new SecurityException("You can only view your own inventory value");
        }

        Double value = inventoryRepository.getTotalInventoryValueByVendor(vendorId);
        return value != null ? value : 0.0;
    }

    /**
     * Get count of active products by vendor
     */
    public Long countActiveProducts(Long vendorId, User currentUser) {
        log.info("Counting active products for vendor: {}", vendorId);

        // Permission check
        if (!currentUser.isAdmin() && !currentUser.getId().equals(vendorId)) {
            throw new SecurityException("You can only view your own product count");
        }

        return inventoryRepository.countActiveProductsByVendor(vendorId);
    }

    /**
     * Get top selling products
     */
    public List<InventoryItem> getTopSellingProducts(User currentUser) {
        log.info("Fetching top selling products");

        List<InventoryItem> topProducts = inventoryRepository.findTopSellingProducts();

        // Admin sees all, vendors see only their own
        if (currentUser.isAdmin()) {
            return topProducts;
        } else if (currentUser.isVendor()) {
            return topProducts.stream()
                    .filter(item -> item.getVendor().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        }

        // Customers see all active products
        return topProducts.stream()
                .filter(InventoryItem::getIsActive)
                .collect(Collectors.toList());
    }

    /**
     * Get dashboard statistics for vendor
     */
    public VendorDashboardStats getVendorDashboard(Long vendorId, User currentUser) {
        log.info("Generating dashboard stats for vendor: {}", vendorId);

        // Permission check
        if (!currentUser.isAdmin() && !currentUser.getId().equals(vendorId)) {
            throw new SecurityException("You can only view your own dashboard");
        }

        List<InventoryItem> vendorInventory = inventoryRepository.findByVendorId(vendorId);

        long totalProducts = vendorInventory.size();
        long activeProducts = vendorInventory.stream().filter(InventoryItem::getIsActive).count();
        long lowStockCount = vendorInventory.stream().filter(InventoryItem::needsReorder).count();
        long outOfStockCount = vendorInventory.stream()
                .filter(item -> item.getStockQuantity() == 0)
                .count();

        int totalStock = vendorInventory.stream()
                .mapToInt(InventoryItem::getStockQuantity)
                .sum();

        int totalReserved = vendorInventory.stream()
                .mapToInt(InventoryItem::getReservedQuantity)
                .sum();

        int totalSold = vendorInventory.stream()
                .mapToInt(InventoryItem::getSoldQuantity)
                .sum();

        BigDecimal totalValue = vendorInventory.stream()
                .map(InventoryItem::getTotalInventoryValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal potentialRevenue = vendorInventory.stream()
                .map(InventoryItem::getPotentialRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return VendorDashboardStats.builder()
                .vendorId(vendorId)
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .lowStockCount(lowStockCount)
                .outOfStockCount(outOfStockCount)
                .totalStock(totalStock)
                .totalReserved(totalReserved)
                .totalSold(totalSold)
                .totalInventoryValue(totalValue)
                .potentialRevenue(potentialRevenue)
                .build();
    }

    // ==================== DELETE OPERATIONS ====================

    /**
     * Soft delete - deactivate inventory item
     * Only owner or admin can delete
     */
    @Transactional
    public void deactivateInventory(Long id, User currentUser) {
        log.info("Deactivating inventory ID: {} by user: {}", id, currentUser.getEmail());

        InventoryItem inventory = getInventoryById(id);

        // Permission check
        if (!inventory.canBeModifiedBy(currentUser)) {
            throw new SecurityException("You don't have permission to deactivate this inventory");
        }

        inventory.setIsActive(false);
        inventory.setStatus(StockStatus.DISCONTINUED);
        inventory.setLastModifiedBy(currentUser);

        inventoryRepository.save(inventory);
        log.info("Successfully deactivated inventory ID: {}", id);
    }

    /**
     * Reactivate inventory item
     */
    @Transactional
    public void reactivateInventory(Long id, User currentUser) {
        log.info("Reactivating inventory ID: {} by user: {}", id, currentUser.getEmail());

        InventoryItem inventory = getInventoryById(id);

        // Permission check
        if (!inventory.canBeModifiedBy(currentUser)) {
            throw new SecurityException("You don't have permission to reactivate this inventory");
        }

        inventory.setIsActive(true);
        inventory.updateStockStatus();
        inventory.setLastModifiedBy(currentUser);

        inventoryRepository.save(inventory);
        log.info("Successfully reactivated inventory ID: {}", id);
    }

    /**
     * Hard delete - permanently remove inventory (admin only)
     */
    @Transactional
    public void deleteInventory(Long id, User currentUser) {
        log.info("Permanently deleting inventory ID: {} by user: {}", id, currentUser.getEmail());

        // Only admin can hard delete
        if (!currentUser.isAdmin()) {
            throw new SecurityException("Only admins can permanently delete inventory");
        }

        InventoryItem inventory = getInventoryById(id);

        // Check if item has reserved or sold quantity
        if (inventory.getReservedQuantity() > 0) {
            throw new IllegalStateException("Cannot delete inventory with reserved stock");
        }

        inventoryRepository.deleteById(id);
        log.info("Successfully deleted inventory ID: {}", id);
    }

    // ==================== HELPER CLASSES ====================

    /**
     * DTO for vendor dashboard statistics
     */
    @lombok.Data
    @lombok.Builder
    public static class VendorDashboardStats {
        private Long vendorId;
        private Long totalProducts;
        private Long activeProducts;
        private Long lowStockCount;
        private Long outOfStockCount;
        private Integer totalStock;
        private Integer totalReserved;
        private Integer totalSold;
        private BigDecimal totalInventoryValue;
        private BigDecimal potentialRevenue;
    }
}
