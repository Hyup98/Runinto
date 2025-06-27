// src/components/CreateEventModal.js

import React, { useState, useEffect, useCallback } from 'react';
import styled, { keyframes } from 'styled-components';
import LocationPicker from './LocationPicker';

// --- Styled Components (기존과 동일) ---
const fadeIn = keyframes`
    from { opacity: 0; }
    to { opacity: 1; }
`;

const ModalOverlay = styled.div`
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.6);
    backdrop-filter: blur(5px);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 2000;
    animation: ${fadeIn} 0.3s ease-out;
`;

const ModalContent = styled.div`
    background-color: white;
    padding: 24px 32px;
    border-radius: 16px;
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.2);
    width: 90%;
    max-width: 550px;
    position: relative;
    max-height: 90vh;
    overflow-y: auto;
`;

const CloseButton = styled.button`
    position: absolute;
    top: 16px;
    right: 16px;
    background: #f1f1f1;
    border: none;
    border-radius: 50%;
    width: 32px;
    height: 32px;
    font-size: 20px;
    line-height: 32px;
    text-align: center;
    color: #555;
    cursor: pointer;
    transition: all 0.2s;
    &:hover {
        background-color: #e0e0e0;
        transform: rotate(90deg);
    }
`;

const Form = styled.form`
    display: flex;
    flex-direction: column;
    gap: 16px;
`;

const Title = styled.h2`
    font-size: 24px;
    font-weight: 700;
    color: #222;
    text-align: center;
    margin: 0 0 16px 0;
`;

const Input = styled.input`
    width: 100%;
    padding: 12px;
    border: 1px solid #ccc;
    border-radius: 8px;
    font-size: 16px;
    transition: all 0.2s;
    &:focus {
        outline: none;
        border-color: #FF385C;
        box-shadow: 0 0 0 2px rgba(255, 56, 92, 0.2);
    }
`;

const Textarea = styled(Input).attrs({ as: 'textarea' })`
    resize: vertical;
    min-height: 90px;
`;

const Label = styled.label`
    font-size: 14px;
    font-weight: 600;
    margin-bottom: -8px;
    color: #333;
`;

const CheckboxGroup = styled.div`
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
`;

const CheckboxLabel = styled.label`
    display: flex;
    align-items: center;
    cursor: pointer;
    input {
        margin-right: 6px;
    }
`;

const ErrorMessage = styled.p`
    color: #d91b42;
    font-size: 14px;
    text-align: center;
    margin: 8px 0 0 0;
    height: 16px;
`;

const ButtonContainer = styled.div`
    display: flex;
    gap: 12px;
    margin-top: 16px;
    justify-content: flex-end;
`;

const SubmitButton = styled.button`
    background: linear-gradient(90deg, #FF385C 0%, #E61E4D 100%);
    color: white;
    padding: 14px 20px;
    border: none;
    border-radius: 10px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    &:hover { opacity: 0.9; }
`;

const DeleteButton = styled.button`
    background: #6c757d;
    color: white;
    padding: 14px 20px;
    border: none;
    border-radius: 10px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    margin-right: auto;
    &:hover { background: #5a6268; }
`;


function CreateEventModal({ onClose, onSuccess, userId, eventToEdit, initialCenter }) {
    const [isEditMode, setIsEditMode] = useState(false);
    const [eventData, setEventData] = useState({
        title: '',
        description: '',
        maxParticipants: 2,
        latitude: initialCenter?.lat || 37.5665,
        longitude: initialCenter?.lng || 126.9780,
        isPublic: true,
        categories: new Set(),
        creationTime: new Date().toTimeString().split(' ')[0],
    });
    const [error, setError] = useState('');

    useEffect(() => {
        if (eventToEdit) {
            setIsEditMode(true);
            setEventData({
                ...eventToEdit,
                latitude: eventToEdit.latitude || initialCenter.lat,
                longitude: eventToEdit.longitude || initialCenter.lng,
                categories: new Set(eventToEdit.eventCategories?.map(c => c.category) || []),
            });
        }
    }, [eventToEdit, initialCenter]);

    // 💡 BUG FIX: 함수형 업데이트를 사용하여 stale state 문제를 해결합니다.
    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setEventData(prevData => {
            if (name === 'categories') {
                const newCategories = new Set(prevData.categories);
                checked ? newCategories.add(value) : newCategories.delete(value);
                return { ...prevData, categories: newCategories };
            } else {
                return { ...prevData, [name]: type === 'checkbox' ? checked : value };
            }
        });
    };

    const handleLocationChange = useCallback((location) => {
        setEventData(prev => ({ ...prev, latitude: location.lat, longitude: location.lng }));
    }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        const url = isEditMode ? `/events/${eventToEdit.eventId}` : `/events?user=${userId}`;
        const method = isEditMode ? 'PATCH' : 'POST';
        const requestDto = {
            title: eventData.title, description: eventData.description,
            maxParticipants: parseInt(eventData.maxParticipants, 10),
            latitude: eventData.latitude, longitude: eventData.longitude,
            isPublic: eventData.isPublic, categories: Array.from(eventData.categories),
            ...(isEditMode ? {} : { creationTime: eventData.creationTime })
        };
        if (requestDto.categories.length === 0) { setError("카테고리를 최소 하나 이상 선택해야 합니다."); return; }
        try {
            const response = await fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(requestDto) });
            if (response.ok) {
                alert(isEditMode ? "이벤트가 수정되었습니다." : "이벤트가 생성되었습니다.");
                onSuccess();
            } else { setError(await response.text() || "요청 실패"); }
        } catch (err) { setError("네트워크 오류가 발생했습니다."); }
    };

    const handleDelete = async () => {
        if (!isEditMode || !window.confirm("정말로 이 이벤트를 삭제하시겠습니까?")) return;
        try {
            const response = await fetch(`/events/${eventToEdit.eventId}`, { method: 'DELETE' });
            if (response.ok) {
                alert("삭제되었습니다.");
                onSuccess();
            } else { alert("삭제 실패"); }
        } catch (err) { setError("삭제 처리 중 오류 발생"); }
    };

    const availableCategories = ['EAT', 'ACTIVITY', 'TALKING', 'MOVIE', 'MUSIC', 'GAME'];

    return (
        <ModalOverlay>
            <ModalContent onClick={(e) => e.stopPropagation()}>
                <CloseButton onClick={onClose}>×</CloseButton>
                <Title>{isEditMode ? '모임 정보 수정' : '새로운 모임 생성'}</Title>
                <Form onSubmit={handleSubmit}>
                    <Label htmlFor="title">모임 제목</Label>
                    <Input type="text" id="title" name="title" value={eventData.title} onChange={handleInputChange} required />

                    <Label htmlFor="description">모임 설명</Label>
                    <Textarea id="description" name="description" value={eventData.description} onChange={handleInputChange} required />

                    <Label>위치 선택 (깃발을 드래그하여 지정)</Label>
                    <LocationPicker onLocationChange={handleLocationChange} initialCenter={{ lat: eventData.latitude, lng: eventData.longitude }} />

                    <Label htmlFor="maxParticipants">최대 인원</Label>
                    <Input type="number" id="maxParticipants" name="maxParticipants" min="2" max="50" value={eventData.maxParticipants} onChange={handleInputChange} />

                    <Label>카테고리</Label>
                    <CheckboxGroup>
                        {availableCategories.map(cat => (
                            <CheckboxLabel key={cat}>
                                <input type="checkbox" name="categories" value={cat} checked={eventData.categories.has(cat)} onChange={handleInputChange} />
                                {cat}
                            </CheckboxLabel>
                        ))}
                    </CheckboxGroup>

                    <CheckboxLabel>
                        <input type="checkbox" name="isPublic" checked={eventData.isPublic} onChange={handleInputChange} />
                        모임 전체 공개
                    </CheckboxLabel>

                    <ErrorMessage>{error}</ErrorMessage>
                    <ButtonContainer>
                        {isEditMode && <DeleteButton type="button" onClick={handleDelete}>이벤트 삭제</DeleteButton>}
                        <SubmitButton type="submit">{isEditMode ? '저장하기' : '생성하기'}</SubmitButton>
                    </ButtonContainer>
                </Form>
            </ModalContent>
        </ModalOverlay>
    );
}

export default CreateEventModal;