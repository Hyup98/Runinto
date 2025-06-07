package com.runinto.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.cache.redis")
public class RedisCacheProperties {
    private String host;
    private int port;
}