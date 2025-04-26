package com.runinto.chat.domain.repository;

import com.runinto.chat.domain.ChatMessage;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ChatMessageMemoryRepository {

    ArrayList<ChatMessage> messages = new ArrayList<>();

    public Optional<List<ChatMessage>> getMessages(Long chatRoomId) {
        List<ChatMessage> ans = new ArrayList<>();
        for (ChatMessage message : messages) {
            if(message.getChatRoomId().equals(chatRoomId)) {
                ans.add(message);
            }
        }
        return Optional.of(ans);
    }

    public void save(ChatMessage message) {
        messages.add(message);
    }

    public Optional<List<ChatMessage>> getAllMessages() {
        List<ChatMessage> ans = new ArrayList<>();
        for (ChatMessage message : messages) {
            ans.add(message);
        }
        return Optional.of(ans);
    }

    public void clear() {
        messages.clear();
    }

}
