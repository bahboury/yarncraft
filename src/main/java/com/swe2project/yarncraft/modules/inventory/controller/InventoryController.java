package com.swe2project.yarncraft.modules.inventory.controller;

import com.swe2project.yarncraft.modules.inventory.entity.InventoryItem;
import com.swe2project.yarncraft.modules.inventory.entity.InventoryItem.StockStatus;
import com.swe2project.yarncraft.modules.inventory.service.InventoryService;
import com.swe2project.yarncraft.modules.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    // ==================== HELPER METHOD ====================
    
    /**
     * Get currently authenticated user from Spring Security context
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        throw new RuntimeException("User not authenticated");
    }
    
    // ==================== CREATE & UPDATE ENDPOINTS ====================
    
    /**
     * Create new inventory item
     * POST /api/inventory
     * Access: Admin, Approved Vendors
     */
    @PostMapping
    public ResponseEntity<?> createInventory(@RequestBody InventoryItem inventoryItem) {
        try {
            User currentUser = getCurrentUser();
            log.info("Request to create inventory by user: {}", currentUser.getEmail());
            
            InventoryItem created = inventoryService.createInventory(inventoryItem, currentUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
            
        } catch (SecurityException e) {
            log.error("Permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error creating inventory: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create inventory"));
        }
    }
    
    /**
     * Update existing inventory item
     * PUT /api/inventory/{id}
     * Access: Admin, Owner Vendor
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateInventory(
            @PathVariable Long id,
            @RequestBody InventoryItem inventoryItem) {
        try {
            User currentUser = getCurrentUser();
            log.info("Request to update inventory ID: {} by user: {}", id, currentUser.getEmail());
            
            InventoryItem updated = inventoryService.updateInventory(id, inventoryItem, currentUser);
            return ResponseEntity.ok(updated);
            
        } catch (SecurityException e) {
            log.error("Permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Inventory not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating inventory: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update inventory"));
        }
    }
    
    // ==================== READ ENDPOINTS ====================
    
    /**
     * Get all inventory items
     * GET /api/inventory
     * Access: All authenticated users (filtered by role)
     */
    @GetMapping
    public ResponseEntity<?> getAllInventory() {
        try {
            User currentUser = getCurrentUser();
            log.info("Request to get all inventory by user: {}", currentUser.getEmail());
            
            List<InventoryItem> items = inventoryService.getAllInventory(currentUser);
            return ResponseEntity.ok(items);
            
        } catch (Exception e) {
            log.error("Error fetching inventory: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch inventory"));
        }
    }
    
    /**
     * Get inventory by ID
     * GET /api/inventory/{id}
     * Access: All authenticated users
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getInventoryById(@PathVariable Long id) {
        try {
            log.info("Request to get inventory by ID: {}", id);
            
            InventoryItem item = inventoryService.getInventoryById(id);
            return ResponseEntity.ok(item);
            
        } catch (RuntimeException e) {
            log.error("Inventory not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching inventory: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch inventory"));
        }
    }
    
    /**
     * Get inventory by product ID
     * GET /api/inventory/product/{productId}
     * Access: All authenticated users
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<?> getInventoryByProduct(@PathVariable Long productId) {
        try {
            log.info("Request to get inventory for product: {}", productId);
            
            InventoryItem item = inventoryService.getInventoryByProductId(productId);
            return ResponseEntity.ok(item);
            
        } catch (RuntimeException e) {
            log.error("Inventory not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching inventory: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch inventory"));
        }
    }
    
    /**
     * Get vendor's inventory
     * GET /api/inventory/vendor/{vendorId}
     * Access: Admin (any vendor), Vendor (own only)
     */
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<?> getVendorInventory(@PathVariable Long vendorId) {
        try {
            User currentUser = getCurrentUser();
            log.info("Request to get vendor {} inventory by user: {}", 
                     vendorId, currentUser.getEmail());
            
            List<InventoryItem> items = inventoryService.getVendorInventory(vendorId, currentUser);
            return ResponseEntity.ok(items);
            
        } catch (SecurityException e) {
            log.error("Permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching vendor inventory: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch vendor inventory"));
        }
    }
    
    /**
     * Get active inventory items
     * GET /api/inventory/active
     * Access: All authenticated users
     */
    @GetMapping("/active")
    public ResponseEntity<?> getActiveInventory() {
        try {
            log.info("Request to get active inventory");
            
            List<InventoryItem> items = inventoryService.getActiveInventory();
            return ResponseEntity.ok(items);
            
        } catch (Exception e) {
            log.error("Error fetching active inventory: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch active inventory"));
        }
    }
    
    /**
     * Get inventory by category
     * GET /api/inventory/category/{category}
     * Access: All authenticated users
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<?> getInventoryByCategory(@PathVariable String category) {
        try {
            log.info("Request to get inventory for category: {}", category);
            
            List<InventoryItem> items = inventoryService.getInventoryByCategory(category);
            return ResponseEntity.ok(items);
            
        } catch (Exception e) {
            log.error("Error fetching inventory by category: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch inventory by category"));
        }
    }
    
    /**
     * Search inventory by name
     * GET /api/inventory/search?q=searchTerm
     * Access: All authenticated users
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchInventory(@RequestParam String q) {
        try {
            log.info("Request to search inventory with term: {}", q);
            
            List<InventoryItem> items = inventoryService.searchInventory(q);
            return ResponseEntity.ok(items);
            
        } catch (Exception e) {
            log.error("Error searching inventory: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to search inventory"));
        }
    }
    
    /**
     * Advanced search with multiple filters
     * GET /api/inventory/search/advanced?name=...&category=...&vendorId=...
     * Access: All authenticated users (filtered by role)
     */
    @GetMapping("/search/advanced")
    public ResponseEntity<?> searchInventoryAdvanced(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long vendorId) {
        try {
            User currentUser = getCurrentUser();
            log.info("Advanced search - Name: {}, Category: {}, VendorId: {}", 
                     name, category, vendorId);
            
            List<InventoryItem> items = inventoryService.searchInventoryAdvanced(
                    name, category, vendorId, currentUser);
            return ResponseEntity.ok(items);
            
        } catch (SecurityException e) {
            log.error("Permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error in advanced search: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to search inventory"));
        }
    }
    
    // ==================== STOCK MANAGEMENT ENDPOINTS ====================
    
    /**
     * Check stock availability
     * GET /api/inventory/check/{productId}?quantity=5
     * Access: All authenticated users
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<?> checkStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        try {
            log.info("Request to check stock for product {} quantity {}", productId, quantity);
            
            boolean hasStock = inventoryService.checkStock(productId, quantity);
            return ResponseEntity.ok(Map.of(
                    "productId", productId,
                    "requestedQuantity", quantity,
                    "available", hasStock
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error checking stock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to check stock"));
        }
    }
    
    /**
     * Get available stock quantity
     * GET /api/inventory/available/{productId}
     * Access: All authenticated users
     */
    @GetMapping("/available/{productId}")
    public ResponseEntity<?> getAvailableStock(@PathVariable Long productId) {
        try {
            log.info("Request to get available stock for product: {}", productId);
            
            Integer stock = inventoryService.getAvailableStock(productId);
            return ResponseEntity.ok(Map.of(
                    "productId", productId,
                    "availableStock", stock
            ));
            
        } catch (Exception e) {
            log.error("Error getting available stock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get available stock"));
        }
    }
    
    /**
     * Reserve stock (for cart)
     * POST /api/inventory/reserve/{productId}?quantity=2
     * Access: All authenticated users
     */
    @PostMapping("/reserve/{productId}")
    public ResponseEntity<?> reserveStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        try {
            log.info("Request to reserve {} units for product {}", quantity, productId);
            
            inventoryService.reserveStock(productId, quantity);
            return ResponseEntity.ok(Map.of(
                    "message", "Stock reserved successfully",
                    "productId", productId,
                    "quantity", quantity
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("Insufficient stock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error reserving stock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to reserve stock"));
        }
    }
    
    /**
     * Release reserved stock (cart item removed)
     * POST /api/inventory/release/{productId}?quantity=2
     * Access: All authenticated users
     */
    @PostMapping("/release/{productId}")
    public ResponseEntity<?> releaseStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        try {
            log.info("Request to release {} reserved units for product {}", quantity, productId);
            
            inventoryService.releaseReservedStock(productId, quantity);
            return ResponseEntity.ok(Map.of(
                    "message", "Reserved stock released successfully",
                    "productId", productId,
                    "quantity", quantity
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error releasing stock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to release stock"));
        }
    }
    
    /**
     * Confirm reservation (order completed)
     * POST /api/inventory/confirm/{productId}?quantity=2
     * Access: All authenticated users
     */
    @PostMapping("/confirm/{productId}")
    public ResponseEntity<?> confirmReservation(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        try {
            log.info("Request to confirm reservation of {} units for product {}", 
                     quantity, productId);
            
            inventoryService.confirmReservation(productId, quantity);
            return ResponseEntity.ok(Map.of(
                    "message", "Reservation confirmed successfully",
                    "productId", productId,
                    "quantity", quantity
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("Insufficient reserved stock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error confirming reservation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to confirm reservation"));
        }
    }
    
    /**
     * Decrease stock directly
     * POST /api/inventory/decrease/{productId}?quantity=2
     * Access: Admin, Owner Vendor
     */
    @PostMapping("/decrease/{productId}")
    public ResponseEntity<?> decreaseStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        try {
            log.info("Request to decrease stock for product {} by {}", productId, quantity);
            
            inventoryService.decreaseStock(productId, quantity);
            return ResponseEntity.ok(Map.of(
                    "message", "Stock decreased successfully",
                    "productId", productId,
                    "quantity", quantity
            ));
            
        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("Insufficient stock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error decreasing stock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to decrease stock"));
        }
    }
    
    /**
     * Restock inventory
     * PUT /api/inventory/restock/{productId}?quantity=50
     * Access: Admin, Owner Vendor
     */
    @PutMapping("/restock/{productId}")
    public ResponseEntity<?> restockInventory(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {
        try {
            User currentUser = getCurrentUser();
            log.info("Request to restock product {} with {} units by user {}", 
                     productId, quantity, currentUser.getEmail());
            
            InventoryItem updated = inventoryService.restockInventory(productId, quantity, currentUser);
            return ResponseEntity.ok(updated);
            
        } catch (SecurityException e) {
            log.error("Permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            log.error("Restocking error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error restocking inventory: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to restock inventory"));
        }
    }
    
    /**
     * Manual stock adjustment (admin only)
     * PUT /api/inventory/adjust/{productId}?quantity=100&reason=Inventory%20correction
     * Access: Admin only
     */
    @PutMapping("/adjust/{productId}")
    public ResponseEntity<?> adjustStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            @RequestParam String reason) {
        try {
            User currentUser = getCurrentUser();
            log.info("Request to adjust stock for product {} to {} by admin", productId, quantity);
            
            InventoryItem updated = inventoryService.adjustStock(productId, quantity, reason, currentUser);
            return ResponseEntity.ok(updated);
            
        } catch (SecurityException e) {
            log.error("Permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid input: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error adjusting stock: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to adjust stock"));
        }
    }
    
    // ==================== MONITORING & ALERTS ENDPOINTS ====================
    
    /**
     * Get low stock items
     * GET /api/inventory/alerts/low-stock
     * Access: Admin (all), Vendor (own)
     */
    @GetMapping("/alerts/low-stock")
    public ResponseEntity<?> getLowStockItems() {
        try {
            User currentUser = getCurrentUser();
            log.info("Request to get low stock items by user: {}", currentUser.getEmail());
            
            List<InventoryItem> items = inventoryService.getLowStockItems(currentUser);
            return ResponseEntity.ok(items);
            
        } catch (Exception e) {
            log.error("Error fetching low stock items: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch low stock items"));
        }
    }
    
    /**
     * Get out of stock items
     * GET /api/inventory/alerts/out-of-stock
     * Access: Admin (all), Vendor (own)
     */
    @GetMapping("/alerts/out-of-stock")
    public ResponseEntity<?> getOutOfStockItems() {
        try {
            User currentUser = getCurrentUser();
            log.info("Request to get out of stock items by user: {}", currentUser.getEmail());
            
            List<InventoryItem> items = inventoryService.getOutOfStockItems(currentUser);
            return ResponseEntity.ok(items);
            
        } catch (Exception e) {
            log.error("Error fetching out of stock items: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch out of stock items"));
        }
    }
    
    /**
     * Get inventory by status
     * GET /api/inventory/status/{status}
     * Access: Admin (all), Vendor (own), Customer (active only)
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<?> getInventoryByStatus(@PathVariable StockStatus status) {
        try {
            User currentUser = getCurrentUser();
            log.info("Request to get inventory with status: {} by user: {}", 
                     status, currentUser.getEmail());
            
            List<InventoryItem> items = inventoryService.getInventoryByStatus(status, currentUser);
            return ResponseEntity.ok(items);
            
        } catch (Exception e) {
            log.error("Error fetching inventory by status: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch inventory by status"));
        }
    }
    
    // ==================== ANALYTICS ENDPOINTS ====================
    
    /**
     * Get total inventory value for vendor
     * GET /api/inventory/analytics/value/{vendorId}
     * Access: Admin (any vendor), Vendor (own only)
     */
    @GetMapping("/analytics/value/{vendorId}")
    public ResponseEntity<?> getTotalInventoryValue(@PathVariable Long vendorId) {
        try {
            User currentUser = getCurrentUser();
            log.info("Request to get inventory value for vendor: {}", vendorId);
            
            Double value = inventoryService.getTotalInventoryValue(vendorId, currentUser);
            return ResponseEntity.ok(Map.of(
                    "vendorId", vendorId,
                    "totalInventoryValue", value
            ));
            
        } catch (SecurityException e) {
            log.error("Permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error calculating inventory value: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to calculate inventory value"));
        }
    }
    
    /**
     * Get active product count for vendor
     * GET /api/inventory/analytics/count/{vendorId}
     * Access: Admin (any vendor), Vendor (own only)
     */
    @GetMapping("/analytics/count/{vendorId}")
    public ResponseEntity<?> countActiveProducts(@PathVariable Long vendorId) {
        try {
            User currentUser = getCurrentUser();
            log.info("Request to count active products for vendor: {}", vendorId);
            
            Long count = inventoryService.countActiveProducts(vendorId, currentUser);
            return ResponseEntity.ok(Map.of(
                    "vendorId", vendorId,
                    "activeProductCount", count
            ));
            
        } catch (SecurityException e) {
            log.error("Permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error counting products: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to count products"));
        }
    }
    
    /**
     * Get top selling products
     * GET /api/inventory/analytics/top-selling
     * Access: Admin (all), Vendor (own), Customer (active only)
     */
    @GetMapping("/analytics/top-selling")
    public ResponseEntity<?> getTopSellingProducts() {
        try {
            User currentUser = getCurrentUser();
            log.info("Request to get top selling products");
            
            List<InventoryItem> items = inventoryService.getTopSellingProducts(currentUser);
            return ResponseEntity.ok(items);
            
        } catch (Exception e) {
            log.error("Error fetching top selling products: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch top selling products"));
        }
    }
    
    /**
     * Get vendor dashboard statistics
     * GET /api/inventory/dashboard/{vendorId}
     * Access: Admin (any vendor), Vendor (own only)
     */
    @GetMapping("/dashboard/{vendorId}")
    public ResponseEntity<?> getVendorDashboard(@PathVariable Long vendorId) {
        try {
            User currentUser = getCurrentUser();
            log.info("Request to get dashboard for vendor: {}", vendorId);
            
            InventoryService.VendorDashboardStats stats = 
                    inventoryService.getVendorDashboard(vendorId, currentUser);
            return ResponseEntity.ok(stats);
            
        } catch (SecurityException e) {
            log.error("Permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error generating dashboard: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate dashboard"));
        }
    }
    
    // ==================== DELETE ENDPOINTS ====================
    
    /**
     * Deactivate inventory (soft delete)
     * DELETE /api/inventory/{id}
     * Access: Admin, Owner Vendor
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deactivateInventory(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            log.info("Request to deactivate inventory ID: {} by user: {}", 
                     id, currentUser.getEmail());
            
            inventoryService.deactivateInventory(id, currentUser);
            return ResponseEntity.ok(Map.of(
                    "message", "Inventory deactivated successfully",
                    "inventoryId", id
            ));
            
        } catch (SecurityException e) {
            log.error("Permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Inventory not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Error deactivating inventory: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to deactivate inventory"));
        }
    }
    
    /**
     * Reactivate inventory
     * PUT /api/inventory/{id}/reactivate
     * Access: Admin, Owner Vendor
     */
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<?> reactivateInventory(@PathVariable Long id) {
        try {
            User currentUser = getCurrentUser();
            log.info("Request to reactivate inventory ID: {} by user: {}", 
                     id, currentUser.getEmail());
            
            inventoryService.reactivateInventory(id, currentUser);
            return ResponseEntity.ok(Map.of(
                    "message", "Inventory reactivated successfully",
                    "inventoryId", id
            ));
            
        } catch (SecurityException e) {
            log.error("Permission denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
                        } catch (RuntimeException e) {
        log.error("Inventory not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
        log.error("Error reactivating inventory: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to reactivate inventory"));
    }
}
/**
 * Permanently delete inventory (hard delete)
 * DELETE /api/inventory/{id}/permanent
 * Access: Admin only
 */
@DeleteMapping("/{id}/permanent")
public ResponseEntity<?> deleteInventoryPermanently(@PathVariable Long id) {
    try {
        User currentUser = getCurrentUser();
        log.info("Request to permanently delete inventory ID: {} by admin", id);
        
        inventoryService.deleteInventory(id, currentUser);
        return ResponseEntity.ok(Map.of(
                "message", "Inventory permanently deleted",
                "inventoryId", id
        ));
        
    } catch (SecurityException e) {
        log.error("Permission denied: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", e.getMessage()));
    } catch (IllegalStateException e) {
        log.error("Cannot delete: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
    } catch (RuntimeException e) {
        log.error("Inventory not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
        log.error("Error deleting inventory: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to delete inventory"));
    }
}}
