package com.swe2project.yarncraft.modules.product.repository;

import com.swe2project.yarncraft.modules.product.entity.Category;
import com.swe2project.yarncraft.modules.product.entity.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Find all products by a specific vendor
    List<Product> findByVendorId(Long vendorId);

    // Filter products by category (e.g., for the homepage)
    List<Product> findByCategory(Category category);

    // This magic name tells Spring: "Go to Vendor field, check isApproved, and only return True ones"
    List<Product> findByVendorIsApprovedTrue();
}