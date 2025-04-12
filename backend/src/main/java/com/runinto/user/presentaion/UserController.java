package com.runinto.user.presentaion;

import com.runinto.user.domain.User;
import com.runinto.user.dto.request.UpdateProfileRequest;
import com.runinto.user.dto.response.ProfileResponse;
import com.runinto.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile/{user_id}")
    public ResponseEntity<ProfileResponse> GetProfile(@PathVariable("user_id") Long userId) {
        //여기 고민
        User user = userService.getUser(userId).orElseThrow();
        final ProfileResponse profileResponse = ProfileResponse.from(user);
        return ResponseEntity.ok().body(profileResponse);
    }

    @PatchMapping("/profile/{user_id}")
    public ResponseEntity<ProfileResponse> UpdateProfile(
            @PathVariable("user_id") Long userId,
            @RequestBody UpdateProfileRequest request
    ) {
        // 1. 사용자 조회 -> 여기 고민
        User user = userService.getUser(userId).orElseThrow();

        // 2. 수정 가능한 필드만 업데이트
        if (request.getName() != null) user.setName(request.getName());
        if (request.getAge() != null) user.setAge(request.getAge());
        if (request.getSex() != null) user.setSex(request.getSex());
        if (request.getIntro() != null) user.setIntro(request.getIntro());
        if (request.getProfileImg() != null) user.setProfileImageUrl(request.getProfileImg());

        userService.saveUser(user);

        // 3. 응답 객체 생성
        ProfileResponse response = ProfileResponse.from(user);
        return ResponseEntity.ok(response);
    }
}
