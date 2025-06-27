// src/components/EventDetailModal.jsx
import React from 'react';
import styled from 'styled-components';

// CreateEventModal과 동일한 스타일 재사용
const ModalOverlay = styled.div` /* ... */ `;
const ModalContent = styled.div` /* ... */ `;
const CloseButton = styled.button` /* ... */ `;
const Title = styled.h2` /* ... */ `;
const SubmitButton = styled.button` /* ... */ `;


function EventDetailModal({ event, onClose, onJoin }) {
    if (!event) return null;

    const handleJoinClick = () => {
        if(window.confirm("이 모임에 참가를 신청하시겠습니까?")) {
            onJoin(event.eventId);
        }
    };

    return (
        <ModalOverlay onClick={onClose}>
            <ModalContent onClick={(e) => e.stopPropagation()}>
                <CloseButton onClick={onClose}>×</CloseButton>
                <Title>{event.title}</Title>

                {/* 상세 정보 표시 영역 */}
                <div>
                    <p><strong>주최자:</strong> {event.host?.name || '정보 없음'}</p>
                    <p><strong>설명:</strong> {event.description}</p>
                    <p><strong>참여 현황:</strong> {event.participants} / {event.maxParticipants} 명</p>
                    <div>
                        <strong>카테고리:</strong>
                        {event.eventCategories?.map(cat => <span key={cat.category}> #{cat.category}</span>)}
                    </div>
                </div>

                <SubmitButton onClick={handleJoinClick}>
                    참가 신청하기
                </SubmitButton>
            </ModalContent>
        </ModalOverlay>
    );
}

export default EventDetailModal;