package com.runinto.chat.domain.repository.message;

import com.runinto.chat.domain.repository.chatroom.Chatroom;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Time;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chat_message")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatroom_id")
    private Chatroom chatroom;

    @Column(nullable = false)
    private Long senderId;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private Time sendTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessage chatMessage)) return false;
        return id != null && id.equals(chatMessage.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}