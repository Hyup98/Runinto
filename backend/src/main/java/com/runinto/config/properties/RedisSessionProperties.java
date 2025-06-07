package com.runinto.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.session.redis")
public class RedisSessionProperties {
    private String host;
    private int port;
}