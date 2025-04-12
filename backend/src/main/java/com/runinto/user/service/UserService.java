package com.runinto.user.service;

import com.runinto.event.domain.Event;
import com.runinto.user.domain.User;
import com.runinto.user.dto.response.ProfileResponse;
import com.runinto.user.domain.repository.UserMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMemoryRepository userMemoryRepository;

    public Optional<User> getUser(final Long userId) {
        return userMemoryRepository.findById(userId);
    }

    public void saveUser(final User user) {
        userMemoryRepository.save(user);
    }

}
