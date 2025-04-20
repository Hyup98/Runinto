package com.runinto.user.dto.request;

import com.runinto.user.domain.Gender;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UpdateProfileRequest {
    private String name;
    private Integer age;
    private Gender gender;
    private String description;
    private String profileImg;  // profile_img는 자바에서는 카멜 케이스로
}