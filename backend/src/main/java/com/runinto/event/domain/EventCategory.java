package com.runinto.event.domain;

import lombok.Builder;
import lombok.Data;

@Data
public class EventCategory {
    private Long id;

    private EventType category;

    private Long eventId;

    @Builder
    public EventCategory(long i, EventType eventType, Long event) {
        this.id = i;
        this.category = eventType;
        this.eventId = event;
    }
}
