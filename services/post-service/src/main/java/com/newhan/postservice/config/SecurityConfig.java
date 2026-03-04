package com.newhan.postservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod; // <--- Import this
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // 1. Allow Images (Fixes broken images)
                // .requestMatchers("/images/**").permitAll()
                
                // // 2. Allow Reading Posts (Fixes "Error fetching posts: 403")
                // .requestMatchers(HttpMethod.GET, "/api/posts").permitAll()
                
                // // 3. Allow Uploading Images (Fixes "Upload failed: 403")
                // .requestMatchers("/api/posts/upload").permitAll()
                
                // 4. Everything else (Create/Delete) requires a Token
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}