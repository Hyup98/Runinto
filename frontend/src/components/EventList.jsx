// src/components/EventList.jsx
import React from 'react';
import styled from 'styled-components';
import EventCard from './EventCard';

const ListContainer = styled.div`
    padding: 24px;
`;

const EmptyState = styled.div`
    text-align: center;
    color: #717171;
    padding-top: 50px;
`;

// props에 onCardClick 추가
function EventList({ events, onCardClick }) {
    if (!events || events.length === 0) {
        return (
            <ListContainer>
                <EmptyState>주변에 진행중인 모임이 없습니다.</EmptyState>
            </ListContainer>
        );
    }

    return (
        <ListContainer>
            {/* onCardClick prop을 EventCard에 전달 */}
            {events.map(event => (
                <EventCard
                    key={event.eventId}
                    event={event}
                    onCardClick={onCardClick}
                />
            ))}
        </ListContainer>
    );
}

export default EventList;