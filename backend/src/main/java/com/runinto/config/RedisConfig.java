package com.runinto.config; // 실제 프로젝트의 설정 패키지 경로로 수정해주세요.

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession // Redis를 HTTP 세션 저장소로 사용하도록 활성화합니다.
public class RedisConfig {

    // application.yml 파일의 spring.session.redis.host 값을 주입받습니다.
    @Value("${spring.session.redis.host}")
    private String redisHost;

    // application.yml 파일의 spring.session.redis.port 값을 주입받습니다.
    @Value("${spring.session.redis.port}")
    private int redisPort;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 주입받은 host와 port를 사용하여 LettuceConnectionFactory를 생성합니다.
        return new LettuceConnectionFactory(redisHost, redisPort);
    }

    // RedisTemplate 빈 설정 (선택 사항):
    // Spring Session 자체는 RedisConnectionFactory만 있으면 동작합니다.
    // 이 RedisTemplate은 애플리케이션에서 세션 관리 외의 다른 목적으로 Redis에 직접 접근할 때 필요합니다.
    // 필요 없다면 이 빈 정의는 생략해도 됩니다.
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory); // 위에서 생성한 RedisConnectionFactory 사용

        // Key 직렬화 방식: String
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value 직렬화 방식: String (또는 다른 직렬화 방식 선택 가능, 예: GenericJackson2JsonRedisSerializer)
        // Spring Session이 세션 데이터를 저장할 때는 자체적인 직렬화 방식을 사용하므로,
        // 이 RedisTemplate의 Value 직렬화 방식은 세션 저장에 직접적인 영향을 주지 않을 수 있습니다.
        // 하지만 일반적인 데이터 저장 시 일관성을 위해 설정합니다.
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        // template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        // template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }
}