package com.runinto.event.dto.response;

import com.runinto.chat.domain.Chatroom;
import com.runinto.event.domain.Event;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
public class EventResponse {
    private Long eventId;
    private String title;
    private String description;
    private int maxParticipants;
    private Time creationTime;
    private double latitude;
    private double longitude;
    private Chatroom chatroom;
    private boolean isPublic;
    private int participants;

    public EventResponse(Long eventId, String description, Chatroom chatroom, String title, int participants, int maxParticipants, Time creationTime, double latitude, double longitude, boolean aPublic) {
        this.eventId = eventId;
        this.description = description;
        this.chatroom = chatroom;
        this.title = title;
        this.participants = participants;
        this.maxParticipants = maxParticipants;
        this.creationTime = creationTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isPublic = aPublic;
    }

    public static EventResponse from(final Event event) {
        return new EventResponse(event.getEventId(), event.getDescription(),event.getChatroom(),
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
