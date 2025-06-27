package com.Runinto.consumer.config;

import common.kafka.dto.CacheUpdateMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    // application.yml 또는 properties에 설정된 Kafka 서버 주소
    private String bootstrapServers = "localhost:9092"; // 실제 환경에 맞게 수정

    @Bean
    public ConsumerFactory<String, CacheUpdateMessage> cacheUpdateConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "cache-management-group");

        // 💡 역직렬화 설정
        JsonDeserializer<CacheUpdateMessage> deserializer = new JsonDeserializer<>(CacheUpdateMessage.class);
        // 신뢰할 수 없는 패키지로부터의 역직렬화를 허용 (개발 편의성, 보안에 유의)
        deserializer.addTrustedPackages("*");

        return new DefaultKafkaConsumerFactory<>(
                props,
                new StringDeserializer(),
                deserializer); // JsonDeserializer를 값(value) 역직렬화기로 사용
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CacheUpdateMessage> cacheUpdateListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CacheUpdateMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cacheUpdateConsumerFactory());
        return factory;
    }
}