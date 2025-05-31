package com.runinto.auth.presentaion;

import com.runinto.auth.domain.SessionConst;
import com.runinto.auth.dto.request.LoginRequest;
import com.runinto.auth.dto.request.LogoutRequest;
import com.runinto.auth.dto.request.SignupRequest;
import com.runinto.auth.service.AuthService;
import com.runinto.user.domain.Role;
import com.runinto.user.domain.User;
import com.runinto.user.dto.request.RegisterRequest;
import com.runinto.user.dto.response.ProfileResponse;
import com.runinto.user.service.UserService;
import com.runinto.util.ImageStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final ImageStorageService imageStorageService;

    public AuthController(final AuthService authService , final UserService userService, final ImageStorageService imageStorageService) {
        this.authService = authService;
        this.userService = userService;
        this.imageStorageService = imageStorageService;
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

    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> signup(@RequestPart("profile") RegisterRequest request,
                                         @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {

        userService.ensureUserNameAndEmailAreUnique(request.getName(), request.getEmail());

        String imgUrl = imageStorageService.saveImage(imageFile);

        User user = userService.registerUser(request, imgUrl);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProfileResponse.from(user));
    }

    @GetMapping("test")
    public ResponseEntity<String> testV1() {
        return ResponseEntity.status(HttpStatus.OK).body("Test Successful");
    }
}
