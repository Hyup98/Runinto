package com.runinto.chat.dto.response;

import com.runinto.chat.domain.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Time;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageResponse {
    private Long chatRoomId;
    private Long senderId;
    private String message;
    private Time sendTime;
    
    public static ChatMessageResponse from(ChatMessage chatMessage) {
        return new ChatMessageResponse(
                chatMessage.getChatRoomId(),
                chatMessage.getSenderId(),
                chatMessage.getMessage(),
                chatMessage.getSendTime()
        );
    }
}