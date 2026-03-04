package com.newhan.postservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // --- EXTREME DEBUGGING ---
        String path = request.getRequestURI();
        // Only log API requests to avoid spam
        if (path.startsWith("/api/")) {
             System.out.println("🔍 Filter checking: " + path);
             String header = request.getHeader("Authorization");
             String param = request.getParameter("token");
             
             if (header == null && param == null) {
                 System.out.println("   ⚠️ No Token found in Header or URL");
             } else if (header != null) {
                 System.out.println("   ✅ Found Header Token: " + header.substring(0, Math.min(15, header.length())) + "...");
             } else {
                 System.out.println("   ✅ Found URL Token: " + param.substring(0, Math.min(15, param.length())) + "...");
             }
        }
        // -------------------------

        try {
            String token = extractToken(request);
            if (token != null) {
                if (validateToken(token)) {
                    Authentication auth = createAuthentication(token);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    System.err.println("❌ Token Invalid for: " + path);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Filter Crash: " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        // Check URL for images
        String paramToken = request.getParameter("token");
        if (paramToken != null && !paramToken.isEmpty()) {
            return paramToken;
        }
        return null;
    }

    private boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            System.err.println("❌ Token Validation Failed: " + e.getMessage());
            return false;
        }
    }

    private Authentication createAuthentication(String token) {
        String userId = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
        return new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
    }
}