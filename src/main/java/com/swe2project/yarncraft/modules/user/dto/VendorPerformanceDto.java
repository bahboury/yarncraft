package com.swe2project.yarncraft.modules.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VendorPerformanceDto {
    private Long vendorId;
    private String vendorName;
    private String shopName;
    private long totalProducts;
    private long totalSold;
    private double totalRevenue;
}