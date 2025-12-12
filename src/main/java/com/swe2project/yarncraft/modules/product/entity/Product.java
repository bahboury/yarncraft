package com.swe2project.yarncraft.modules.product.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.swe2project.yarncraft.modules.user.entity.User;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    // Relationship: Many Products -> One Vendor
    //in other words (one vendor can have many products)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonIgnore
    private User vendor;
}