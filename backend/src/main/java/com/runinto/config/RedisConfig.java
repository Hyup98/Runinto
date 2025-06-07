package com.runinto.config; // 실제 프로젝트의 설정 패키지 경로로 수정해주세요.

import com.runinto.config.properties.RedisCacheProperties;
import com.runinto.config.properties.RedisSessionProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession // Redis를 HTTP 세션 저장소로 사용하도록 활성화합니다.
@EnableConfigurationProperties({RedisSessionProperties.class, RedisCacheProperties.class})
public class RedisConfig {

    //세션용 Redis Connection Factory 생성 ---
    @Primary
    @Bean(name = "sessionRedisConnectionFactory")
    public RedisConnectionFactory sessionRedisConnectionFactory(RedisSessionProperties sessionProperties) {
        return new LettuceConnectionFactory(sessionProperties.getHost(), sessionProperties.getPort());
    }

    //캐시용 Redis Connection Factory 생성 ---
    @Bean(name = "cacheRedisConnectionFactory")
    public RedisConnectionFactory cacheRedisConnectionFactory(RedisCacheProperties cacheProperties) {
        return new LettuceConnectionFactory(cacheProperties.getHost(), cacheProperties.getPort());
    }

    // --- 3. 세션용 RedisTemplate 생성 ---
    @Bean(name = "sessionRedisTemplate")
    // 주입받고 싶은 빈의 이름을 @Qualifier로 명확하게 지정합니다.
    public RedisTemplate<String, Object> sessionRedisTemplate(
            @Qualifier("sessionRedisConnectionFactory") RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    // --- 4. 캐시용 RedisTemplate 생성 ---
    @Bean(name = "cacheRedisTemplate")
    // 마찬가지로 @Qualifier를 사용하여 "cacheRedisConnectionFactory"를 명시적으로 주입받습니다.
    public RedisTemplate<String, Object> cacheRedisTemplate(
            @Qualifier("cacheRedisConnectionFactory") RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}