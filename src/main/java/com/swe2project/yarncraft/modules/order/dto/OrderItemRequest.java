package com.swe2project.yarncraft.modules.order.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemRequest {
    private Long productId;
    private int quantity;
    private BigDecimal price;
}