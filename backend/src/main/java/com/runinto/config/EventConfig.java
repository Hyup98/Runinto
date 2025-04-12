package com.runinto.config;

import com.runinto.event.domain.repository.EventMemoryRepository;
import com.runinto.event.service.EventService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EventConfig {

    @Bean
    public EventService eventService() {return new EventService(eventMemoryRepository());}

    @Bean
    public EventMemoryRepository eventMemoryRepository() {return new EventMemoryRepository();}
}
