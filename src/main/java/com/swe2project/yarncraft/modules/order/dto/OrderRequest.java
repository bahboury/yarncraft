package com.swe2project.yarncraft.modules.order.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class OrderRequest {
    // 👇 Added these so we can save where to ship!
    private String shippingAddress;
    private String phone;

    private String paymentMethod; // e.g. "CREDIT_CARD" (Optional for now)

    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private int quantity;
        private BigDecimal price; // We will use this or fetch from DB
        private Map<String, String> customizations; // e.g. Color, Size
    }
}