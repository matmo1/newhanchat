package com.newhan.newhanchat.config.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;

import static org.springframework.messaging.simp.SimpMessageType.*;

@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig {

    @Bean
    MessageMatcherDelegatingAuthorizationManager.Builder messageAuthorizationManager() {
        return MessageMatcherDelegatingAuthorizationManager.builder()
            .simpDestMatchers("/app/**").authenticated()
            .simpSubscribeDestMatchers("/user/**").authenticated()
            .simpTypeMatchers(CONNECT, UNSUBSCRIBE, DISCONNECT).permitAll()
            .anyMessage().denyAll();
    }

    @Bean
    SecurityContextChannelInterceptor securityContextChannelInterceptor() {
        return new SecurityContextChannelInterceptor();
    }
}
