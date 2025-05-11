package com.runinto.chat.dto;

import com.runinto.chat.domain.repository.chatroom.Chatroom;

public record ChatroomSummaryDto(Long id) {
    public static ChatroomSummaryDto from(Chatroom chatroom) {
        return new ChatroomSummaryDto(chatroom.getId());
    }
}