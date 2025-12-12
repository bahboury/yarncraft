package com.swe2project.yarncraft.modules.user.repository;

import com.swe2project.yarncraft.modules.user.entity.ApplicationStatus;
import com.swe2project.yarncraft.modules.user.entity.VendorApplication;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorApplicationRepository extends JpaRepository<VendorApplication, Long> {
    // Find application for a specific user
    Optional<VendorApplication> findByUserId(Long userId);

    // Find all pending applications (For the Admin Dashboard)
    List<VendorApplication> findByStatus(ApplicationStatus status);
}