package com.runinto.event.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class JoinEventRequest {
    private Long userId;
}