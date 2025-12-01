package com.newhan.newhanchat.config.websocket; // Check package name matches your file

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final WebSocketAuthenticator webSocketAuthenticator;

    public WebSocketConfig(WebSocketAuthenticator webSocketAuthenticator) {
        this.webSocketAuthenticator = webSocketAuthenticator;
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // FIX: Remove .withSockJS()
        // This allows raw WebSockets (Android/iOS) to connect directly
        registry.addEndpoint("/ws-chat")
            .setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/queue");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthenticator);
    }
}