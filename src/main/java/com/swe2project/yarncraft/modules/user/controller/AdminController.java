package com.swe2project.yarncraft.modules.user.controller;

import com.swe2project.yarncraft.modules.user.entity.VendorApplication;
import com.swe2project.yarncraft.modules.user.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import lombok.RequiredArgsConstructor;

// ... imports ...

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    @GetMapping("/applications")
    public ResponseEntity<List<VendorApplication>> getPendingApplications() {
        return ResponseEntity.ok(userService.getAllPendingApplications());
    }

    @PostMapping("/applications/{id}/approve")
    public ResponseEntity<String> approveVendor(@PathVariable Long id) {
        userService.approveVendor(id);
        return ResponseEntity.ok("Vendor approved successfully.");
    }

    // --- NEW ENDPOINT ---
    @PostMapping("/applications/{id}/reject")
    public ResponseEntity<String> rejectVendor(@PathVariable Long id) {
        userService.rejectVendor(id);
        return ResponseEntity.ok("Vendor application rejected. They can now submit a new one.");
    }
}