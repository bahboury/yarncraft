package com.swe2project.yarncraft.modules.order.service;

import com.swe2project.yarncraft.common.exception.ResourceNotFoundException;
import com.swe2project.yarncraft.modules.inventory.service.InventoryService;
import com.swe2project.yarncraft.modules.order.dto.OrderRequest;
import com.swe2project.yarncraft.modules.order.entity.Order;
import com.swe2project.yarncraft.modules.order.entity.OrderItem;
import com.swe2project.yarncraft.modules.order.entity.OrderStatus;
import com.swe2project.yarncraft.modules.order.repository.OrderRepository;
import com.swe2project.yarncraft.modules.product.entity.Product;
import com.swe2project.yarncraft.modules.product.repository.ProductRepository;
import com.swe2project.yarncraft.modules.user.entity.Role;
import com.swe2project.yarncraft.modules.user.entity.User;
import com.swe2project.yarncraft.modules.user.repository.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;

    @Transactional
    public Order placeOrder(String userEmail, OrderRequest request) {

        // 1. Get Customer (User)
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Prepare Order Object
        // 🛠️ FIX: Use 'user' instead of 'customer', 'orderDate' instead of 'createdAt'
        Order order = Order.builder()
                .user(user)                        // ✅ Fixed Name
                .status(OrderStatus.PENDING)       // ✅ Fixed Type (Enum)
                // .paymentMethod(...)             // ⚠️ Removing this unless you added it to your Order Entity
                .shippingAddress(request.getShippingAddress()) // ✅ Added Address from Request
                .phone(request.getPhone())         // ✅ Added Phone from Request
                .orderDate(LocalDateTime.now())    // ✅ Fixed Name
                .items(new ArrayList<>())          // Initialize list
                .build();

        BigDecimal calculatedTotal = BigDecimal.ZERO;

        // 3. Process Each Item
        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {

            // A. Fetch Product
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemRequest.getProductId()));

            // B. Deduct Stock
            inventoryService.deductStock(product.getId(), itemRequest.getQuantity());

            // C. Create OrderItem
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .priceAtPurchase(product.getPrice()) // Using DB price is safer
                    .build();

            // D. Handle Customizations (OPTIONAL)
            /* * ⚠️ NOTE: The 'Order' entity I provided earlier did NOT have Customizations.
             * If you want to support this, you must create a Customization Entity
             * and add 'private List<Customization> customizations' to OrderItem.java.
             * For now, I am commenting this out to make the code compile.
             */

            // Add to Order List
            order.getItems().add(orderItem);

            // Add to Total
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            calculatedTotal = calculatedTotal.add(itemTotal);
        }

        // 4. Finalize Order
        // 🛠️ FIX: Use 'setTotalAmount' instead of 'setTotalPrice'
        order.setTotalAmount(calculatedTotal);

        return orderRepository.save(order);
    }

    // --- READ OPERATIONS ---

    public List<Order> getUserOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 👇 MAKE SURE THIS MATCHES YOUR REPOSITORY
        return orderRepository.findByUser(user);
    }

    public Order getOrderById(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 🛠️ FIX: Use 'getUser()' instead of 'getCustomer()'
        if (!order.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("You are not authorized to view this order.");
        }

        return order;
    }
}