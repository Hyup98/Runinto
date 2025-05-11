package com.runinto.user.dto.response;

import com.runinto.chat.dto.ChatroomSummaryDto;
import com.runinto.event.dto.EventSummaryDto;
import com.runinto.user.domain.Gender;
import com.runinto.user.domain.Role;
import com.runinto.user.domain.User;

import java.util.List;

public record UserDetailResponse(
        Long userId,
        String name,
        String email,
        String imgUrl,
        String description,
        Gender gender,
        Integer age,
        Role role,
        List<EventSummaryDto> joinedEvents,
        List<ChatroomSummaryDto> joinedChatrooms
) {
    public static UserDetailResponse from(User user) {
        return new UserDetailResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getImgUrl(),
                user.getDescription(),
                user.getGender(),
                user.getAge(),
                user.getRole(),
                user.getEventParticipants().stream()
                        .map(EventSummaryDto::from)
                        .toList(),
                user.getChatParticipations().stream()
                        .map(p -> ChatroomSummaryDto.from(p.getChatroom()))
                        .toList()
        );
    }
}