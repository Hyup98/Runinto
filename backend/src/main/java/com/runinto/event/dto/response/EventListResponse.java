package com.runinto.event.dto.response;

import com.runinto.event.domain.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class EventListResponse {
    List<Event> events;
}
