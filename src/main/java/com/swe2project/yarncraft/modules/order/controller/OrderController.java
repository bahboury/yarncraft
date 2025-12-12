package com.swe2project.yarncraft.modules.order.controller;

import com.swe2project.yarncraft.common.dto.ApiResponse;
import com.swe2project.yarncraft.modules.order.dto.OrderRequest;
import com.swe2project.yarncraft.modules.order.entity.Order;
import com.swe2project.yarncraft.modules.order.service.OrderService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<Order>> placeOrder(
            @RequestBody OrderRequest request,
            Authentication authentication
    ) {
        Order order = orderService.placeOrder(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success(order, "Order placed successfully!"));
    }
}