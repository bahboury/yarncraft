package com.swe2project.yarncraft.modules.user.service;

import com.swe2project.yarncraft.modules.user.dto.UserProfileDto;
import com.swe2project.yarncraft.modules.user.dto.VendorApplicationDto;
import com.swe2project.yarncraft.modules.user.entity.ApplicationStatus;
import com.swe2project.yarncraft.modules.user.entity.Role;
import com.swe2project.yarncraft.modules.user.entity.User;
import com.swe2project.yarncraft.modules.user.entity.VendorApplication;
import com.swe2project.yarncraft.modules.user.repository.UserRepository;
import com.swe2project.yarncraft.modules.user.repository.VendorApplicationRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;
    private final VendorApplicationRepository vendorApplicationRepository;

    // --- 1. Vendor Logic: Submit Application ---
    public void applyAsVendor(String userEmail, VendorApplicationDto dto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != Role.VENDOR) {
            throw new RuntimeException("Only Vendors can submit shop applications.");
        }

        // Check if they already have an application
        Optional<VendorApplication> existingApp = vendorApplicationRepository.findByUserId(user.getId());

        if (existingApp.isPresent()) {
            VendorApplication app = getVendorApplication(dto, existingApp);
            vendorApplicationRepository.save(app);
            return;
        }

        // If no application exists, create a new one
        var application = VendorApplication.builder()
                .shopName(dto.getShopName())
                .description(dto.getDescription())
                .status(ApplicationStatus.PENDING)
                .user(user)
                .build();

        vendorApplicationRepository.save(application);
    }

    private static VendorApplication getVendorApplication(VendorApplicationDto dto, Optional<VendorApplication> existingApp) {
        VendorApplication app = existingApp.get();
        // TRAP FIX: If it's PENDING or APPROVED, stop them.
        // If it's REJECTED, let them overwrite it.
        if (app.getStatus() != ApplicationStatus.REJECTED) {
            throw new RuntimeException("You already have a pending or approved application.");
        }

        // Overwrite the existing REJECTED application with new details
        app.setShopName(dto.getShopName());
        app.setDescription(dto.getDescription());
        app.setStatus(ApplicationStatus.PENDING); // Reset to Pending
        return app;
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

    // --- 4. Admin Logic: Reject Vendor (NEW) ---
    public void rejectVendor(Long applicationId) {
        VendorApplication application = vendorApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        application.setStatus(ApplicationStatus.REJECTED);
        vendorApplicationRepository.save(application);
        // Note: We do NOT enable the user. They remain approved=false.
    }

    // --- 5. Get User Profile (NEW) ---
    public UserProfileDto getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Base Profile
        var profileBuilder = UserProfileDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isApproved(user.isApproved());

        // If Vendor, fetch application details to show status (e.g., "REJECTED - Fix Name")
        if (user.getRole() == Role.VENDOR) {
            Optional<VendorApplication> app = vendorApplicationRepository.findByUserId(user.getId());
            if (app.isPresent()) {
                profileBuilder.shopName(app.get().getShopName());
                profileBuilder.shopDescription(app.get().getDescription());
                profileBuilder.applicationStatus(app.get().getStatus());
            }
        }

        return profileBuilder.build();
    }

    // --- 6. Update Profile (SAFE VERSION) ---
    public void updateUserProfile(String email, com.swe2project.yarncraft.modules.user.dto.UpdateProfileDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Update Basic Info (Always allowed)
        if (dto.getName() != null && !dto.getName().isBlank()) {
            user.setName(dto.getName());
            userRepository.save(user);
        }

        // 2. Update Shop Info (Only if Vendor AND Application exists)
        if (user.getRole() == Role.VENDOR) {
            // Check if application exists, but DO NOT crash if it doesn't
            var appOptional = vendorApplicationRepository.findByUserId(user.getId());

            if (appOptional.isPresent()) {
                VendorApplication app = appOptional.get();
                if (dto.getShopName() != null) app.setShopName(dto.getShopName());
                if (dto.getDescription() != null) app.setDescription(dto.getDescription());
                vendorApplicationRepository.save(app);
            }
        }
    }
}