package com.runinto.event.dto.request;

import com.runinto.event.domain.EventCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
public class FindEventRequest {
    private double nelatitude;
    private double nelongitude;
    private double swlatitude;
    private double swlongitude;
    List<EventCategory> categories;
}
