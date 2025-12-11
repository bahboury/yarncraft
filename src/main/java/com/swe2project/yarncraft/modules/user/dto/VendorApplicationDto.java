package com.swe2project.yarncraft.modules.user.dto;

import com.swe2project.yarncraft.modules.user.entity.ApplicationStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VendorApplicationDto {
    private Long id;
    private Long userId;
    private String shopName;
    private String description;
    private ApplicationStatus status; // PENDING, APPROVED, REJECTED
}