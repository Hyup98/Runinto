package com.runinto.user.domain;

import lombok.*;

@Data
@AllArgsConstructor
public class User {
    private Long userId;
    private String name;
    private String imgUrl;
    private String description;
    private Gender gender;
    private Integer age;

    private String role;
    private String password;
    private String email;

    public void setSex(Gender sex) {
        gender = sex;
    }

    public void setIntro(String newDescription) {
        description = newDescription;
    }

    public void setProfileImageUrl(String profileImg) {
        imgUrl = profileImg;
    }
}
