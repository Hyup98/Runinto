package com.runinto.user.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {
    @Query("""
        SELECT ep.event 
        FROM EventParticipant ep 
        WHERE ep.user.userId = :userId
    """)
    List<Event> findJoinedEvents(@Param("userId") Long userId);
}