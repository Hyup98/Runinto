package com.runinto.chat.service;

import com.runinto.chat.domain.ChatMessage;
import com.runinto.chat.domain.Chatroom;
import com.runinto.chat.domain.repository.ChatMessageMemoryRepository;
import com.runinto.chat.domain.repository.ChatRoomMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.realm.AuthenticatedUserRealm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChatService {
    private final ChatRoomMemoryRepository chatRoomMemoryRepository;
    private final ChatMessageMemoryRepository chatMessageMemoryRepository;

    public ChatService(ChatRoomMemoryRepository chatRoomMemoryRepository, ChatMessageMemoryRepository chatMessageMemoryRepository) {
        this.chatRoomMemoryRepository = chatRoomMemoryRepository;
        this.chatMessageMemoryRepository = chatMessageMemoryRepository;
    }

    public Optional<Chatroom> findChatroomById(Long id) {
        return chatRoomMemoryRepository.getChatroom(id);
    }

    public Optional<List<ChatMessage>> findChatMessagesByRoomId(Long roomId) {
        return chatMessageMemoryRepository.getMessages(roomId);
    }

    public Optional<List<ChatMessage>> findAll() {
        return chatMessageMemoryRepository.getAllMessages();
    }

    public void clear() {
        chatMessageMemoryRepository.clear();
    }

    public Long createChatRoom() {
        chatRoomMemoryRepository.save(new Chatroom(++Chatroom.chatRoomCount));
        return  Chatroom.chatRoomCount;
    }

    public void sendMessage(ChatMessage message) {
        chatMessageMemoryRepository.save(message);
    }

    public void deleteChatroom(Long id) {
        chatRoomMemoryRepository.deleteChatroom(id);
    }

    public void addParticipant(Long chatroomId, Long userId) {
        Optional<Chatroom> chatroom = chatRoomMemoryRepository.getChatroom(chatroomId);
        chatroom.ifPresent(room -> {
            if (room.getApplicants() == null) {
                room.setApplicants(new ArrayList<>());
            }
            room.addApplicant(userId);
            chatRoomMemoryRepository.save(room);
        });
    }

    public void removeParticipant(Long chatroomId, Long userId) {
        Optional<Chatroom> chatroom = chatRoomMemoryRepository.getChatroom(chatroomId);
        chatroom.ifPresent(room -> {
            if (room.getApplicants() != null) {
                room.getApplicants().remove(userId);
                chatRoomMemoryRepository.save(room);
            }
        });
    }


}
