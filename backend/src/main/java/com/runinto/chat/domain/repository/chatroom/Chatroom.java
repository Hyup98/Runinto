package com.runinto.chat.domain.repository.chatroom;

import com.runinto.chat.domain.repository.message.ChatMessage;
import com.runinto.event.domain.Event;
import com.runinto.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "chatroom")
public class Chatroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatroomid;

    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @OneToMany(mappedBy = "chatroom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatMessage> messages = new ArrayList<>();

    @OneToMany(mappedBy = "chatroom", cascade = CascadeType.ALL)
    private Set<ChatroomParticipant> participants;

    public void addParticipant(User user) {
        participants.add(ChatroomParticipant.builder()
                .chatroom(this)
                .user(user)
                .joinedAt(java.time.LocalDateTime.now())
                .build());
    }

    public void removeParticipant(Long userId) {
        participants.removeIf(participant -> participant.getUser().getUserId().equals(userId));
    }

    public void sendMessage(ChatMessage message) {
        messages.add(message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Chatroom chatroom)) return false;
        return chatroomid != null && chatroomid.equals(chatroom.chatroomid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(chatroomid);
    }
}
