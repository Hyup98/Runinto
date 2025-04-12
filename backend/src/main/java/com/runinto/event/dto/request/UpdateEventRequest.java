package com.runinto.event.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class UpdateEventRequest {
    private String title;
    private String description;
    private Integer maxParticipants;
    private Double latitude;
    private Double longitude;
    private Boolean isPublic;
}
