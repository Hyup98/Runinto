package com.runinto.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventListResponse {
    private List<EventResponse> events; // EventResponse DTO의 리스트를 가져야 함
}
