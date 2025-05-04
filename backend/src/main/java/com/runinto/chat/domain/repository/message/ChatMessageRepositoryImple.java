package com.runinto.chat.domain.repository.message;

import com.runinto.chat.domain.repository.chatroom.Chatroom;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepositoryImple {
    public Optional<List<ChatMessage>> getMessages(Long chatRoomId);
    public void save(ChatMessage message);
    public Optional<List<ChatMessage>> getAllMessages(Chatroom chatroom);
    public void clear();
}
