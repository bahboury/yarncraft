package com.swe2project.yarncraft.modules.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileDto {
    private String name;           // For all users
    private String shopName;       // Vendors only
    private String description;    // Vendors only
}