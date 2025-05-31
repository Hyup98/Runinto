package com.runinto.event.dto.response;

import com.runinto.event.domain.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class EventListResponse {
    private List<EventResponse> events; // EventResponse DTO의 리스트를 가져야 함

    // List<Event> 엔티티를 받아 List<EventResponse> DTO로 변환하는 생성자 (컨트롤러에서 사용)
    public EventListResponse(List<Event> eventEntities) {
        if (eventEntities != null) {
            this.events = eventEntities.stream()
                    .map(EventResponse::from) // 각 Event 엔티티를 EventResponse DTO로 변환
                    .collect(Collectors.toList());
        } else {
            this.events = List.of(); // null이면 빈 리스트로 초기화
        }
    }
}
