package com.runinto.event.domain.repository;

import com.runinto.event.domain.Event;
import com.runinto.event.domain.EventType;
import com.runinto.event.dto.request.FindEventRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository
@Primary
public class EventH2Repository implements EventRepositoryImple {

    private final EventJpaRepository eventJpaRepository;

    public EventH2Repository(EventJpaRepository eventJpaRepository) {
        this.eventJpaRepository = eventJpaRepository;
    }

    public Optional<Event> findById(long id) {
        return eventJpaRepository.findById(id);
    }

    public Event save(Event event) {
        return eventJpaRepository.save(event);
    }

    public List<Event> findAll() {
        return eventJpaRepository.findAll();
    }

    public List<Event> findByCategory(Set<EventType> categories) {
        return eventJpaRepository.findByCategories(categories);
    }

    public List<Event> findByArea(double nelatitude, double nelongitude, double swlatitude, double swlongitude) {
        return eventJpaRepository.findByArea(nelatitude, nelongitude, swlatitude, swlongitude);
    }

    public List<Event> findByDynamicCondition(FindEventRequest request) {
        // Specification.where(null)은 항상 참인 조건으로 시작하여, 다른 조건들을 and로 연결합니다.
        Specification<Event> spec = Specification.where(null);

        // 위치 정보 필터링 조건 추가
        if (request.getSwlatitude() != null && request.getNelatitude() != null &&
                request.getSwlongitude() != null && request.getNelongitude() != null) {
            spec = spec.and(EventSpecifications.isInArea(
                    request.getSwlatitude(),
                    request.getNelatitude(),
                    request.getSwlongitude(),
                    request.getNelongitude()
            ));
        }

        // 카테고리 필터링 조건 추가
        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            spec = spec.and(EventSpecifications.hasCategoryIn(request.getCategories()));
        }

        // 'isPublic' 필터링 (FindEventRequest에 isPublic 필드가 있고, 이를 필터링 조건에 넣고 싶다면)
        // if (request.getIsPublic() != null) {
        //     spec = spec.and(EventSpecifications.isPublic(request.getIsPublic()));
        // }


        return eventJpaRepository.findAll(spec); // JpaSpecificationExecutor의 findAll 메서드 사용
    }

    public int getSize(){
        return (int) eventJpaRepository.count();
    }

    public boolean delete(Event event) {
        if (eventJpaRepository.existsById(event.getId())) {
            eventJpaRepository.deleteById(event.getId());
            return true;
        }
        return false;
    }

    public void clear() {
        eventJpaRepository.deleteAll();
    }
}
