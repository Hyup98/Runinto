package com.runinto.chat.dto.response;

import com.runinto.chat.domain.repository.message.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;

/**
 * Response DTO for chat messages
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponse {
    private Long id;
    private Long chatRoomId;
    private Long senderId;
    private String message;
    private Time sendTime;

    /**
     * Create a ChatMessageResponse from a ChatMessage
     * @param chatMessage the chat message
     * @return a ChatMessageResponse
     */
    public static ChatMessageResponse from(ChatMessage chatMessage) {
        return ChatMessageResponse.builder()
                .id(chatMessage.getId())
                .chatRoomId(chatMessage.getChatroom().getId())
                .senderId(chatMessage.getSenderId())
                .message(chatMessage.getMessage())
                .sendTime(chatMessage.getSendTime())
                .build();
    }
}
