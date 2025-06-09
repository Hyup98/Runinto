package com.runinto.kafka.service;

import com.runinto.kafka.dto.CacheUpdateMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(String topic, Object payload) {
        // 지정된 토픽으로 메시지를 보냅니다.
        kafkaTemplate.send(topic, payload);
    }
}