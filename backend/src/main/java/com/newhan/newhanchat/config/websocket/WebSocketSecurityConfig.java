package com.newhan.newhanchat.config.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.ChannelInterceptor; // Import this
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.context.SecurityContextChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

import static org.springframework.messaging.simp.SimpMessageType.*;

@Configuration
@EnableWebSocket
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

    // --- FIX: Disable CSRF for WebSockets ---
    // This overrides the default CsrfChannelInterceptor with a no-op one.
    @Bean
    public ChannelInterceptor csrfChannelInterceptor() {
        return new ChannelInterceptor() {};
    }
}