package com.runinto.auth.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignupRequest {
    String email;
    String password;
    String name;
}
