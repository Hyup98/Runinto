package com.runinto.chat.domain.repository.chatroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatroomJpaRepository extends JpaRepository<Chatroom, Long> {
    Optional<Chatroom> findByEventId(Long eventId);
}
