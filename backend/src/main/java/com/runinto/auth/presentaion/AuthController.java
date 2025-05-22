package com.runinto.auth.presentaion;

import com.runinto.auth.domain.SessionConst;
import com.runinto.auth.dto.request.LoginRequest;
import com.runinto.auth.dto.request.LogoutRequest;
import com.runinto.auth.dto.request.SignupRequest;
import com.runinto.auth.service.AuthService;
import com.runinto.user.domain.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(final AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signin")
    public ResponseEntity<String> signin(
            @RequestBody LoginRequest loginRequest, 
            HttpServletRequest request) {

        authService.login(loginRequest.getEmail(), loginRequest.getPassword(), request);

        return ResponseEntity.ok("Login Successful");
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.ok("Logout Successful");
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignupRequest signupRequest) {

        return ResponseEntity.ok("Signup Successful");
    }

    @GetMapping("test")
    public ResponseEntity<String> testV1() {
        return ResponseEntity.status(HttpStatus.OK).body("Test Successful");
    }
}
