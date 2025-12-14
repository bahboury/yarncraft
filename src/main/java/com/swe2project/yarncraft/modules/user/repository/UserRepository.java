package com.swe2project.yarncraft.modules.user.repository;

import com.swe2project.yarncraft.modules.user.entity.Role;
import com.swe2project.yarncraft.modules.user.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String username);

    // Useful for the Admin Dashboard
    List<User> findByRoleAndIsApprovedFalse(Role role);
}
