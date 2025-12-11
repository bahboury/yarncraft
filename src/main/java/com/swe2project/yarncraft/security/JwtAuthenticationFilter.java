package com.swe2project.yarncraft.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Look for the "Authorization" header in the incoming request
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // 2. Check: Does the header exist? Does it start with "Bearer "?
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // If no token, let them pass (SecurityConfig will block them later if needed)
            return;
        }

        // 3. Extract the actual token string (remove "Bearer " prefix)
        jwt = authHeader.substring(7);

        // 4. Extract the email from the token (using JwtService)
        userEmail = jwtService.extractUsername(jwt);

        // 5. If we found an email AND they aren't already logged in...
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. Load their details from the database (to check permissions/roles)
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

            // 7. validate: Is the token expired? Does the email match?
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // 8. Create an Authentication Token (Internal Spring Security Object)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities() // Passes their Role (ADMIN, VENDOR) to the context
                );

                // 9. "Stamp" the user as Authenticated in the SecurityContext
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // 10. Pass the request to the next filter (or the Controller)
        filterChain.doFilter(request, response);
    }
}
