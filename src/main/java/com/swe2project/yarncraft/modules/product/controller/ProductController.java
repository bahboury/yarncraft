package com.swe2project.yarncraft.modules.product.controller;

import com.swe2project.yarncraft.common.dto.ApiResponse;
import com.swe2project.yarncraft.modules.product.dto.ProductRequest;
import com.swe2project.yarncraft.modules.product.entity.Category;
import com.swe2project.yarncraft.modules.product.entity.Product;
import com.swe2project.yarncraft.modules.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 1. Create Product (Vendor only)
    @PostMapping
    public ResponseEntity<ApiResponse<Product>> createProduct(
            @RequestBody ProductRequest request,
            Authentication authentication
    ) {
        Product product = productService.createProduct(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(product, "Product created successfully"));
    }

    // 2. Get All Products (Public)
    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.success(productService.getAllProducts(), "Fetched all products"));
    }

    // 3. Get Single Product (Public)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id), "Fetched product details"));
    }

    //Get Single Product by Category (Public)
    @GetMapping("/category")
    public ResponseEntity<ApiResponse<List<Product>>> getProductsByCategory(@RequestParam Category category) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductsByCategory(category), "Fetched products by category"));
    }


    // 4. Update Product (Vendor Owner only)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest request,
            Authentication authentication
    ) {
        Product updatedProduct = productService.updateProduct(id, authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(updatedProduct, "Product updated successfully"));
    }


    // 5. Delete Product (Vendor Owner only)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            Authentication authentication
    ) {
        productService.deleteProduct(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Product deleted successfully"));
    }

    // 6. Get Vendor's Products (Public or Vendor Dashboard)
    @GetMapping("/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<List<Product>>> getVendorProducts(@PathVariable Long vendorId) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductsByVendor(vendorId), "Fetched vendor products"));
    }
}