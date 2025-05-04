package com.runinto.chat.domain.repository.message;

import com.runinto.chat.domain.repository.chatroom.Chatroom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageJpaRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByChatroom(Chatroom chatroom);
}
