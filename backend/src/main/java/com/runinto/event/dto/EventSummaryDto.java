package com.runinto.event.dto;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventParticipant;

public record EventSummaryDto(Long eventId, String title) {
    public static EventSummaryDto from(EventParticipant ep) {
        Event e = ep.getEvent();
        return new EventSummaryDto(e.getId(), e.getTitle());
    }
}