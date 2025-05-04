package com.runinto.user.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.user.domain.Gender;
import com.runinto.user.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Repository
public class UserMemoryRepository implements UserRepositoryImple{

    private Map<Long, User> users = new HashMap<>();

    public Optional<User> findById(Long id) {
        log.info("Find User by id: {}", id);
        return Optional.ofNullable(users.get(id));
    };

    public void save(User user) {
        users.put(user.getUserId(), user);
        log.info("Save user: {} , MapSize : {}", user, users.size());
    }

    public void delete(Long id) {
        users.remove(id);
    }

    public int getSize() {
        return users.size();
    }

    @Override
    public List<Event> findJoinedEvents(Long userId) {
        return List.of();
    }
}
