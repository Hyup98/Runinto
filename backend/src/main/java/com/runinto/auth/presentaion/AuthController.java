package com.runinto.auth.presentaion;

import com.runinto.auth.dto.request.LoginRequest;
import com.runinto.auth.dto.request.LogoutRequest;
import com.runinto.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<String> signinV1(@RequestBody LoginRequest loginRequest) {
        if (authService.signin(loginRequest.getId(), loginRequest.getPassword())) {
            return ResponseEntity.ok("Login Successful");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Login Failed");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logoutV1(@RequestBody LogoutRequest logoutRequest) {
        if (authService.logout(logoutRequest.getId())) {
            return ResponseEntity.ok("Login Successful");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Logout Failed");
    }
}
