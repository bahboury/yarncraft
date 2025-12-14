package com.swe2project.yarncraft.modules.product.dto;

import com.swe2project.yarncraft.modules.product.entity.Category;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Category category;
    private String imageUrl;

    // 👇 THIS IS THE FIELD WE NEED FOR THE FRONTEND
    private String vendorName;
}