package com.swe2project.yarncraft.modules.product.dto;

import com.swe2project.yarncraft.modules.product.entity.Category;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private Category category;
    private String imageUrl;
}

