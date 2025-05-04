package com.runinto.chat.service;

import com.runinto.chat.domain.repository.chatroom.ChatroomH2Repository;
import com.runinto.chat.domain.repository.chatroom.ChatroomRepositoryImple;
import com.runinto.chat.domain.repository.message.ChatMessage;
import com.runinto.chat.domain.repository.chatroom.Chatroom;
import com.runinto.chat.domain.repository.message.ChatMessageH2Repository;
import com.runinto.chat.domain.repository.message.ChatMessageRepositoryImple;
import com.runinto.user.domain.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    private final ChatroomRepositoryImple chatroomRepository;
    private final ChatMessageRepositoryImple chatMessageRepository;

    public ChatService(ChatroomH2Repository chatroomH2Repository, ChatMessageH2Repository chatMessageH2Repository) {
        this.chatroomRepository = chatroomH2Repository;
        this.chatMessageRepository = chatMessageH2Repository;
    }

    public Optional<Chatroom> findChatroomById(Long id) {
        return chatroomRepository.getChatroom(id);
    }

    public Optional<List<ChatMessage>> findChatMessagesByRoom(Chatroom chatroom) {
        return chatMessageRepository.getAllMessages(chatroom);
    }

    public Optional<List<ChatMessage>> findAllMessages() {
        // Since there's no direct equivalent, we might need to implement this differently
        // For now, returning an empty optional
        return Optional.empty();
    }

    public List<ChatMessage> findAll() {
        // This method is used in tests
        // For now, returning an empty list
        return new ArrayList<>();
    }

    @Transactional
    public void clear() {
        // This method is used in tests to clear all chat messages
        chatMessageRepository.clear();
    }

    @Transactional
    public Chatroom createChatRoomForEvent(Long eventId) {
        Chatroom chatroom = Chatroom.builder().build();
        chatroomRepository.save(chatroom);
        return chatroom;
    }

    @Transactional
    public ChatMessage sendMessage(ChatMessage message) {
        chatMessageRepository.save(message);
        return message;
    }

    @Transactional
    public void deleteChatroom(Long id) {
        chatroomRepository.deleteChatroom(id);
    }

    @Transactional
    public void addParticipant(Chatroom chatroom, User user) {
        chatroom.addParticipant(user);
        chatroomRepository.save(chatroom);
    }

    @Transactional
    public void removeParticipant(Chatroom chatroom, Long userId) {
        chatroom.removeParticipant(userId);
        chatroomRepository.save(chatroom);
    }
}
