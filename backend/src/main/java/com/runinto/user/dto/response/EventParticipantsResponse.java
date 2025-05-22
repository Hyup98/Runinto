package com.runinto.user.dto.response;

import com.runinto.event.domain.EventParticipant;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EventParticipantsResponse {
    Long userId;
    String name;
    String imgUrl;

    public static EventParticipantsResponse from(EventParticipant participant) {
        return new EventParticipantsResponse(participant.getId(), participant.getUser().getName(), participant.getUser().getImgUrl());
    }
}
