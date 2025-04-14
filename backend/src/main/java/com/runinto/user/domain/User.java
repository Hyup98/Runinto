package com.runinto.user.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan
@Getter
@Setter
public class User {
    private Long userId;
    private String name;
    private String imgUrl;
    private String description;
    private Gender gender;
    private Integer age;


    @Builder
    public User(Long userId, String name, String imgUrl, String description, Gender gender, int age) {
        this.userId = userId;
        this.name = name;
        this.imgUrl = imgUrl;
        this.description = description;
        this.gender = gender;
        this.age = age;
    }

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
