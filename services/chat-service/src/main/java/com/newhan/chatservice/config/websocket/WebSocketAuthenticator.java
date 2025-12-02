package com.newhan.chatservice.config.websocket;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class WebSocketAuthenticator implements ChannelInterceptor {

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            
            // --- DEBUG LOGS ---
            System.out.println("WebSocket Authenticator: Received CONNECT frame");
            if (authHeader == null) {
                System.out.println("WebSocket Authenticator: No Authorization header found!");
            } else {
                System.out.println("WebSocket Authenticator: Found header, length: " + authHeader.length());
            }
            // ------------------

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    String userId = Jwts.parserBuilder()
                        .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes()))
                        .build()
                        .parseClaimsJws(token)
                        .getBody()
                        .getSubject();
                    
                    Authentication auth = new UsernamePasswordAuthenticationToken(userId, null, Collections.emptyList());
                    
                    // IMPORTANT: Set the user in the accessor so Spring Security sees it
                    accessor.setUser(auth);
                    SecurityContextHolder.getContext().setAuthentication(auth);
                    
                    System.out.println("WebSocket Authenticator: User authenticated successfully: " + userId);
                } catch (Exception e) {
                    System.out.println("WebSocket Authenticator: Token validation failed: " + e.getMessage());
                }
            }
        }
        return message;
    }
}