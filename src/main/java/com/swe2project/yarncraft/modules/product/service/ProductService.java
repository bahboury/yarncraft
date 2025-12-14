package com.swe2project.yarncraft.modules.product.service;

import com.swe2project.yarncraft.common.exception.ResourceNotFoundException;
import com.swe2project.yarncraft.modules.inventory.service.InventoryService;
import com.swe2project.yarncraft.modules.product.dto.ProductRequest;
import com.swe2project.yarncraft.modules.product.dto.ProductResponse;
import com.swe2project.yarncraft.modules.product.entity.Category;
import com.swe2project.yarncraft.modules.product.entity.Product;
import com.swe2project.yarncraft.modules.product.repository.ProductRepository;
import com.swe2project.yarncraft.modules.user.entity.Role;
import com.swe2project.yarncraft.modules.user.entity.User;
import com.swe2project.yarncraft.modules.user.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;

    // --- CREATE ---
    @Transactional
    public Product createProduct(String userEmail, ProductRequest request) {
        User user = getUserByEmail(userEmail);

        if (user.getRole() != Role.VENDOR) {
            throw new RuntimeException("Only vendors can add products.");
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .vendor(user)
                .build();

        Product savedProduct = productRepository.save(product);
        inventoryService.initializeInventory(savedProduct);

        return savedProduct;
    }

    // --- READ (UPDATED TO RETURN DTOs) ---

    // 1. Get All Public Products
    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findByVendorIsApprovedTrue();

        // Convert List<Product> -> List<ProductResponse>
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    // 2. Get Single Product
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToProductResponse(product);
    }

    // 3. Get Vendor Products (Keep returning Entities for dashboard, or convert to DTO if you prefer)
    // For now, let's convert it so the dashboard is consistent
    public List<ProductResponse> getProductsByVendor(Long vendorId) {
        List<Product> products = productRepository.findByVendorId(vendorId);
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    public List<Product> getProductsByCategory(Category category) {
        return productRepository.findByCategory(category);
    }

    // --- UPDATE ---
    public Product updateProduct(Long productId, String userEmail, ProductRequest request) {
        // (Keep existing logic...)
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        User user = getUserByEmail(userEmail);

        if (!product.getVendor().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to update this product.");
        }

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getCategory() != null) product.setCategory(request.getCategory());
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());

        return productRepository.save(product);
    }

    // --- DELETE ---
    @Transactional
    public void deleteProduct(Long productId, String userEmail) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        User user = getUserByEmail(userEmail);

        if (!product.getVendor().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to delete this product.");
        }

        // 👇 NEW: Find and delete the associated InventoryItem first
        try {
            inventoryService.deleteInventoryByProductId(productId);
        } catch (ResourceNotFoundException e) {
            // Log, but continue: The inventory might not have been created yet.
            System.out.println("Warning: Inventory for product " + productId + " not found, continuing deletion.");
        }

        // 💥 Finally, delete the Product
        productRepository.delete(product);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    // 👇 HELPER METHOD: Converts Entity to DTO
    private ProductResponse mapToProductResponse(Product product) {
        // 1. Get Vendor Name Safely
        String vName = "Unknown Vendor";
        if (product.getVendor() != null && product.getVendor().getName() != null) {
            vName = product.getVendor().getName();
        }

        // 🔍 DEBUG: Print to console to prove it found the name
        System.out.println("Mapping Product: " + product.getName() + " | Vendor: " + vName);

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .vendorName(vName) // 👈 Ensure this matches your DTO field exactly!
                .build();
    }

    // 👇 ADD THIS METHOD
    public List<ProductResponse> getMyProducts(String userEmail) {
        User user = getUserByEmail(userEmail); // Uses your existing helper method

        // Use the existing repository method (findByVendorId)
        List<Product> products = productRepository.findByVendorId(user.getId());

        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }
}