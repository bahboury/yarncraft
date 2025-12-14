package com.swe2project.yarncraft.security;

import com.swe2project.yarncraft.modules.user.repository.UserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {


    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Use the UserRepository to find the user in the MySQL DB
        // 2. If found, return the User object (which implements UserDetails)
        return userRepository.findByEmail(email)
                // 3. If NOT found, throw an error so Spring knows to reject the login
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
