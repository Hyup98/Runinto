package com.runinto.event.domain.repository;

import com.runinto.event.domain.EventParticipant;
import com.runinto.event.domain.ParticipationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface EventParticipantRepository extends JpaRepository<EventParticipant, Long> {

    @Query("SELECT ep FROM EventParticipant ep JOIN FETCH ep.user WHERE ep.event.id = :eventId AND ep.status = :status")
    List<EventParticipant> findParticipantsByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") ParticipationStatus status);

    @Modifying
    @Transactional // Modifying 쿼리는 트랜잭션이 필요합니다.
    @Query("UPDATE EventParticipant ep SET ep.status = :status WHERE ep.id = :id")
    void updateStatusById(@Param("id") Long id, @Param("status") ParticipationStatus status);
}