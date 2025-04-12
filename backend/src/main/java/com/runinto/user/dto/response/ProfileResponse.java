package com.runinto.user.dto.response;


import com.runinto.user.domain.Gender;
import com.runinto.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProfileResponse {
    private final String name;
    private final String imgUrl;
    private final String description;
    private final Gender gender;
    private final int age;

    public static ProfileResponse from(final User user) {
        return new ProfileResponse(user.getName(), user.getImgUrl(), user.getDescription(), user.getGender(), user.getAge());
    }
}
