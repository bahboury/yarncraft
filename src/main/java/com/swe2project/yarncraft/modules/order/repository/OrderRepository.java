package com.swe2project.yarncraft.modules.order.repository;

import com.swe2project.yarncraft.modules.order.entity.Order;
import com.swe2project.yarncraft.modules.user.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Find all orders for a specific customer (Used for "My Orders" page)
    List<Order> findByCustomer(User customer);
}