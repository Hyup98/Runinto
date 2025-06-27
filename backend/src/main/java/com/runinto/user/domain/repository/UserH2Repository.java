package com.runinto.user.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventParticipant;
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
public class UserH2Repository {

    private final UserJpaRepository userJpaRepository;

    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id);
        //return userJpaRepository.findWithAssociationsById(id);
    }

    public User save(User user) {
        return userJpaRepository.save(user);
    }

    public void delete(Long id) {
        userJpaRepository.deleteById(id);
    }

    //우선 jpa n+1문제 해결을 위해 수정
    //객체가 연관된 엔티티를 불러올때
    public Optional<User> findByEmail(String email) {
        //return userJpaRepository.findByEmail(email);
        return userJpaRepository.findWithAssociationsByEmail(email);
    }

    public List<EventParticipant> findParticipationsByUserId(Long userId) {
        return userJpaRepository.findParticipationsByUserId(userId);
    }

    public void deleteAll() {
        userJpaRepository.deleteAll();
    }

    public boolean existsByName(String name) {
        return userJpaRepository.existsByName(name);
    }

    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    public boolean existsByUserId(Long userId) {
        return userJpaRepository.existsByUserId(userId);
    }

}
