package com.swe2project.yarncraft.modules.order.dto;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class OrderRequest {
    // The list of items the user wants to buy
    private List<OrderItemRequest> items;
    private String paymentMethod;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private int quantity;

        // Dynamic attributes: "Color": "Red", "Size": "M"
        private Map<String, String> customizations;
    }
}