package com.swe2project.yarncraft.modules.user.controller;

import com.swe2project.yarncraft.common.dto.ApiResponse;
import com.swe2project.yarncraft.modules.user.dto.VendorPerformanceDto;
import com.swe2project.yarncraft.modules.user.entity.VendorApplication;
import com.swe2project.yarncraft.modules.user.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping("/applications")
    public ResponseEntity<ApiResponse<List<VendorApplication>>> getPendingApplications() {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getAllApplications(),
                "Pending applications fetched successfully"
        ));
    }

    @PostMapping("/applications/{id}/approve")
    public ResponseEntity<ApiResponse<String>> approveVendor(@PathVariable Long id) {
        userService.approveVendor(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Vendor approved",
                "Vendor approved successfully."
        ));
    }

    @PostMapping("/applications/{id}/reject")
    public ResponseEntity<ApiResponse<String>> rejectVendor(@PathVariable Long id) {
        userService.rejectVendor(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Vendor rejected",
                "Vendor application rejected. They can now submit a new one."
        ));
    }

    @GetMapping("/vendor-stats")
    public ResponseEntity<ApiResponse<List<VendorPerformanceDto>>> getVendorStats() {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getVendorPerformanceStats(),
                "Vendor performance stats fetched successfully"
        ));
    }
}