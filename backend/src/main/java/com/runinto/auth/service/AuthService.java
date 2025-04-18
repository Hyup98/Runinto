package com.runinto.auth.service;

import com.runinto.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Autowired
    private final UserService userService;

    public boolean signin(String id, String password) {
        return userService.getUser(Long.valueOf(id)).isPresent();
    }

    public boolean logout(String id) {
        return userService.getUser(Long.valueOf(id)).isEmpty();
    }

    //토큰 재발급

    //소셜 로그인
}
