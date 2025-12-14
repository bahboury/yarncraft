package com.swe2project.yarncraft.modules.order.repository;

import com.swe2project.yarncraft.modules.order.entity.Order;
import com.swe2project.yarncraft.modules.user.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}