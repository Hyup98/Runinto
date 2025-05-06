package com.runinto.user.dto.request;

import com.runinto.user.domain.Gender;
import com.runinto.user.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String imgUrl;
    private String description;
    private Gender gender;
    private Integer age;
    private Role role;
}