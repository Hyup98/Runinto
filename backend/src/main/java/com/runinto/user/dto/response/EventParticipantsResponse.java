package com.runinto.user.dto.response;

import com.runinto.event.domain.EventParticipant;
import com.runinto.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventParticipantsResponse {
    Long userId;
    String name;
    String imgUrl;

    public static EventParticipantsResponse from(EventParticipant participant) {
        return new EventParticipantsResponse(participant.getUser().getUserId(), participant.getUser().getName(), participant.getUser().getImgUrl());
    }

    public static EventParticipantsResponse from(User participant) {
        return new EventParticipantsResponse(participant.getUserId(), participant.getName(), participant.getImgUrl());
    }
}
