package com.runinto.config;

import com.runinto.user.domain.repository.UserMemoryRepository;
import com.runinto.user.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfig {

    @Bean
    public UserService userService() { return new UserService(userMemoryRepository());}

    @Bean
    public UserMemoryRepository userMemoryRepository() { return new UserMemoryRepository();}
}
