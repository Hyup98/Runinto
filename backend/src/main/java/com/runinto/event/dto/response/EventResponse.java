package com.runinto.event.dto.response;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventParticipant;
import com.runinto.event.domain.ParticipationStatus;
import com.runinto.event.dto.EventCategoryInfoForResponseDto;// 💡 EventCacheDto import 추가
import com.runinto.event.dto.cache.EventCacheDto;
import com.runinto.user.dto.response.EventParticipantsResponse;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // 💡 Builder 어노테이션을 사용하면 더 깔끔하게 객체를 생성할 수 있습니다.
public class EventResponse {
    private Long eventId;
    private EventParticipantsResponse host;
    private String title;
    private String description;
    private int maxParticipants;
    private Time creationTime;
    private double latitude;
    private double longitude;
    private Long chatroomId;
    private boolean isPublic;
    private int participants;
    private Set<EventCategoryInfoForResponseDto> eventCategories;

    private ParticipationStatus myParticipationStatus;

    // 기존 from(Event) 메소드
    public static EventResponse from(final Event event) {
        if (event == null) return null;
        int approvedParticipantsCount = 0;

        if (event.getEventParticipants() != null) {
            approvedParticipantsCount = (int) event.getEventParticipants().stream()
                    .filter(p -> p.getStatus() == ParticipationStatus.APPROVED)
                    .count();
        }
        return EventResponse.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .maxParticipants(event.getMaxParticipants())
                .creationTime(event.getCreationTime())
                .latitude(event.getLatitude())
                .longitude(event.getLongitude())
                .chatroomId(event.getChatroom() != null ? event.getChatroom().getId() : null)
                .isPublic(event.isPublic())
                .participants(approvedParticipantsCount)
                .eventCategories(event.getEventCategories().stream()
                        .map(EventCategoryInfoForResponseDto::from)
                        .collect(Collectors.toSet()))
                .host(EventParticipantsResponse.from(event.getHost()))
                .build();
    }

    // 💡 해결책: EventCacheDto를 EventResponse로 변환하는 새로운 from 메소드 추가
    public static EventResponse from(final EventCacheDto cacheDto) {
        if (cacheDto == null) return null;
        return EventResponse.builder()
                .eventId(cacheDto.eventId())
                .title(cacheDto.title())
                .description(cacheDto.description())
                .maxParticipants(cacheDto.maxParticipants())
                .creationTime(cacheDto.creationTime())
                .latitude(cacheDto.latitude())
                .longitude(cacheDto.longitude())
                .chatroomId(cacheDto.chatroomId())
                .isPublic(cacheDto.isPublic())
                .participants(cacheDto.participants())
                .host(cacheDto.host())
                .eventCategories(cacheDto.eventCategories())
                .build();
    }


    public static EventResponse from(final EventParticipant participation) {
        if (participation == null) return null;
        EventResponse response = from(participation.getEvent()); // 기존 from(Event) 재사용
        response.setMyParticipationStatus(participation.getStatus()); // 참여 상태 주입
        return response;
    }

    public static List<EventResponse> from(final List<EventParticipant> participations) {
        return participations.stream()
                .map(EventResponse::from)
                .collect(Collectors.toList());
    }
}