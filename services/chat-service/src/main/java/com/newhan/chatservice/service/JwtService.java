package com.newhan.chatservice.service;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.newhan.chatservice.model.user.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    public String generateToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(user.getUserId().toString())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(1, ChronoUnit.HOURS)))
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes()))
                .compact();
    }
}
