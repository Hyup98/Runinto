package com.runinto.chat.domain.repository.chatroom;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Slf4j
@Repository
@Primary
public class ChatroomH2Repository implements ChatroomRepositoryImple{

    private final ChatRoomMemoryRepository chatRoomMemoryRepository;

    public ChatroomH2Repository(ChatRoomMemoryRepository chatRoomMemoryRepository) {
        this.chatRoomMemoryRepository = chatRoomMemoryRepository;
    }


    @Override
    public Optional<Chatroom> getChatroom(Long id) {
        return Optional.empty();
    }

    @Override
    public void save(Chatroom chatroom) {

    }

    @Override
    public void deleteChatroom(Long id) {

    }
}
