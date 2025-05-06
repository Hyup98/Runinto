package com.runinto.chat.dto.response;

import com.runinto.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponse {
    private Long userId;
    private String name;
    private String imgUrl;
    private String description;
    private Integer age;

    /**
     * Create a ParticipantResponse from a User
     * @param user the user
     * @return a ParticipantResponse
     */
    public static ParticipantResponse from(User user) {
        return ParticipantResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .imgUrl(user.getImgUrl())
                .description(user.getDescription())
                .age(user.getAge())
                .build();
    }
}
