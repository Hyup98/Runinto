package com.runinto.event.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
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
