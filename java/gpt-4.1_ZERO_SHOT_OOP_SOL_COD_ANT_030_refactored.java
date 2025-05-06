package com.example.websocket.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.configuration.EnableWebSocketMessageBroker;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.messaging.access.intercept.MessageSecurityMetadataSourceRegistryCustomizer;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/queue", "/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    // Other WebSocket configuration (if needed)
}

@Configuration
public class WebSocketSecurityConfig {

    @Bean
    public MessageSecurityMetadataSourceRegistryCustomizer webSocketSecurityCustomizer() {
        return new WebSocketSecurityCustomizer();
    }

    private static class WebSocketSecurityCustomizer implements MessageSecurityMetadataSourceRegistryCustomizer {
        @Override
        public void customize(MessageSecurityMetadataSourceRegistry messages) {
            messages
                .simpMessageDestMatchers("/queue/**", "/topic/**").denyAll()
                .simpSubscribeDestMatchers("/queue/**/*-user*", "/topic/**/*-user*").denyAll()
                .anyMessage().authenticated();
        }
    }
}