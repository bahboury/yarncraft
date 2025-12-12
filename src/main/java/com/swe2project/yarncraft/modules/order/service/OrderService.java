package com.swe2project.yarncraft.modules.order.service;

import com.swe2project.yarncraft.common.exception.ResourceNotFoundException;
import com.swe2project.yarncraft.modules.inventory.service.InventoryService;
import com.swe2project.yarncraft.modules.order.dto.OrderRequest;
import com.swe2project.yarncraft.modules.order.entity.Customization;
import com.swe2project.yarncraft.modules.order.entity.Order;
import com.swe2project.yarncraft.modules.order.entity.OrderItem;
import com.swe2project.yarncraft.modules.order.repository.OrderRepository;
import com.swe2project.yarncraft.modules.product.entity.Product;
import com.swe2project.yarncraft.modules.product.repository.ProductRepository;
import com.swe2project.yarncraft.modules.user.entity.Role;
import com.swe2project.yarncraft.modules.user.entity.User;
import com.swe2project.yarncraft.modules.user.repository.UserRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.transaction.Transactional;
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

        // 1. Get Customer
        User customer = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 2. Prepare Order Object
        Order order = Order.builder()
                .customer(customer)
                .status("PENDING")
                .paymentMethod(request.getPaymentMethod())
                .createdAt(LocalDateTime.now())
                .build();

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        // 3. Process Each Item in Cart
        for (OrderRequest.OrderItemRequest itemRequest : request.getItems()) {

            // A. Fetch Product
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemRequest.getProductId()));

            // B. CHECK & DEDUCT STOCK (UPDATED TO USE YOUR NEW SERVICE)
            // The decreaseStock method in your new service automatically checks
            // if (quantity <= 0) and if (available < quantity), so we don't need manual checks here!
            try {
                inventoryService.decreaseStock(product.getId(), itemRequest.getQuantity());
            } catch (IllegalStateException e) {
                // Catch the specific error from InventoryService and rethrow it clearly
                throw new RuntimeException("Stock error for product " + product.getName() + ": " + e.getMessage());
            }

            // C. Calculate Price
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalPrice = totalPrice.add(itemTotal);

            // D. Create OrderItem
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();

            // E. Handle Customizations (Color, Size)
            if (itemRequest.getCustomizations() != null) {
                List<Customization> customizations = new ArrayList<>();
                for (Map.Entry<String, String> entry : itemRequest.getCustomizations().entrySet()) {
                    customizations.add(Customization.builder()
                            .orderItem(orderItem)
                            .attributeName(entry.getKey())   // "Color"
                            .attributeValue(entry.getValue()) // "Red"
                            .build());
                }
                orderItem.setCustomizations(customizations);
            }

            orderItems.add(orderItem);
        }

        // 4. Finalize Order
        order.setItems(orderItems);
        order.setTotalPrice(totalPrice);

        return orderRepository.save(order);
    }

    // --- READ OPERATIONS ---

    // Get all orders for the logged-in user
    public List<Order> getUserOrders(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return orderRepository.findByCustomer(user);
    }

    // Optional: Get a specific order by ID (Ensure they own it!)
    public Order getOrderById(Long orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Security Check: Users can only see THEIR OWN orders (Admins see all)
        if (!order.getCustomer().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new RuntimeException("You are not authorized to view this order.");
        }

        return order;
    }
}