package com.swe2project.yarncraft.modules.order.entity;

import com.swe2project.yarncraft.modules.product.entity.Product;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;

    private BigDecimal priceAtPurchase; // Price might change later, so we save what they paid!

    // One Item has Many Customizations (Color=Red, Size=M)
    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL)
    private List<Customization> customizations;
}