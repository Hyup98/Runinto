package com.runinto.chat.domain.repository.chatroom;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ChatroomRepositoryImple {

    Optional<Chatroom> findById(Long id);

    Chatroom save(Chatroom chatroom);

    void deleteById(Long id);

    void delete(Chatroom chatroom);

    void deleteAll();

    Optional<Chatroom> findByEventId(Long eventId);

    List<Chatroom> findAll();

    public Optional<Set<ChatroomParticipant>> findChatroomParticipant (Long id);
}
