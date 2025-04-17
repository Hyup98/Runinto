package com.runinto.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatMessageRequest {
    private String senderId;
    private String message;
    private String ChatRoomId;
}
