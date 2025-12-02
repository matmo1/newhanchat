package com.newhan.newhanchat.config.websocket;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import java.security.Principal;
import com.newhan.newhanchat.service.UserService;

@Component
public class WebSocketEventListener {

    private final UserService userService;

    public WebSocketEventListener(UserService userService) {
        this.userService = userService;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        Principal user = event.getUser();
        if (user != null) {
            // user.getName() is the userId (set in WebSocketAuthenticator)
            System.out.println("✅ User Connected: " + user.getName());
            userService.connectUser(user.getName());
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        Principal user = event.getUser();
        if (user != null) {
            System.out.println("❌ User Disconnected: " + user.getName());
            userService.disconnectUser(user.getName());
        }
    }
}