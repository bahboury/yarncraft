package com.swe2project.yarncraft.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

// --- JWT Token Generation and Validation ---
// This class will handle the creation, parsing, and validation of JWT tokens.
// It will typically use a secret key for signing and verifying tokens.
// Methods like generateToken(UserDetails userDetails), extractUsername(String token),
// and validateToken(String token, UserDetails userDetails) will be implemented here.
@Service
public class JwtService {

    // ⚠️ Ideally, store this in application.properties.
    // This is a 256-bit Hex key required for HS256 algorithm
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    // Generates a new Token for a specific user
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .setClaims(extraClaims)                         // 1. Add custom data (if any)
                .setSubject(userDetails.getUsername())          // 2. Save the EMAIL inside the token (Subject = Who this token belongs to)
                .setIssuedAt(new Date(System.currentTimeMillis())) // 3. Timestamp of creation
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 4. Set Expiration (24 Hours from now)
                .signWith(getSignInKey(), SignatureAlgorithm.HS256) // 5. Sign it with our SECRET_KEY so nobody can fake it
                .compact();                                     // 6. Compress into a String
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
