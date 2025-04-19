package com.runinto.event.dto.request;

import com.runinto.event.domain.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Set;

@Data
@AllArgsConstructor
public class FindEventRequest {
    private double nelatitude;
    private double nelongitude;
    private double swlatitude;
    private double swlongitude;
    Set<EventType> categories;
}
