package com.runinto.user.dto.request;

import com.runinto.user.domain.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateProfileRequest {
    private String name;
    private Integer age;
    private Gender sex;
    private String intro;
    private String profileImg;  // profile_img는 자바에서는 카멜 케이스로
}