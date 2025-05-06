package com.runinto.chat.domain.repository.chatroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatroomParticipantJPARepository extends JpaRepository<ChatroomParticipant, Long> {

}
