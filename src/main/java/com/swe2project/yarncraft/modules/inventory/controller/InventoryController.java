package com.swe2project.yarncraft.modules.inventory.controller;

import com.swe2project.yarncraft.common.dto.ApiResponse;
import com.swe2project.yarncraft.common.exception.ResourceNotFoundException;
import com.swe2project.yarncraft.modules.inventory.entity.InventoryItem;
import com.swe2project.yarncraft.modules.inventory.entity.InventoryItem.StockStatus;
import com.swe2project.yarncraft.modules.inventory.service.InventoryService;
import com.swe2project.yarncraft.modules.user.entity.User;
import com.swe2project.yarncraft.modules.user.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Slf4j
public class InventoryController {

    private final InventoryService inventoryService;
    private final UserRepository userRepository;

    // ==================== HELPER METHOD ====================

    /**
     * Get currently authenticated user from Database using the Email in the Token
     * This is safer than casting the Principal directly.
     */
    private User getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("User not authenticated");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // ==================== CREATE & UPDATE ENDPOINTS ====================

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryItem>> createInventory(
            @RequestBody InventoryItem inventoryItem,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        InventoryItem created = inventoryService.createInventory(inventoryItem, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Inventory created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryItem>> updateInventory(
            @PathVariable Long id,
            @RequestBody InventoryItem inventoryItem,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        InventoryItem updated = inventoryService.updateInventory(id, inventoryItem, currentUser);
        return ResponseEntity.ok(ApiResponse.success(updated, "Inventory updated successfully"));
    }

    // ==================== READ ENDPOINTS ====================

    @GetMapping
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getAllInventory(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getAllInventory(currentUser),
                "Inventory fetched successfully"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryItem>> getInventoryById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getInventoryById(id),
                "Inventory item fetched successfully"
        ));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<InventoryItem>> getInventoryByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getInventoryByProductId(productId),
                "Inventory for product fetched successfully"
        ));
    }

    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getVendorInventory(
            @PathVariable Long vendorId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getVendorInventory(vendorId, currentUser),
                "Vendor inventory fetched successfully"
        ));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getActiveInventory() {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getActiveInventory(),
                "Active inventory fetched successfully"
        ));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getInventoryByCategory(@PathVariable String category) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getInventoryByCategory(category),
                "Inventory by category fetched successfully"
        ));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> searchInventory(@RequestParam String q) {
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.searchInventory(q),
                "Search results fetched successfully"
        ));
    }

    @GetMapping("/search/advanced")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> searchInventoryAdvanced(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long vendorId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.searchInventoryAdvanced(name, category, vendorId, currentUser),
                "Advanced search results fetched successfully"
        ));
    }

    // ==================== STOCK MANAGEMENT ENDPOINTS ====================

    @GetMapping("/check/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity) {

        boolean hasStock = inventoryService.checkStock(productId, quantity);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of(
                        "productId", productId,
                        "requestedQuantity", quantity,
                        "available", hasStock
                ),
                "Stock check completed"
        ));
    }

    @GetMapping("/available/{productId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAvailableStock(@PathVariable Long productId) {
        Integer stock = inventoryService.getAvailableStock(productId);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("productId", productId, "availableStock", stock),
                "Available stock retrieved"
        ));
    }

    @PutMapping("/restock/{productId}")
    public ResponseEntity<ApiResponse<InventoryItem>> restockInventory(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        InventoryItem updated = inventoryService.restockInventory(productId, quantity, currentUser);
        return ResponseEntity.ok(ApiResponse.success(updated, "Restocked successfully"));
    }

    @PutMapping("/adjust/{productId}")
    public ResponseEntity<ApiResponse<InventoryItem>> adjustStock(
            @PathVariable Long productId,
            @RequestParam Integer quantity,
            @RequestParam String reason,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        InventoryItem updated = inventoryService.adjustStock(productId, quantity, reason, currentUser);
        return ResponseEntity.ok(ApiResponse.success(updated, "Stock adjusted manually"));
    }

    // ==================== MONITORING & ALERTS ====================

    @GetMapping("/alerts/low-stock")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getLowStockItems(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getLowStockItems(currentUser),
                "Low stock alerts retrieved"
        ));
    }

    @GetMapping("/alerts/out-of-stock")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getOutOfStockItems(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getOutOfStockItems(currentUser),
                "Out of stock alerts retrieved"
        ));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getInventoryByStatus(
            @PathVariable StockStatus status,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getInventoryByStatus(status, currentUser),
                "Inventory items by status retrieved"
        ));
    }

    // ==================== ANALYTICS ====================

    @GetMapping("/analytics/value/{vendorId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTotalInventoryValue(
            @PathVariable Long vendorId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        Double value = inventoryService.getTotalInventoryValue(vendorId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("vendorId", vendorId, "totalInventoryValue", value),
                "Inventory value calculated"
        ));
    }

    @GetMapping("/analytics/count/{vendorId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> countActiveProducts(
            @PathVariable Long vendorId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        Long count = inventoryService.countActiveProducts(vendorId, currentUser);
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("vendorId", vendorId, "activeProductCount", count),
                "Active products counted"
        ));
    }

    @GetMapping("/analytics/top-selling")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getTopSellingProducts(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getTopSellingProducts(currentUser),
                "Top selling products retrieved"
        ));
    }

    @GetMapping("/dashboard/{vendorId}")
    public ResponseEntity<ApiResponse<InventoryService.VendorDashboardStats>> getVendorDashboard(
            @PathVariable Long vendorId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getVendorDashboard(vendorId, currentUser),
                "Vendor dashboard stats generated"
        ));
    }

    // 👇 ADD THIS METHOD (Convenience for "My Dashboard")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<InventoryService.VendorDashboardStats>> getMyDashboard(
            Authentication authentication) {

        // 1. Get Logged-in User
        User currentUser = getCurrentUser(authentication);

        // 2. Ensure they are a Vendor (or Admin)
        if (!currentUser.isVendor() && !currentUser.isAdmin()) {
            throw new RuntimeException("Only vendors can access dashboard stats");
        }

        // 3. Get Stats using their own ID
        return ResponseEntity.ok(ApiResponse.success(
                inventoryService.getVendorDashboard(currentUser.getId(), currentUser),
                "My dashboard stats generated"
        ));
    }
    // ==================== DELETE ENDPOINTS ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deactivateInventory(
            @PathVariable Long id,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        inventoryService.deactivateInventory(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Inventory deactivated successfully", "Success"));
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponse<String>> reactivateInventory(
            @PathVariable Long id,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        inventoryService.reactivateInventory(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Inventory reactivated successfully", "Success"));
    }

    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<ApiResponse<String>> deleteInventoryPermanently(
            @PathVariable Long id,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        inventoryService.deleteInventory(id, currentUser);
        return ResponseEntity.ok(ApiResponse.success("Inventory permanently deleted", "Success"));
    }

    @GetMapping("/my-inventory")
    public ResponseEntity<ApiResponse<List<InventoryItem>>> getMyInventory(
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);

        // This relies on inventoryService.getVendorInventory(vendorId, currentUser)
        // which already handles the security check (if the user is the vendor or admin)
        List<InventoryItem> inventoryItems = inventoryService.getVendorInventory(currentUser.getId(), currentUser);

        return ResponseEntity.ok(ApiResponse.success(inventoryItems, "My inventory list retrieved successfully"));
    }
}