package com.runinto.chat.domain;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMessage {
    private Long id;
    private Chatroom chatroom;
    private Long senderId;
    private String message;
    private LocalDateTime timestamp;
}
