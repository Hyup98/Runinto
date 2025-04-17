package com.runinto.config;

import com.runinto.chat.domain.repository.ChatMessageMemoryRepository;
import com.runinto.chat.domain.repository.ChatRoomMemoryRepository;
import com.runinto.chat.service.ChatService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    @Bean
    public ChatMessageMemoryRepository chatMessageMemoryRepository() {
        return new ChatMessageMemoryRepository();
    }

    @Bean
    public ChatRoomMemoryRepository chatRoomMemoryRepository() {
        return new ChatRoomMemoryRepository();
    }

    @Bean
    public ChatService chatService() {
        return new ChatService(chatRoomMemoryRepository(), chatMessageMemoryRepository());
    }
}
