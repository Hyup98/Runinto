package com.runinto.config;

import com.runinto.chat.service.CustomWebSocketHandler;
import com.runinto.chat.service.TestCustomWebSocketHandler;
import com.runinto.util.UserHandshakeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class webSocketConfig implements WebSocketConfigurer {

    private final CustomWebSocketHandler customWebSocketHandler;
    private final UserHandshakeInterceptor userHandshakeInterceptor;

    public webSocketConfig(CustomWebSocketHandler customWebSocketHandler, UserHandshakeInterceptor userHandshakeInterceptor, TestCustomWebSocketHandler testCustomWebSocketHandler) {
        this.customWebSocketHandler = customWebSocketHandler;
        this.userHandshakeInterceptor = userHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(customWebSocketHandler, "/ws/chat")
                .addInterceptors(userHandshakeInterceptor)
                .setAllowedOrigins("*");
    }

}
