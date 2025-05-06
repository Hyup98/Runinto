package com.runinto.chat.domain.repository.chatroom;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository
@Primary
public class ChatroomH2Repository implements ChatroomRepositoryImple {

    private final ChatroomJpaRepository chatRoomJpaRepository;

    public ChatroomH2Repository(ChatroomJpaRepository chatroomJpaRepository) {
        this.chatRoomJpaRepository = chatroomJpaRepository;
    }

    @Override
    public Optional<Set<ChatroomParticipant>> findChatroomParticipant (Long id) {
        Chatroom chatroom = chatRoomJpaRepository.findById(id).orElse(null);
        if(chatroom != null) {
            return Optional.of(chatroom.getParticipants());
        }
        return Optional.empty();
    }

    @Override
    public Optional<Chatroom> findById(Long id) {
        return chatRoomJpaRepository.findById(id);
    }

    @Transactional
    @Override
    public Chatroom save(Chatroom chatroom) {
        log.debug("Saving chatroom: {}", chatroom);
        return chatRoomJpaRepository.save(chatroom);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        log.debug("Deleting chatroom by ID: {}", id);
        chatRoomJpaRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void delete(Chatroom chatroom) {
        log.debug("Deleting chatroom: {}", chatroom);
        chatRoomJpaRepository.delete(chatroom);
    }

    @Transactional
    @Override
    public void deleteAll() {
        log.debug("Deleting all chatrooms");
        chatRoomJpaRepository.deleteAll();
    }

    @Override
    public Optional<Chatroom> findByEventId(Long eventId) {
        log.debug("Finding chatroom by event ID: {}", eventId);
        return chatRoomJpaRepository.findByEventId(eventId);
    }

    @Override
    public List<Chatroom> findAll() {
        log.debug("Finding all chatrooms");
        return chatRoomJpaRepository.findAll();
    }


}
