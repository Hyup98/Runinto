package com.runinto.chat.service;

import com.runinto.chat.domain.repository.chatroom.ChatroomH2Repository;
import com.runinto.chat.domain.repository.chatroom.ChatroomParticipant;
import com.runinto.chat.domain.repository.chatroom.ChatroomRepositoryImple;
import com.runinto.chat.domain.repository.message.ChatMessage;
import com.runinto.chat.domain.repository.chatroom.Chatroom;
import com.runinto.chat.domain.repository.message.ChatMessageH2Repository;
import com.runinto.chat.domain.repository.message.ChatMessageRepositoryImple;
import com.runinto.chat.dto.response.ChatroomResponse;
import com.runinto.event.domain.Event;
import com.runinto.event.domain.repository.EventH2Repository;
import com.runinto.event.domain.repository.EventRepositoryImple;
import com.runinto.user.domain.User;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing chat functionality including chatrooms, messages, and participants
 */
@Slf4j
@Service
public class ChatService {

    private final ChatroomRepositoryImple chatroomRepository;
    private final ChatMessageRepositoryImple chatMessageRepository;
    private final EventRepositoryImple eventRepository;

    public ChatService(ChatroomH2Repository chatroomH2Repository, ChatMessageH2Repository chatMessageH2Repository, EventH2Repository eventH2Repository) {
        this.chatroomRepository = chatroomH2Repository;
        this.chatMessageRepository = chatMessageH2Repository;
        this.eventRepository = eventH2Repository;
    }

    public void clear() {
        log.info("Clearing all chatrooms and messages");
        chatroomRepository.deleteAll();
        chatMessageRepository.deleteAll();
    }

    @Transactional(readOnly = true)
    public Optional<Set<ChatroomParticipant>> findChatroomParticipantByChatroomID(Long chatroomId) {
        return chatroomRepository.findById(chatroomId)
                .map(chatroom -> {
                    chatroom.getParticipants().size(); // 초기화
                    return chatroom.getParticipants();
                });
    }

    @Transactional
    public void save(Chatroom chatroom) {
        chatroomRepository.save(chatroom);
    }

    @Transactional
    public Chatroom createChatRoomForEvent(Long eventId) {
        log.info("Creating chatroom for event with ID: {}", eventId);
        Event event = eventRepository.findById(eventId);

        Optional<Chatroom> existingChatroom = chatroomRepository.findByEventId(eventId);
        if (existingChatroom.isPresent()) {
            log.info("Chatroom already exists for event with ID: {}", eventId);
            return existingChatroom.get();
        }

        Chatroom chatroom = Chatroom.builder()
                .event(event)
                .participants(new HashSet<>())
                .messages(new ArrayList<>())
                .build();

        return chatroomRepository.save(chatroom);
    }

    @Transactional
    public ChatMessage sendMessage(ChatMessage chatMessage) {
        log.debug("Sending message: {}", chatMessage);
        if (chatMessage.getChatroom() == null) {
            throw new IllegalArgumentException("Chatroom cannot be null");
        }
        if (chatMessage.getSenderId() == null) {
            throw new IllegalArgumentException("Sender ID cannot be null");
        }
        if (chatMessage.getMessage() == null || chatMessage.getMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }

        return chatMessageRepository.save(chatMessage);
    }

    @Transactional
    public void deleteChatroom(Long chatroomId) {
        log.info("Deleting chatroom with ID: {}", chatroomId);
        if (!chatroomRepository.findById(chatroomId).isPresent()) {
            throw new EntityNotFoundException("Chatroom not found with id: " + chatroomId);
        }
        chatroomRepository.deleteById(chatroomId);
    }

    @Transactional
    public void deleteChatroomByEventId(Long eventId) {
        log.info("Deleting chatroom for event with ID: {}", eventId);
        Chatroom chatroom = chatroomRepository.findByEventId(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Chatroom not found for eventId: " + eventId));
        chatroomRepository.delete(chatroom);
    }

    public Chatroom getChatroomByEventId(Long eventId) {
        log.debug("Getting chatroom for event with ID: {}", eventId);
        return chatroomRepository.findByEventId(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Chatroom not found for eventId: " + eventId));
    }

    @Transactional
    public void addParticipant(Chatroom chatroom, User user) {
        log.debug("Adding participant: {} to chatroom: {}", user, chatroom);
        if (chatroom.getParticipants() == null) {
            chatroom.setParticipants(new HashSet<>());
        }

        ChatroomParticipant participant = ChatroomParticipant.builder()
                .chatroom(chatroom)
                .user(user)
                .joinedAt(LocalDateTime.now())
                .build();

        chatroom.getParticipants().add(participant);
        chatroomRepository.save(chatroom);
    }

    @Transactional
    public void removeParticipant(Chatroom chatroom, Long userId) {
        log.debug("Removing participant with ID: {} from chatroom: {}", userId, chatroom);
        if (chatroom.getParticipants() == null) {
            return;
        }

        chatroom.getParticipants()
                .removeIf(participant -> participant.getUser().getUserId().equals(userId));

        chatroomRepository.save(chatroom);
    }

    public List<ChatroomResponse> getAllChatrooms() {
        return chatroomRepository.findAll().stream()
                .map(chatroom -> new ChatroomResponse(chatroom.getId()))
                .toList();
    }
}
