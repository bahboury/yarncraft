package com.swe2project.yarncraft.modules.user.service;

import com.swe2project.yarncraft.modules.user.dto.VendorApplicationDto;
import com.swe2project.yarncraft.modules.user.entity.ApplicationStatus;
import com.swe2project.yarncraft.modules.user.entity.Role;
import com.swe2project.yarncraft.modules.user.entity.User;
import com.swe2project.yarncraft.modules.user.entity.VendorApplication;
import com.swe2project.yarncraft.modules.user.repository.UserRepository;
import com.swe2project.yarncraft.modules.user.repository.VendorApplicationRepository;

import org.springframework.stereotype.Service;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final VendorApplicationRepository vendorApplicationRepository;

    // --- 1. Vendor Logic: Submit Application ---
    public void applyAsVendor(String userEmail, VendorApplicationDto dto) {
        // Find the user who is logged in
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if they are actually a VENDOR
        if (user.getRole() != Role.VENDOR) {
            throw new RuntimeException("Only Vendors can submit shop applications.");
        }

        // Check if they already applied
        if (vendorApplicationRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("You have already submitted an application.");
        }

        // Create the application
        var application = VendorApplication.builder()
                .shopName(dto.getShopName())
                .description(dto.getDescription())
                .status(ApplicationStatus.PENDING) // Default status
                .user(user)
                .build();

        vendorApplicationRepository.save(application);
    }

    // --- 2. Admin Logic: View Pending Applications ---
    public List<VendorApplication> getAllPendingApplications() {
        return vendorApplicationRepository.findByStatus(ApplicationStatus.PENDING);
    }

    // --- 3. Admin Logic: Approve Vendor ---
    public void approveVendor(Long applicationId) {
        // Find the application
        VendorApplication application = vendorApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        // Update Application Status
        application.setStatus(ApplicationStatus.APPROVED);
        vendorApplicationRepository.save(application);

        // Update User Status (Enable their account)
        User vendor = application.getUser();
        vendor.setApproved(true);
        userRepository.save(vendor);
    }
}