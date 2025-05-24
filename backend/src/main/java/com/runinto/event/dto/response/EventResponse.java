package com.runinto.event.dto.response;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventCategory;
import com.runinto.event.domain.EventType;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
@RequiredArgsConstructor
public class EventResponse {
    private Long eventId;
    private String title;
    private String description;
    private int maxParticipants;
    private Time creationTime;
    private double latitude;
    private double longitude;
    private Long chatroomId;
    private boolean isPublic;
    private int participants;
    private Set<EventCategory> eventCategories;

    public EventResponse(Long eventId, String description, Long chatroomId, String title, int participants, int maxParticipants, Time creationTime, double latitude, double longitude, boolean aPublic, Set<EventCategory> eventCategories) {
        this.eventId = eventId;
        this.description = description;
        this.chatroomId = chatroomId;
        this.title = title;
        this.participants = participants;
        this.maxParticipants = maxParticipants;
        this.creationTime = creationTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isPublic = aPublic;
        this.eventCategories = eventCategories;
    }

    public static EventResponse from(final Event event) {
        return new EventResponse(event.getId(), event.getDescription(),event.getId(),
                event.getTitle(), event.getChatroom().getParticipants().size(), event.getMaxParticipants(), event.getCreationTime(),
                event.getLatitude(), event.getLongitude(),event.isPublic(),event.getEventCategories());
    }

    public static List<EventResponse> from(final List<Event> events) {
        List<EventResponse> responses = new ArrayList<>();
        for (Event event : events) {
            responses.add(from(event));
        }
        return responses;
    }
}
