package com.runinto.consumer.kafka;

import common.kafka.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatConsumer {

    private final RedisTemplate<String, Object> redisTemplate;

    @KafkaListener(topics = "chat-messages-topic", containerFactory = "chatMessageListenerContainerFactory")
    public void consume(ChatMessageDto message) {
        log.info("Kafka로부터 채팅 메시지 수신: {}", message.getContent());

        // (선택) 여기에 DB에 채팅 내역을 저장하는 로직 추가
        // chatRepository.save(message);

        // Redis Pub/Sub을 통해 모든 backend 서버에 메시지 전파
        String channel = "chatroom:" + message.getChatroomId();
        redisTemplate.convertAndSend(channel, message);
    }
}