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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Transactional
    @PostMapping("/register")
    public ResponseEntity<ProfileResponse> register(@RequestBody RegisterRequest request) {
        log.info("Registering new user: {}", request.getName());

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .imgUrl(request.getImgUrl())
                .description(request.getDescription())
                .gender(request.getGender())
                .age(request.getAge())
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .build();

        userService.saveUser(user);

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
