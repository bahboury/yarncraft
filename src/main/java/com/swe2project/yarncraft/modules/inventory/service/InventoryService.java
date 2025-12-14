package com.swe2project.yarncraft.modules.inventory.service;

import com.swe2project.yarncraft.common.exception.ResourceNotFoundException;
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

    // ========================================================================
    // 1. CREATE & UPDATE OPERATIONS
    // ========================================================================

    /**
     * Create new inventory item
     * Only Admin or Approved Vendor can create inventory
     */
    @Transactional
    public InventoryItem createInventory(InventoryItem inventoryItem, User currentUser) {
        log.info("Creating inventory for product: {} by user: {}", inventoryItem.getProductId(), currentUser.getEmail());

        if (!currentUser.canManageInventory()) {
            throw new SecurityException("You don't have permission to create inventory");
        }

        if (inventoryRepository.findByProductId(inventoryItem.getProductId()).isPresent()) {
            throw new IllegalArgumentException("Inventory already exists for this product");
        }

        if (currentUser.isVendor()) {
            inventoryItem.setVendor(currentUser);
        } else if (currentUser.isAdmin() && inventoryItem.getVendor() == null) {
            throw new IllegalArgumentException("Admin must specify a vendor for the inventory");
        }

        inventoryItem.setCreatedBy(currentUser);
        inventoryItem.setLastModifiedBy(currentUser);
        inventoryItem.updateStockStatus();

        return inventoryRepository.save(inventoryItem);
    }

    /**
     * Update existing inventory item
     */
    @Transactional
    public InventoryItem updateInventory(Long id, InventoryItem updatedItem, User currentUser) {
        log.info("Updating inventory ID: {} by user: {}", id, currentUser.getEmail());

        InventoryItem existing = getInventoryById(id);

        if (!existing.canBeModifiedBy(currentUser)) {
            throw new SecurityException("You don't have permission to modify this inventory");
        }

        // Update fields (only non-null values)
        if (updatedItem.getProductName() != null) existing.setProductName(updatedItem.getProductName());
        if (updatedItem.getProductSku() != null) existing.setProductSku(updatedItem.getProductSku());
        if (updatedItem.getProductCategory() != null) existing.setProductCategory(updatedItem.getProductCategory());
        if (updatedItem.getReorderLevel() != null) existing.setReorderLevel(updatedItem.getReorderLevel());
        if (updatedItem.getMaxStockLevel() != null) existing.setMaxStockLevel(updatedItem.getMaxStockLevel());
        if (updatedItem.getUnitCost() != null) existing.setUnitCost(updatedItem.getUnitCost());
        if (updatedItem.getUnitPrice() != null) existing.setUnitPrice(updatedItem.getUnitPrice());
        if (updatedItem.getWarehouseLocation() != null)
            existing.setWarehouseLocation(updatedItem.getWarehouseLocation());
        if (updatedItem.getDescription() != null) existing.setDescription(updatedItem.getDescription());
        if (updatedItem.getNotes() != null) existing.setNotes(updatedItem.getNotes());
        if (updatedItem.getIsActive() != null) existing.setIsActive(updatedItem.getIsActive());

        existing.setLastModifiedBy(currentUser);
        existing.updateStockStatus();

        return inventoryRepository.save(existing);
    }

    // ========================================================================
    // 2. READ OPERATIONS
    // ========================================================================

    public InventoryItem getInventoryById(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory not found with ID: " + id));
    }

    public InventoryItem getInventoryByProductId(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found for product: " + productId));
    }

    public List<InventoryItem> getAllInventory(User currentUser) {
        if (currentUser.isAdmin()) {
            return inventoryRepository.findAll();
        } else if (currentUser.isApprovedVendor()) {
            return inventoryRepository.findByVendor(currentUser);
        } else {
            return inventoryRepository.findByIsActiveTrue();
        }
    }

    public List<InventoryItem> getVendorInventory(Long vendorId, User currentUser) {
        if (!currentUser.isAdmin() && !currentUser.getId().equals(vendorId)) {
            throw new SecurityException("You can only view your own inventory");
        }
        return inventoryRepository.findByVendorId(vendorId);
    }

    public List<InventoryItem> getActiveInventory() {
        return inventoryRepository.findByIsActiveTrue();
    }

    public List<InventoryItem> getInventoryByCategory(String category) {
        return inventoryRepository.findByProductCategory(category);
    }

    public List<InventoryItem> searchInventory(String searchTerm) {
        return inventoryRepository.searchByProductName(searchTerm);
    }

    public List<InventoryItem> searchInventoryAdvanced(String productName, String category, Long vendorId, User currentUser) {
        if (vendorId != null && !currentUser.isAdmin() && !currentUser.getId().equals(vendorId)) {
            throw new SecurityException("You can only search your own inventory");
        }
        return inventoryRepository.searchInventory(productName, category, vendorId);
    }

    // ========================================================================
    // 3. STOCK MANAGEMENT OPERATIONS
    // ========================================================================

    public boolean checkStock(Long productId, Integer quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        return inventoryRepository.hasAvailableStock(productId, quantity);
    }

    public Integer getAvailableStock(Long productId) {
        Integer stock = inventoryRepository.getAvailableStock(productId);
        return stock != null ? stock : 0;
    }

    @Transactional
    public void reserveStock(Long productId, Integer quantity) {
        log.info("Reserving {} units for product {}", quantity, productId);
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        if (!checkStock(productId, quantity)) {
            throw new IllegalStateException("Insufficient stock available for product: " + productId);
        }

        int updated = inventoryRepository.reserveStock(productId, quantity);
        if (updated == 0) throw new IllegalStateException("Failed to reserve stock.");
    }

    @Transactional
    public void releaseReservedStock(Long productId, Integer quantity) {
        log.info("Releasing {} reserved units for product {}", quantity, productId);
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        inventoryRepository.releaseReservedStock(productId, quantity);
    }

    @Transactional
    public void confirmReservation(Long productId, Integer quantity) {
        log.info("Confirming reservation of {} units for product {}", quantity, productId);
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");

        int updated = inventoryRepository.confirmReservation(productId, quantity);
        if (updated == 0) throw new IllegalStateException("Failed to confirm reservation.");
    }

    /**
     * THE CRITICAL FIX: Direct stock deduction logic
     * Used by OrderService to finalize purchases.
     */
    @Transactional
    public void deductStock(Long productId, int quantity) {
        // 1. Find the inventory
        InventoryItem inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Inventory not found"));

        // 2. Check if we have enough
        if (inventory.getAvailableStock() < quantity) {
            throw new RuntimeException("Not enough stock! Available: " + inventory.getAvailableStock());
        }

        // 3. CORRECT: Update the physical stock quantity
        inventory.setStockQuantity(inventory.getStockQuantity() - quantity);

        // 4. Update sales stats and status
        inventory.setSoldQuantity(inventory.getSoldQuantity() + quantity);
        inventory.setLastSoldAt(LocalDateTime.now());
        inventory.updateStockStatus();

        // 5. SAVE IT!
        inventoryRepository.save(inventory);
    }

    /**
     * Alias method for deductStock (used by Controller)
     */
    @Transactional
    public void decreaseStock(Long productId, Integer quantity) {
        deductStock(productId, quantity);
    }

    @Transactional
    public InventoryItem restockInventory(Long productId, Integer quantity, User currentUser) {
        log.info("Restocking product {} with {} units", productId, quantity);
        if (quantity <= 0) throw new IllegalArgumentException("Restock quantity must be positive");

        InventoryItem inventory = getInventoryByProductId(productId);

        if (!inventory.canBeModifiedBy(currentUser)) {
            throw new SecurityException("You don't have permission to restock this inventory");
        }

        if (inventory.getRequiresApproval() && !currentUser.isAdmin()) {
            throw new IllegalStateException("This inventory requires admin approval for restocking");
        }

        // Increase physical stock
        inventory.setStockQuantity(inventory.getStockQuantity() + quantity);

        inventory.setLastRestockedAt(LocalDateTime.now());
        inventory.setLastModifiedBy(currentUser);
        inventory.updateStockStatus();

        return inventoryRepository.save(inventory);
    }

    @Transactional
    public InventoryItem adjustStock(Long productId, Integer newQuantity, String reason, User currentUser) {
        log.info("Admin adjusting stock for product {} to {}", productId, newQuantity);

        if (!currentUser.isAdmin()) {
            throw new SecurityException("Only admins can manually adjust stock");
        }
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        InventoryItem inventory = getInventoryByProductId(productId);
        inventory.setStockQuantity(newQuantity);
        inventory.setLastModifiedBy(currentUser);

        String newNote = (inventory.getNotes() != null ? inventory.getNotes() + "\n" : "") +
                "Stock adjusted by admin: " + reason + " at " + LocalDateTime.now();
        inventory.setNotes(newNote);

        inventory.updateStockStatus();

        return inventoryRepository.save(inventory);
    }

    // ========================================================================
    // 4. ALERTS & ANALYTICS
    // ========================================================================

    public List<InventoryItem> getLowStockItems(User currentUser) {
        List<InventoryItem> lowStockItems = inventoryRepository.findLowStockItems();
        if (currentUser.isAdmin()) return lowStockItems;
        if (currentUser.isVendor()) {
            return lowStockItems.stream()
                    .filter(item -> item.getVendor().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public List<InventoryItem> getOutOfStockItems(User currentUser) {
        List<InventoryItem> outOfStockItems = inventoryRepository.findOutOfStockItems();
        if (currentUser.isAdmin()) return outOfStockItems;
        if (currentUser.isVendor()) {
            return outOfStockItems.stream()
                    .filter(item -> item.getVendor().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public List<InventoryItem> getInventoryByStatus(StockStatus status, User currentUser) {
        List<InventoryItem> items = inventoryRepository.findByStatus(status);
        if (currentUser.isAdmin()) return items;
        if (currentUser.isVendor()) {
            return items.stream()
                    .filter(item -> item.getVendor().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        } else {
            return items.stream()
                    .filter(InventoryItem::getIsActive)
                    .collect(Collectors.toList());
        }
    }

    public Double getTotalInventoryValue(Long vendorId, User currentUser) {
        if (!currentUser.isAdmin() && !currentUser.getId().equals(vendorId)) {
            throw new SecurityException("You can only view your own inventory value");
        }
        Double value = inventoryRepository.getTotalInventoryValueByVendor(vendorId);
        return value != null ? value : 0.0;
    }

    public Long countActiveProducts(Long vendorId, User currentUser) {
        if (!currentUser.isAdmin() && !currentUser.getId().equals(vendorId)) {
            throw new SecurityException("You can only view your own product count");
        }
        return inventoryRepository.countActiveProductsByVendor(vendorId);
    }

    public List<InventoryItem> getTopSellingProducts(User currentUser) {
        List<InventoryItem> topProducts = inventoryRepository.findTopSellingProducts();
        if (currentUser.isAdmin()) return topProducts;
        if (currentUser.isVendor()) {
            return topProducts.stream()
                    .filter(item -> item.getVendor().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        }
        return topProducts.stream().filter(InventoryItem::getIsActive).collect(Collectors.toList());
    }

    public VendorDashboardStats getVendorDashboard(Long vendorId, User currentUser) {
        if (!currentUser.isAdmin() && !currentUser.getId().equals(vendorId)) {
            throw new SecurityException("You can only view your own dashboard");
        }

        List<InventoryItem> vendorInventory = inventoryRepository.findByVendorId(vendorId);

        long totalProducts = vendorInventory.size();
        long activeProducts = vendorInventory.stream().filter(InventoryItem::getIsActive).count();
        long lowStockCount = vendorInventory.stream().filter(InventoryItem::needsReorder).count();
        long outOfStockCount = vendorInventory.stream().filter(item -> item.getStockQuantity() == 0).count();

        int totalStock = vendorInventory.stream().mapToInt(InventoryItem::getStockQuantity).sum();
        int totalReserved = vendorInventory.stream().mapToInt(InventoryItem::getReservedQuantity).sum();
        int totalSold = vendorInventory.stream().mapToInt(InventoryItem::getSoldQuantity).sum();

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

    // ========================================================================
    // 5. DELETE & ACTIVATION OPERATIONS
    // ========================================================================

    @Transactional
    public void deactivateInventory(Long id, User currentUser) {
        InventoryItem inventory = getInventoryById(id);
        if (!inventory.canBeModifiedBy(currentUser)) {
            throw new SecurityException("You don't have permission to deactivate this inventory");
        }
        inventory.setIsActive(false);
        inventory.setStatus(StockStatus.DISCONTINUED);
        inventory.setLastModifiedBy(currentUser);
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void reactivateInventory(Long id, User currentUser) {
        InventoryItem inventory = getInventoryById(id);
        if (!inventory.canBeModifiedBy(currentUser)) {
            throw new SecurityException("You don't have permission to reactivate this inventory");
        }
        inventory.setIsActive(true);
        inventory.updateStockStatus();
        inventory.setLastModifiedBy(currentUser);
        inventoryRepository.save(inventory);
    }

    @Transactional
    public void deleteInventory(Long id, User currentUser) {
        if (!currentUser.isAdmin()) {
            throw new SecurityException("Only admins can permanently delete inventory");
        }
        InventoryItem inventory = getInventoryById(id);
        if (inventory.getReservedQuantity() > 0) {
            throw new IllegalStateException("Cannot delete inventory with reserved stock");
        }
        inventoryRepository.deleteById(id);
    }

    @Transactional
    public void deleteInventoryByProductId(Long productId) {
        InventoryItem inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product ID: " + productId));

        if (inventory.getReservedQuantity() > 0) {
            throw new IllegalStateException("Cannot delete inventory with reserved stock. Release reservation first.");
        }

        inventoryRepository.delete(inventory);
        log.info("Successfully deleted inventory for Product ID: {}", productId);
    }
    // ========================================================================
    // 6. HELPER CLASSES
    // ========================================================================

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

    // ========================================================================
    // 7. INTEGRATION HELPERS
    // ========================================================================

    /**
     * Called by ProductService when a new product is created.
     */
    @Transactional
    public void initializeInventory(com.swe2project.yarncraft.modules.product.entity.Product product) {
        if (inventoryRepository.findByProductId(product.getId()).isPresent()) {
            return;
        }

        InventoryItem item = InventoryItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .vendor(product.getVendor())
                // Initialize with 10 stock so you can test Checkout immediately!
                .stockQuantity(10)
                .reservedQuantity(0)
                .soldQuantity(0)
                .isActive(true)
                .status(StockStatus.IN_STOCK)
                .build();

        inventoryRepository.save(item);
        log.info("Initialized inventory for Product ID: {}", product.getId());
    }
}