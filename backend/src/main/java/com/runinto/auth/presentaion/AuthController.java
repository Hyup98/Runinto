package com.runinto.auth.presentaion;

import com.runinto.auth.domain.SessionConst;
import com.runinto.auth.domain.UserSessionDto;
import com.runinto.auth.dto.request.LoginRequest;
import com.runinto.auth.dto.request.LogoutRequest;
import com.runinto.auth.dto.request.SignupRequest;
import com.runinto.auth.dto.response.LoginResponse;
import com.runinto.auth.service.AuthService;
import com.runinto.user.domain.Role;
import com.runinto.user.domain.User;
import com.runinto.user.service.UserService;
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
    public ResponseEntity<LoginResponse> signin(
            @RequestBody LoginRequest loginRequest, 
            HttpServletRequest request) {

        Long userId = authService.login(loginRequest.getEmail(), loginRequest.getPassword(), request).getUserId();

        LoginResponse loginResponse = new LoginResponse(userId);

        return ResponseEntity.ok(loginResponse);
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

    @GetMapping("/profile")
    public ResponseEntity<?> profile(HttpSession session) {
        // 1. 세션에서 UserSessionDto 객체를 가져옵니다.
        Object sessionAttribute = session.getAttribute(SessionConst.LOGIN_MEMBER);

        if (sessionAttribute == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        if (!(sessionAttribute instanceof UserSessionDto)) {
            // 이 경우는 서버 로직 오류이므로 500 에러가 적절합니다.
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("세션 정보가 올바르지 않습니다.");
        }

        UserSessionDto userSession = (UserSessionDto) sessionAttribute;

        // 2. DTO에 담긴 정보로 실제 User 엔티티를 조회합니다.
        //    (주의: 컨트롤러가 아닌 서비스 계층에서 처리하는 것이 더 좋은 설계입니다.)
        User user = userService.findById(userSession.getUserId());

        // 3. 응답 DTO로 변환하여 반환합니다.
        return ResponseEntity.ok(ProfileResponse.from(user));
    }

    @GetMapping("test")
    public ResponseEntity<String> testV1() {
        return ResponseEntity.status(HttpStatus.OK).body("Test Successful");
    }
}
