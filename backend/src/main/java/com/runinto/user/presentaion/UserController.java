package com.runinto.user.presentaion;

import com.runinto.user.domain.Role;
import com.runinto.user.domain.User;
import com.runinto.user.dto.request.LoginRequest;
import com.runinto.user.dto.request.RegisterRequest;
import com.runinto.user.dto.request.UpdateProfileRequest;
import com.runinto.user.dto.response.EventResponse;
import com.runinto.user.dto.response.ProfileResponse;
import com.runinto.user.dto.response.UserDetailResponse;
import com.runinto.user.service.UserService;
import com.runinto.util.ImageStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

        userService.ensureUserNameAndEmailAreUnique(request.getName(), request.getEmail());

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

    @PatchMapping(value = "/profile/{user_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> UpdateProfile(
            @PathVariable("user_id") Long userId,
            @RequestBody UpdateProfileRequest request,
            @RequestPart(value = "image", required = false) MultipartFile imageFile
    ) throws IOException {
        User user = userService.findById(userId);

        if (request.getName() != null) user.setName(request.getName());
        if (request.getAge() != null && request.getAge() >= 1) user.setAge(request.getAge());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getDescription() != null) user.setDescription(request.getDescription());
        if (imageFile != null && !imageFile.isEmpty()) {
            String imgUrl = imageStorageService.saveImage(imageFile);
            user.setImgUrl(imgUrl);
        }

        userService.saveUser(user);

        ProfileResponse response = ProfileResponse.from(user);
        return ResponseEntity.ok(response);
    }

    //최초 로그인에서 최대한 많은 정보 가져오기
    //참여중인 채팅방, 참여중인 이벤트, 유저 정보
    //비밀번호 암호화및 평문화 추가해야함
    @PostMapping("/login")
    public ResponseEntity<UserDetailResponse> login(@RequestBody LoginRequest request) {
        User user = userService.authenticate(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(UserDetailResponse.from(user));
    }

    @GetMapping("/profile/{user_id}")
    public ResponseEntity<ProfileResponse> GetProfile(@PathVariable("user_id") Long userId) {
        User user = userService.findById(userId);
        final ProfileResponse profileResponse = ProfileResponse.from(user);
        return ResponseEntity.ok().body(profileResponse);
    }

    @GetMapping("/{userId}/joined-events")
    public ResponseEntity<List<EventResponse>> getJoinedEvents(@PathVariable Long userId) {
        List<EventResponse> events = userService.getJoinedEvents(userId);
        return ResponseEntity.ok(events);
    }

}
