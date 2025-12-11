package com.swe2project.yarncraft.modules.user.controller;

import com.swe2project.yarncraft.modules.user.entity.VendorApplication;
import com.swe2project.yarncraft.modules.user.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    // Get all PENDING applications
    @GetMapping("/applications")
    public ResponseEntity<List<VendorApplication>> getPendingApplications() {
        return ResponseEntity.ok(userService.getAllPendingApplications());
    }

    // Approve a specific application
    @PostMapping("/applications/{id}/approve")
    public ResponseEntity<String> approveVendor(@PathVariable Long id) {
        userService.approveVendor(id);
        return ResponseEntity.ok("Vendor approved successfully.");
    }
}