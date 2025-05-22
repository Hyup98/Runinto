package com.runinto.auth.service;

import com.runinto.exception.user.InvalidPasswordException;
import com.runinto.exception.user.UserNotFoundException;
import com.runinto.user.domain.User;
import com.runinto.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import com.runinto.auth.domain.SessionConst;

@Service
public class AuthService {
    private final UserService userService;

    public AuthService(UserService userService) {
        this.userService = userService;
    }

    public User login(String email, String password, HttpServletRequest request) {
        User user = userService.authenticate(email, password);

        HttpSession session = request.getSession(true);
        session.setAttribute(SessionConst.LOGIN_MEMBER, user);
        session.setMaxInactiveInterval(SessionConst.SESSION_TIMEOUT);

        return user;
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

    }


    //소셜 로그인
}
