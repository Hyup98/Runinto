package com.runinto.user.domain.repository;

import com.runinto.user.domain.Gender;
import com.runinto.user.domain.User;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class UserMemoryRepository {

    private Map<Long, User> users;

    public Optional<User> findById(Long id) {
        log.info("Find User by id: {}", id);
        //return Optional.ofNullable(users.get(id));
        return Optional.of(new User("name", "IMGURL", "테스트 유저입니다", Gender.MALE, 1));
    };

    public void save(User user) {
        users.put(user.getUserId(), user);
        log.info("Save user: {} , MapSize : {}", user, users.size());
    }
}
