package com.runinto.user.dto.request;

import com.runinto.user.domain.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {
    private Long userId;
    private String name;
    private Integer age;
    private String description;
    private String imgUrl;
    private Gender gender;

}