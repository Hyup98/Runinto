package com.runinto.auth.service;

import com.runinto.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;

    public boolean login(String id, String password) {
        return userService.getUser(Long.valueOf(id)).isPresent();
    }


}
