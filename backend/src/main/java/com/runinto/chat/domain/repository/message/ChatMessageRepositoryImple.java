package com.runinto.chat.domain.repository.message;

import com.runinto.chat.domain.repository.chatroom.Chatroom;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepositoryImple {

    List<ChatMessage> findAll();


    ChatMessage save(ChatMessage message);

    void deleteAll();

    Optional<List<ChatMessage>> findByChatroomId(Long chatroomId);

    Optional<ChatMessage> findById(Long id);

    void delete(ChatMessage message);

    void deleteById(Long id);
}
