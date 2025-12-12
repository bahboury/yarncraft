package com.swe2project.yarncraft.modules.order.controller;

import com.swe2project.yarncraft.common.dto.ApiResponse;
import com.swe2project.yarncraft.modules.order.dto.OrderRequest;
import com.swe2project.yarncraft.modules.order.entity.Order;
import com.swe2project.yarncraft.modules.order.service.OrderService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 1. Place Order
    @PostMapping
    public ResponseEntity<ApiResponse<Order>> placeOrder(
            @RequestBody OrderRequest request,
            Authentication authentication
    ) {
        Order order = orderService.placeOrder(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(order, "Order placed successfully!"));
    }

    // 2. Get My Orders (History)
    @GetMapping
    public ResponseEntity<ApiResponse<List<Order>>> getMyOrders(Authentication authentication) {
        List<Order> orders = orderService.getUserOrders(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(orders, "Orders retrieved successfully"));
    }

    // 3. Get Single Order Details
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Order>> getOrderById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Order order = orderService.getOrderById(id, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(order, "Order details retrieved successfully"));
    }
}