package com.runinto.chat.domain;

import lombok.Builder;
import lombok.Data;

import java.sql.Time;

@Data
@Builder
public class ChatMessage {
    private Long chatRoomId;
    private Long senderId;
    private String message;
    private Time sendTime;
}
