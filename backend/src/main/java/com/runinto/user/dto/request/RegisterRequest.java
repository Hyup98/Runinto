package com.runinto.user.dto.request;

import com.runinto.user.domain.Gender;
import com.runinto.user.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*todo
지금은 패스워드를 직렬화로 보내지만 암호화하는 방향 고려
 */
@Getter
@Builder
@AllArgsConstructor
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String description;
    private Gender gender;
    private Integer age;
    private Role role;
}