package com.newhan.apigateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.RouteLocator;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApiGatewayApplicationTests {

    @Autowired
    private RouteLocator routeLocator;

    @Test
    void contextLoads() {
        // If the application context fails to start (e.g. bad config), this test fails.
        assertThat(routeLocator).isNotNull();
    }

    @Test
    void verifyRoutesExist() {
        // Get all routes defined in application.properties
        var routes = routeLocator.getRoutes().collectList().block();

        assertThat(routes).isNotNull();
        
        // We expect 3 routes: chat-service, chat-websocket, post-service
        assertThat(routes).hasSize(4);

        // Verify specific Route IDs exist
        assertThat(routes).anyMatch(r -> r.getId().equals("chat-service"));
        assertThat(routes).anyMatch(r -> r.getId().equals("chat-websocket"));
        assertThat(routes).anyMatch(r -> r.getId().equals("post-service"));
        assertThat(routes).anyMatch(r -> r.getId().equals("user-service"));
    }
}