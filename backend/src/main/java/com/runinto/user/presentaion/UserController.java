package com.runinto.user.presentaion;

import com.runinto.user.domain.Gender;
import com.runinto.user.domain.Role;
import com.runinto.user.domain.User;
import com.runinto.user.dto.request.LoginRequest;
import com.runinto.user.dto.request.RegisterRequest;
import com.runinto.user.dto.request.UpdateProfileRequest;
import com.runinto.user.dto.response.EventResponse;
import com.runinto.user.dto.response.ProfileResponse;
import com.runinto.user.service.UserService;
import com.runinto.util.ImageStorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Value("${user.default-profile}")
    private String defaultProfilePath;

    private final UserService userService;
    private final ImageStorageService imageStorageService;

    public UserController(UserService userService, ImageStorageService imageStorageService) {
        this.userService = userService;
        this.imageStorageService = imageStorageService;
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> register(
            @RequestPart("profile") RegisterRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile) throws IOException {

        // 1. 이름 / 이메일 중복 검사
        if (userService.existsByName(request.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 이름입니다.");
        }
        if (userService.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 이메일입니다.");
        }

        // 2. 이미지 저장
        String imgUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imgUrl = imageStorageService.saveImage(imageFile);
        }
        else {
            imgUrl = defaultProfilePath;
        }

        // 3. 유저 저장
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .imgUrl(imgUrl)
                .description(request.getDescription())
                .gender(request.getGender())
                .age(request.getAge())
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .build();

        userService.registerUser(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProfileResponse.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<ProfileResponse> login(@RequestBody LoginRequest request) {
        log.info("Login attempt for: {}", request.getEmail());

        User user = userService.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        if (!user.getPassword().equals(request.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .build();
        }

        return ResponseEntity.ok(ProfileResponse.from(user));
    }

    @GetMapping("/profile/{user_id}")
    public ResponseEntity<ProfileResponse> GetProfile(@PathVariable("user_id") Long userId) {
        User user = userService.getUser(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        final ProfileResponse profileResponse = ProfileResponse.from(user);
        return ResponseEntity.ok().body(profileResponse);
    }

    @PatchMapping("/profile/{user_id}")
    public ResponseEntity<ProfileResponse> UpdateProfile(
            @PathVariable("user_id") Long userId,
            @RequestBody UpdateProfileRequest request
    ) {
        User user = userService.getUser(userId).orElse(null);

        if(user == null) {
            return ResponseEntity.notFound().build();
        }

        if (request.getName() != null) user.setName(request.getName());
        if (request.getAge() != null && request.getAge() >= 1) user.setAge(request.getAge());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getDescription() != null) user.setDescription(request.getDescription());
        if (request.getImgUrl() != null) user.setImgUrl(request.getImgUrl ());

        userService.saveUser(user);

        ProfileResponse response = ProfileResponse.from(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/joined-events")
    public ResponseEntity<List<EventResponse>> getJoinedEvents(@PathVariable Long userId) {
        List<EventResponse> events = userService.getJoinedEvents(userId);
        return ResponseEntity.ok(events);
    }

}
