package com.runinto.user.presentaion;

import com.runinto.event.domain.Event;
import com.runinto.event.dto.response.EventResponse;
import com.runinto.event.service.EventService;
import com.runinto.user.domain.Gender;
import com.runinto.user.domain.Role;
import com.runinto.user.domain.User;
import com.runinto.user.dto.request.UpdateProfileRequest;
import com.runinto.user.dto.response.ProfileResponse;
import com.runinto.user.service.UserService;
import com.runinto.util.ImageStorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Value("${user.default-profile}")
    private String defaultProfilePath;

    private final UserService userService;
    private final ImageStorageService imageStorageService;
    private final EventService eventService;

    public UserController(UserService userService, ImageStorageService imageStorageService, EventService eventService) {
        this.userService = userService;
        this.imageStorageService = imageStorageService;
        this.eventService = eventService;
    }

    @PostConstruct
    public void init() {
        if (!userService.existsByEmail("dummy@example.com")) {
            User dummy = User.builder()
                    .name("더미유저")
                    .email("dummy@example.com")
                    .password("1234") // 테스트용, 실사용 시 인코딩
                    .imgUrl("/img/default.png")
                    .description("테스트 유저입니다.")
                    .gender(Gender.MALE)
                    .age(25)
                    .role(Role.USER)
                    .build();

            userService.registerUser(dummy);
        }
    }

    @PatchMapping(value = "/profile/{user_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfileResponse> UpdateProfile(
            @PathVariable("user_id") Long userId,
            @RequestPart("profile") UpdateProfileRequest request,
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

    @GetMapping("/{userId}/created-events")
    public ResponseEntity<List<EventResponse>> getCreatedEvents(@PathVariable Long userId) {
        // 1. 새로 만든 서비스 메소드를 호출하여 DB에서 필요한 이벤트만 효율적으로 가져옵니다.
        List<Event> createdEvents = eventService.findCreatedByUserId(userId);

        // 2. 조회된 엔티티 목록을 클라이언트로 보낼 DTO 목록으로 변환합니다.
        List<EventResponse> createdEventResponses = createdEvents.stream()
                .map(EventResponse::from)
                .collect(Collectors.toList());

        // 3. 변환된 DTO 목록을 응답합니다.
        return ResponseEntity.ok(createdEventResponses);
    }
}
