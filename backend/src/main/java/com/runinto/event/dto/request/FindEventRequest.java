package com.runinto.event.dto.request;

import com.runinto.event.domain.EventType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FindEventRequest {

    @Min(-90)
    @Max(90)
    private Double nelatitude;

    @Min(-180)
    @Max(180)
    private Double nelongitude;

    @Min(-90)
    @Max(90)
    private Double swlatitude;

    @Min(-180)
    @Max(180)
    private Double swlongitude;

    private Set<EventType> categories;
}
