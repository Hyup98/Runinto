package com.runinto.user.dto.response;

import com.runinto.event.domain.Event;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventResponse {
    private Long eventId;
    private String title;

    public static EventResponse from(Event event) {
        return new EventResponse(event.getEventId(), event.getTitle());
    }

}
