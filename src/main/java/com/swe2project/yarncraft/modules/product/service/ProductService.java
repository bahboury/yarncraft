package com.swe2project.yarncraft.modules.product.service;

import com.swe2project.yarncraft.common.exception.ResourceNotFoundException;
import com.swe2project.yarncraft.modules.inventory.service.InventoryService;
import com.swe2project.yarncraft.modules.product.dto.ProductRequest;
import com.swe2project.yarncraft.modules.product.entity.Category;
import com.swe2project.yarncraft.modules.product.entity.Product;
import com.swe2project.yarncraft.modules.product.repository.ProductRepository;
import com.swe2project.yarncraft.modules.user.entity.Role;
import com.swe2project.yarncraft.modules.user.entity.User;
import com.swe2project.yarncraft.modules.user.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService; // 3. Inject InventoryService

    // --- CREATE ---
    @Transactional // 4. Transactional ensures data integrity
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

        // 5. CRITICAL: Initialize empty inventory for this new product!
        inventoryService.initializeInventory(savedProduct);

        return savedProduct;
    }

    // --- READ ---
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public List<Product> getProductsByVendor(Long vendorId) {
        return productRepository.findByVendorId(vendorId);
    }

    public List<Product> getProductsByCategory(Category category) {
        return productRepository.findByCategory(category);
    }

    // --- UPDATE ---
    public Product updateProduct(Long productId, String userEmail, ProductRequest request) {
        Product product = getProductById(productId);
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
    public void deleteProduct(Long productId, String userEmail) {
        Product product = getProductById(productId);
        User user = getUserByEmail(userEmail);

        if (!product.getVendor().getId().equals(user.getId())) {
            throw new RuntimeException("You are not authorized to delete this product.");
        }

        productRepository.delete(product);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}