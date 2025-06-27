// com/runinto/event/dto/cache/EventCacheDto.java
package com.runinto.event.dto.cache;

import com.runinto.event.domain.Event;
import com.runinto.event.dto.EventCategoryInfoForResponseDto;
import com.runinto.user.dto.response.EventParticipantsResponse;

import java.sql.Time;
import java.util.Set;
import java.util.stream.Collectors;

public record EventCacheDto(
        Long eventId,
        String title,
        String description,
        int maxParticipants,
        Time creationTime,
        double latitude,
        double longitude,
        Long chatroomId,
        boolean isPublic,
        EventParticipantsResponse host,
        int participants,
        Set<EventCategoryInfoForResponseDto> eventCategories,
        String gridId // 💡 캐싱에 필요한 gridId 포함
) {
    public static EventCacheDto from(Event event) {
        return new EventCacheDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getMaxParticipants(),
                event.getCreationTime(),
                event.getLatitude(),
                event.getLongitude(),
                event.getChatroom() != null ? event.getChatroom().getId() : null,
                event.isPublic(),
                new EventParticipantsResponse(event.getHost().getUserId(), event.getHost().getName(), event.getHost().getImgUrl()),
                event.getEventParticipants().size(),
                event.getEventCategories().stream()
                        .map(EventCategoryInfoForResponseDto::from)
                        .collect(Collectors.toSet()),
                event.getGridId() // gridId 추가
        );
    }
}