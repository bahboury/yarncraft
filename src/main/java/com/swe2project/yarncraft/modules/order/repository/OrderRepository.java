package com.swe2project.yarncraft.modules.order.repository;

import com.swe2project.yarncraft.modules.order.entity.Order;
import com.swe2project.yarncraft.modules.user.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    @Query("SELECT SUM(oi.product.price * oi.quantity) FROM OrderItem oi WHERE oi.product.vendor.id = :vendorId")
    Double calculateTotalRevenueForVendor(@Param("vendorId") Long vendorId);

    // Calculate Total Items Sold
    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.vendor.id = :vendorId")
    Long calculateTotalItemsSoldForVendor(@Param("vendorId") Long vendorId);
}