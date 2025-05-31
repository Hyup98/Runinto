package com.runinto.chat.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageRequest {
    private Long senderId;
    private String message;
    private Long chatRoomId;
}