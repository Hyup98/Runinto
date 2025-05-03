package com.runinto.event.domain.repository;

import com.runinto.event.domain.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventJpaRepository extends JpaRepository<Event, Long> {
    
    @Query("SELECT e FROM Event e JOIN e.eventCategories ec WHERE ec.category IN :categories")
    List<Event> findByCategories(@Param("categories") java.util.Set<com.runinto.event.domain.EventType> categories);
    
    @Query("SELECT e FROM Event e WHERE e.latitude >= :swlat AND e.latitude <= :nelat AND e.longitude >= :swlng AND e.longitude <= :nelng")
    List<Event> findByArea(
            @Param("nelat") double nelat,
            @Param("nelng") double nelng,
            @Param("swlat") double swlat,
            @Param("swlng") double swlng);
}