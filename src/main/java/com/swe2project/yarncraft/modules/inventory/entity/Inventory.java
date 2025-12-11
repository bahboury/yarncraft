package com.swe2project.yarncraft.modules.inventory.entity;

import com.swe2project.yarncraft.modules.user.entity.User;
import com.swe2project.yarncraft.modules.user.entity.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory", indexes = {
    @Index(name = "idx_product_id", columnList = "product_id"),
    @Index(name = "idx_vendor_id", columnList = "vendor_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Product Information
    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;
    
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;
    
    @Column(name = "product_sku", length = 100)
    private String productSku;
    
    @Column(name = "product_category", length = 100)
    private String productCategory;
    
    // Stock Management
    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;
    
    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;
    
    @Column(name = "sold_quantity", nullable = false)
    @Builder.Default
    private Integer soldQuantity = 0;
    
    @Column(name = "reorder_level", nullable = false)
    @Builder.Default
    private Integer reorderLevel = 10;
    
    @Column(name = "max_stock_level")
    private Integer maxStockLevel;
    
    // Pricing Information
    @Column(name = "unit_cost", precision = 10, scale = 2)
    private BigDecimal unitCost;
    
    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    // Vendor/Owner Information - Integration with User Module
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private User vendor;
    
    @Column(name = "warehouse_location", length = 255)
    private String warehouseLocation;
    
    // Stock Status
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private StockStatus status = StockStatus.IN_STOCK;
    
    // Inventory Health
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "is_trackable", nullable = false)
    @Builder.Default
    private Boolean isTrackable = true;
    
    @Column(name = "requires_approval", nullable = false)
    @Builder.Default
    private Boolean requiresApproval = false;
    
    // Audit Fields - Who made changes
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_modified_by")
    private User lastModifiedBy;
    
    // Timestamps
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "last_restocked_at")
    private LocalDateTime lastRestockedAt;
    
    @Column(name = "last_sold_at")
    private LocalDateTime lastSoldAt;
    
    // Notes and Description
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    // Enum for Stock Status
    public enum StockStatus {
        IN_STOCK,           // Available for sale
        LOW_STOCK,          // Below reorder level
        OUT_OF_STOCK,       // No stock available
        DISCONTINUED,       // Product no longer sold
        PENDING_RESTOCK,    // Waiting for vendor to restock
        RESERVED            // All stock is reserved in carts
    }
    
    // Lifecycle Callbacks
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        updateStockStatus();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        updateStockStatus();
    }
    
    // Business Logic Methods
    
    /**
     * Automatically updates stock status based on current quantities
     */
    public void updateStockStatus() {
        if (!isActive) {
            this.status = StockStatus.DISCONTINUED;
            return;
        }
        
        int availableStock = getAvailableStock();
        
        if (availableStock == 0 && reservedQuantity > 0) {
            this.status = StockStatus.RESERVED;
        } else if (availableStock == 0) {
            this.status = StockStatus.OUT_OF_STOCK;
        } else if (availableStock <= reorderLevel) {
            this.status = StockStatus.LOW_STOCK;
        } else {
            this.status = StockStatus.IN_STOCK;
        }
    }
    
    /**
     * Get available stock (total minus reserved)
     */
    public int getAvailableStock() {
        return Math.max(0, stockQuantity - reservedQuantity);
    }
    
    /**
     * Get total inventory value (cost-based)
     */
    public BigDecimal getTotalInventoryValue() {
        if (unitCost == null) {
            return BigDecimal.ZERO;
        }
        return unitCost.multiply(BigDecimal.valueOf(stockQuantity));
    }
    
    /**
     * Get potential revenue (if all stock sold at unit price)
     */
    public BigDecimal getPotentialRevenue() {
        if (unitPrice == null) {
            return BigDecimal.ZERO;
        }
        return unitPrice.multiply(BigDecimal.valueOf(getAvailableStock()));
    }
    
    /**
     * Check if stock needs reordering
     */
    public boolean needsReorder() {
        return isActive && getAvailableStock() <= reorderLevel;
    }
    
    /**
     * Check if user has permission to modify this inventory
     * Works with Spring Security and your User entity
     */
    public boolean canBeModifiedBy(User user) {
        if (user == null) {
            return false;
        }
        
        // Admin can modify any inventory
        if (user.getRole() == Role.ADMIN) {
            return true;
        }
        
        // Approved vendors can only modify their own inventory
        if (user.getRole() == Role.VENDOR && user.isApproved()) {
            return this.vendor != null && this.vendor.getId().equals(user.getId());
        }
        
        return false;
    }
    
    /**
     * Check if user can view this inventory
     * All authenticated users can view inventory (for browsing products)
     */
    public boolean canBeViewedBy(User user) {
        if (user == null) {
            return false;  // Anonymous users cannot view inventory details
        }
        // All authenticated users can view
        return user.isEnabled();
    }
    
    /**
     * Check if the vendor who owns this inventory is approved
     */
    public boolean isVendorApproved() {
        return this.vendor != null && 
               this.vendor.getRole() == Role.VENDOR && 
               this.vendor.isApproved();
    }
    
    /**
     * Get the vendor's name (for display purposes)
     */
    public String getVendorName() {
        return this.vendor != null ? this.vendor.getName() : "Unknown Vendor";
    }
    
    /**
     * Get the vendor's email
     */
    public String getVendorEmail() {
        return this.vendor != null ? this.vendor.getEmail() : null;
    }
}
