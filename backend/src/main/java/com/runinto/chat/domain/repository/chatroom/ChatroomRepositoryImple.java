package com.runinto.chat.domain.repository.chatroom;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface ChatroomRepositoryImple {
    public Optional<Chatroom> getChatroom(Long id);
    public void save(Chatroom chatroom);
    public void deleteChatroom(Long id);
}
