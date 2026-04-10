package com.newhan.chatservice.config.websocket;

import com.newhan.chatservice.dto.userdtos.UserStatusUpdateEvent;
import com.newhan.chatservice.service.StatusProducerService;
import com.newhan.chatservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final UserService userService;
    // Inject the new Kafka producer service we created
    private final StatusProducerService statusProducerService; 

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        // Grab the username from the authenticated user principal
        if (event.getUser() != null) {
            String username = event.getUser().getName();
            log.info("User Connected: {}", username);
            
            // Tell the User Service master database!
            statusProducerService.sendStatusUpdate(
                new UserStatusUpdateEvent(username, "ONLINE", LocalDateTime.now())
            );
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();

        if (sessionAttributes != null) {
            String username = (String) sessionAttributes.get("username");
            
            // Fallback in case the session attribute isn't set but the user principal is
            if (username == null && event.getUser() != null) {
                username = event.getUser().getName();
            }

            if (username != null) {
                log.info("User Disconnected: {}", username);
                
                // Update your local Chat Service Mongo cache
                userService.disconnect(username); 
                
                // Tell the User Service master database!
                statusProducerService.sendStatusUpdate(
                    new UserStatusUpdateEvent(username, "OFFLINE", LocalDateTime.now())
                );
            }
        }
    }
}