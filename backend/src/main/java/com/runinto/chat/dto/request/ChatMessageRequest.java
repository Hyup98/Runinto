package com.runinto.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class ChatMessageRequest {
    private Long senderId;
    private String message;
    private Long chatRoomId;
}