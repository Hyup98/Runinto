package com.runinto.chat.domain.repository.message;

import com.runinto.chat.domain.repository.chatroom.Chatroom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Primary
public class ChatMessageH2Repository implements ChatMessageRepositoryImple {

    private final ChatMessageJpaRepository chatMessageJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public ChatMessageH2Repository(ChatMessageJpaRepository chatMessageJpaRepository) {
        this.chatMessageJpaRepository = chatMessageJpaRepository;
    }

    @Override
    public List<ChatMessage> findAll() {
        log.debug("Finding all messages");
        return chatMessageJpaRepository.findAll();
    }

    @Transactional
    @Override
    public ChatMessage save(ChatMessage message) {
        log.debug("Saving message: {}", message);
        return chatMessageJpaRepository.save(message);
    }

    @Transactional
    @Override
    public void deleteAll() {
        log.debug("Deleting all messages");
        chatMessageJpaRepository.deleteAll();
    }

    @Override
    public Optional<List<ChatMessage>> findByChatroomId(Long chatroomId) {
        log.debug("Finding messages by chatroom ID: {}", chatroomId);
        TypedQuery<ChatMessage> query = entityManager.createQuery(
                "SELECT m FROM ChatMessage m WHERE m.chatroom.id = :chatroomId",
                ChatMessage.class);
        query.setParameter("chatroomId", chatroomId);
        List<ChatMessage> messages = query.getResultList();
        return Optional.of(messages); // Return empty list instead of null
    }

    @Override
    public Optional<ChatMessage> findById(Long id) {
        log.debug("Finding message by ID: {}", id);
        return chatMessageJpaRepository.findById(id);
    }

    @Transactional
    @Override
    public void delete(ChatMessage message) {
        log.debug("Deleting message: {}", message);
        chatMessageJpaRepository.delete(message);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        log.debug("Deleting message by ID: {}", id);
        chatMessageJpaRepository.deleteById(id);
    }
}
