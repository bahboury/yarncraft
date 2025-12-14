package com.swe2project.yarncraft.modules.user.controller;

import com.swe2project.yarncraft.common.dto.ApiResponse;
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
    public ResponseEntity<ApiResponse<String>> applyAsVendor(
            @RequestBody VendorApplicationDto dto,
            Authentication authentication
    ) {
        userService.applyAsVendor(authentication.getName(), dto);
        return ResponseEntity.ok(ApiResponse.success(
                "Application Submitted",
                "Application submitted successfully. Wait for Admin approval."
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileDto>> getMyProfile(Authentication authentication) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getUserProfile(authentication.getName()),
                "User profile fetched successfully"
        ));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<String>> updateProfile(
            @RequestBody com.swe2project.yarncraft.modules.user.dto.UpdateProfileDto dto,
            Authentication authentication
    ) {
        userService.updateUserProfile(authentication.getName(), dto);
        return ResponseEntity.ok(ApiResponse.success(
                "Profile Updated",
                "Profile updated successfully."
        ));
    }
}