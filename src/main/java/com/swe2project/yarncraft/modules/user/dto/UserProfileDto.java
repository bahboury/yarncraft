package com.swe2project.yarncraft.modules.user.dto;

import com.swe2project.yarncraft.modules.user.entity.ApplicationStatus;
import com.swe2project.yarncraft.modules.user.entity.Role;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileDto {
    private Long id;
    private String name;
    private String email;
    private Role role;
    private boolean isApproved; // Crucial for Vendors (false = wait, true = dashboard)

    // Vendor Specific Fields (Optional, null if Customer)
    private String shopName;
    private String shopDescription;
    private ApplicationStatus applicationStatus; // PENDING, REJECTED, APPROVED
}