package com.runinto.auth.presentaion;

import com.runinto.auth.dto.request.LoginRequest;
import com.runinto.auth.dto.request.LogoutRequest;
import com.runinto.auth.dto.request.SignupRequest;
import com.runinto.auth.service.AuthService;
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

    //uri로 소셜,eamil 구분
    @PostMapping("/signin")
    public ResponseEntity<String> signinV1(@RequestBody LoginRequest loginRequest) {
        if (authService.signin(loginRequest.getId(), loginRequest.getPassword())) {
            return ResponseEntity.ok("Login Successful");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Login Failed");
    }

    /*@PostMapping("/logout")
    public ResponseEntity<String> logoutV1(@RequestBody LogoutRequest logoutRequest) {
        if (authService.logout(logoutRequest.getId())) {
            return ResponseEntity.ok("Login Successful");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Logout Failed");
    }
*/
    //소셜 or 이메일 회원가입
    @PostMapping("/logout")
    public ResponseEntity<String> signupV1(@RequestBody SignupRequest signupRequest) {

        return ResponseEntity.ok("Signup Successful");
    }

    @GetMapping("test")
    public ResponseEntity<String> testV1() {
        return ResponseEntity.status(HttpStatus.OK).body("Test Successful");
    }
}
