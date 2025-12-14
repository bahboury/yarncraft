package com.swe2project.yarncraft.modules.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Check if the vendor has been approved by an admin
    // Default is false for vendors, true for customers/admins
    @Builder.Default
    @Column(name = "is_approved")
    private boolean isApproved = false;

    // --- UserDetails Implementation (Spring Security) ---

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email; // We use email as the username
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // --- Additional Helper Methods for Inventory Module ---

    /**
     * Check if user has ADMIN role
     */
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    /**
     * Check if user has VENDOR role
     */
    public boolean isVendor() {
        return this.role == Role.VENDOR;
    }

    /**
     * Check if user has CUSTOMER role
     */
    public boolean isCustomer() {
        return this.role == Role.CUSTOMER;
    }

    /**
     * Check if user is an approved vendor
     * Only vendors can be approved, others are automatically "approved"
     */
    public boolean isApprovedVendor() {
        return this.role == Role.VENDOR && this.isApproved;
    }

    /**
     * Check if user can manage inventory
     * Admin can manage all, approved vendors can manage their own
     */
    public boolean canManageInventory() {
        return this.role == Role.ADMIN || (this.role == Role.VENDOR && this.isApproved);
    }

    /**
     * Get user's display name for UI
     */
    public String getDisplayName() {
        return this.name != null ? this.name : this.email;
    }
}
