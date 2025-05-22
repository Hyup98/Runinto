package com.runinto.auth.service;

import com.runinto.auth.domain.UserSessionDto;
import com.runinto.user.domain.User;
import com.runinto.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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

        UserSessionDto sessionUser = UserSessionDto.fromUser(user);

        HttpSession session = request.getSession(true);
        session.setAttribute(SessionConst.LOGIN_MEMBER, sessionUser);

        return user;
    }

    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }
}
