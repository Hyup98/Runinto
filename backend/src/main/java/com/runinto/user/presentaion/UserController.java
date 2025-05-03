package com.runinto.user.presentaion;

import com.runinto.user.domain.User;
import com.runinto.user.dto.request.UpdateProfileRequest;
import com.runinto.user.dto.response.EventResponse;
import com.runinto.user.dto.response.ProfileResponse;
import com.runinto.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
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
