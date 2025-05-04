package com.runinto.chat.domain.repository.message;

import com.runinto.chat.domain.repository.chatroom.Chatroom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Primary
public class ChatMessageH2Repository implements ChatMessageRepositoryImple{

    private final ChatMessageJpaRepository chatMessageJpaRepository;

    public ChatMessageH2Repository(ChatMessageJpaRepository chatMessageJpaRepository) {
        this.chatMessageJpaRepository = chatMessageJpaRepository;
    }

    @Override
    public Optional<List<ChatMessage>> getMessages(Long chatRoomId) {
        return Optional.empty();
    }

    @Override
    public void save(ChatMessage message) {
        chatMessageJpaRepository.save(message);
    }

    @Override
    public Optional<List<ChatMessage>> getAllMessages(Chatroom chatroom) {
        List<ChatMessage> messages = chatMessageJpaRepository.findByChatroom(chatroom);
        return Optional.ofNullable(messages.isEmpty() ? null : messages);
    }

    @Override
    public void clear() {
    }
}
