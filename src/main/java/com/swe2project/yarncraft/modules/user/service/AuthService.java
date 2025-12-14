package com.swe2project.yarncraft.modules.user.service;

import com.swe2project.yarncraft.modules.user.dto.AuthResponse;
import com.swe2project.yarncraft.modules.user.dto.LoginRequest;
import com.swe2project.yarncraft.modules.user.dto.RegisterRequest;
import com.swe2project.yarncraft.modules.user.entity.Role;
import com.swe2project.yarncraft.modules.user.entity.User;
import com.swe2project.yarncraft.modules.user.repository.UserRepository;
import com.swe2project.yarncraft.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // Logic for Registering a new user
    public AuthResponse register(RegisterRequest request) {
        // 1. Convert String to Enum manually (Safe & Explicit)
        Role userRole;
        try {
            // .toUpperCase() handles "customer", "Customer", or "CUSTOMER"
            userRole = Role.valueOf(request.getRole().toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException("Invalid Role! Allowed values: CUSTOMER, VENDOR");
        }

        // 2. Build the User (Now we use the Enum!)
        var user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(userRole) // 👈 Passing the Enum to the Entity
                .isApproved(userRole == Role.CUSTOMER)
                .build();

        userRepository.save(user);

        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder().token(jwtToken).build();
    }

    // Logic for Logging in
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthResponse.builder()
                .token(jwtToken)
                .build();
    }

}
