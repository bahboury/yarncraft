package com.swe2project.yarncraft.modules.user.controller;

import com.swe2project.yarncraft.modules.user.dto.UserProfileDto;
import com.swe2project.yarncraft.modules.user.dto.VendorApplicationDto;
import com.swe2project.yarncraft.modules.user.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/vendor-application")
    public ResponseEntity<String> applyAsVendor(@RequestBody VendorApplicationDto dto, Authentication authentication) {
        // 'authentication.getName()' returns the email from the Token
        String email = authentication.getName();
        userService.applyAsVendor(email, dto);
        return ResponseEntity.ok("Application submitted successfully. Wait for Admin approval.");
    }

    // --- NEW: Who Am I? ---
    @GetMapping("/me")
    public ResponseEntity<UserProfileDto> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserProfile(authentication.getName()));
    }

    // --- NEW: Update Profile ---
    @PutMapping("/me")
    public ResponseEntity<String> updateProfile(@RequestBody com.swe2project.yarncraft.modules.user.dto.UpdateProfileDto dto, Authentication authentication) {
        userService.updateUserProfile(authentication.getName(), dto);
        return ResponseEntity.ok("Profile updated successfully.");
    }
}