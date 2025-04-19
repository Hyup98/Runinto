package com.runinto.chat.service;

import com.runinto.chat.domain.ChatMessage;
import com.runinto.chat.domain.Chatroom;
import com.runinto.chat.domain.repository.ChatMessageMemoryRepository;
import com.runinto.chat.domain.repository.ChatRoomMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {
    @Autowired
    private final ChatRoomMemoryRepository chatRoomMemoryRepository;
    @Autowired
    private final ChatMessageMemoryRepository chatMessageMemoryRepository;

    public Optional<Chatroom> findChatroomById(Long id) {
        return chatRoomMemoryRepository.getChatroom(id);
    }

    public Optional<List<ChatMessage>> findChatMessagesByRoomId(Long roomId) {
        return chatMessageMemoryRepository.getMessages(roomId);
    }

    public Long createChatRoom() {
        chatRoomMemoryRepository.save(new Chatroom(++Chatroom.ChatRoomCount));
        return  Chatroom.ChatRoomCount;
    }

    public void sendMessage(ChatMessage message) {
        chatMessageMemoryRepository.save(message);
    }


}
