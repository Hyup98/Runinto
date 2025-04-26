package com.runinto.event.dto.response;

import com.runinto.chat.domain.Chatroom;
import com.runinto.event.domain.Event;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

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

    public EventResponse(Long eventId, String description, Long chatroomId, String title, int participants, int maxParticipants, Time creationTime, double latitude, double longitude, boolean aPublic) {
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
    }

    public static EventResponse from(final Event event) {
        return new EventResponse(event.getEventId(), event.getDescription(),event.getChatroomId(),
                event.getTitle(), event.getParticipants(), event.getMaxParticipants(), event.getCreationTime(),
                event.getLatitude(), event.getLongitude(),event.isPublic());
    }

    public static List<EventResponse> from(final List<Event> events) {
        List<EventResponse> responses = new ArrayList<>();
        for (Event event : events) {
            responses.add(from(event));
        }
        return responses;
    }
}
