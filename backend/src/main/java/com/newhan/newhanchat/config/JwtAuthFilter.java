package com.newhan.newhanchat.config; 

import java.io.IOException;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        
        String path = request.getRequestURI();
        
        // --- FIX: Add "/ws-chat" to this check ---
        if (path.startsWith("/api/users/register") || 
            path.startsWith("/api/users/login") ||
            path.startsWith("/ws-chat")) { // Allow WebSocket handshake
            
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = extractToken(request);
            if (token == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing JWT token");
                return;
            }
            
            if (validateToken(token)) {
                Authentication auth = createAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
                filterChain.doFilter(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT");
            }
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
        }
    }

    // ... (rest of the file stays the same: extractToken, validateToken, createAuthentication)
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
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
            return false;
        }
    }

    private Authentication createAuthentication(String token) {
        String username = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();

        return new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
    }
}