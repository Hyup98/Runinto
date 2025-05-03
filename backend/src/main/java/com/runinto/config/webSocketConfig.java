package com.runinto.config;

import com.runinto.chat.domain.CustomWebSocketHandler;
import com.runinto.util.UserHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class webSocketConfig implements WebSocketConfigurer {

    private final CustomWebSocketHandler customWebSocketHandler;

    public webSocketConfig(CustomWebSocketHandler customWebSocketHandler) {
        this.customWebSocketHandler = customWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(customWebSocketHandler, "/ws/chat")
                //.addInterceptors(new AuthHandshakeInterceptor()) // (인증 처리: 나중에 활성화)
                .addInterceptors(new UserHandshakeInterceptor())
                .setAllowedOrigins("*");
    }
}
