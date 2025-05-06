package com.runinto.user.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Primary
@RequiredArgsConstructor
public class UserH2Repository implements UserRepositoryImple {

    private final UserJpaRepository userJpaRepository;

    @Override
    public Optional<User> findById(Long id) {
        log.info("Find User by id: {}", id);
        return userJpaRepository.findById(id);
    }

    @Override
    public void save(User user) {
        userJpaRepository.save(user);
        log.info("Saved user: {}", user);
    }

    @Override
    public void delete(Long id) {
        userJpaRepository.deleteById(id);
        log.info("Deleted user with id: {}", id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.info("Find User by email: {}", email);
        return userJpaRepository.findByEmail(email);
    }

    public List<Event> findJoinedEvents(Long userId) {
        return userJpaRepository.findJoinedEvents(userId);
    }

    public void deleteAll() {
        userJpaRepository.deleteAll();
    }
}
