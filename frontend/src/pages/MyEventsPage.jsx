// src/pages/MyEventsPage.jsx
import React, { useState, useEffect, useMemo, useCallback } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import useAuth from '../hooks/useAuth';
import Header from '../components/Header';
import EventDetailPanel from '../components/EventDetailPanel';
import CreateEventModal from '../components/CreateEventModal';

const Container = styled.div` display: flex; flex-direction: column; height: 100vh; `;
const Body = styled.div` display: flex; flex: 1; overflow: hidden; background-color: #f7f7f7; `;
const Sidebar = styled.aside`
    width: 360px; flex-shrink: 0; background-color: #fff;
    border-right: 1px solid #e0e0e0; padding: 24px; overflow-y: auto;
`;
const EventItem = styled.div`
    padding: 16px; border-radius: 12px; margin-bottom: 12px; cursor: pointer;
    border: 1px solid ${props => props.$isSelected ? '#ff385c' : '#ddd'};
    background-color: ${props => props.$isSelected ? '#fff5f7' : '#fff'};
    transition: all 0.2s;
    h4 { margin: 0 0 8px 0; }
    p { margin: 0; font-size: 14px; color: #717171; }
    &:hover { border-color: #ff385c; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
`;
const SidebarSectionTitle = styled.h3`
    font-size: 18px; font-weight: 600; margin: 24px 0 12px 0;
    padding-bottom: 8px; border-bottom: 1px solid #eee;
    &:first-of-type { margin-top: 0; }
`;
const Content = styled.main` flex: 1; overflow-y: auto; `;
const LoadingOrEmpty = styled.div` display: flex; justify-content: center; align-items: center; height: 100%; color: #717171; font-size: 16px; padding: 32px; `;

function MyEventsPage() {
    const { user, isLoading: isAuthLoading } = useAuth();
    const location = useLocation();
    const navigate = useNavigate();
    const [eventList, setEventList] = useState([]);
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [isDataLoading, setIsDataLoading] = useState(true);
    const [error, setError] = useState(null);
    const [isModalOpen, setModalOpen] = useState(false);
    const [eventToEdit, setEventToEdit] = useState(null);

    const pageMode = useMemo(() => {
        return location.pathname.includes('/created-events') ? 'created' : 'joined';
    }, [location.pathname]);

    // 💡 [BUG FIX] useMemo 내부의 변수명을 올바르게 수정하고 반환 값을 정확히 지정합니다.
    const { approvedEvents, pendingEvents } = useMemo(() => {
        if (pageMode !== 'joined' || !eventList) {
            return { approvedEvents: [], pendingEvents: [] };
        }

        // 백엔드에서 받은 myParticipationStatus 값으로 목록 분리
        const approved = eventList.filter(event => event.myParticipationStatus === 'APPROVED');
        const pending = eventList.filter(event => event.myParticipationStatus === 'REQUESTED');

        // 올바른 변수를 키와 값으로 반환
        return { approvedEvents: approved, pendingEvents: pending };
    }, [eventList, pageMode]);

    const fetchEvents = useCallback(async () => {
        if (!user?.id) return;
        // 💡 '참가한 모임' API 경로를 명확히 함
        const apiUrl = pageMode === 'created' ? `/users/${user.id}/created-events` : `/users/${user.id}/joined-events`;
        setIsDataLoading(true);
        try {
            const response = await fetch(apiUrl);
            if (!response.ok) throw new Error("데이터를 불러오는 데 실패했습니다.");
            const data = await response.json();
            const events = Array.isArray(data) ? data : (data.events || []);
            setEventList(events);

            if (events.length > 0) {
                // 현재 선택된 이벤트가 목록에 여전히 있는지 확인, 없으면 첫 번째 항목을 선택
                const currentSelected = events.find(e => e.eventId === selectedEvent?.eventId);
                setSelectedEvent(currentSelected || events[0]);
            } else {
                setSelectedEvent(null);
            }
        } catch (err) {
            setError(err.message);
        } finally { setIsDataLoading(false); }
    }, [user?.id, pageMode, selectedEvent?.eventId]);

    useEffect(() => {
        if (user?.id) {
            fetchEvents();
        }
    }, [user, pageMode, fetchEvents]); // 💡 fetchEvents를 의존성 배열에 추가

    const handleOpenEditModal = (event) => { setEventToEdit(event); setModalOpen(true); };
    const handleCloseModal = () => { setModalOpen(false); setEventToEdit(null); };
    const handleSuccess = () => { handleCloseModal(); fetchEvents(); };
    const handleLogout = async () => { await fetch('/auth/logout', { method: 'POST' }); navigate('/auth'); };

    if (isAuthLoading) return <div>로딩 중...</div>;

    const renderEventItem = (event) => (
        <EventItem
            key={event.eventId}
            $isSelected={selectedEvent?.eventId === event.eventId}
            onClick={() => setSelectedEvent(event)}
        >
            <h4>{event.title}</h4>
            <p>참여 인원: {event.participants} / {event.maxParticipants}</p>
        </EventItem>
    );

    return (
        <>
            <Container>
                <Header user={user} onLogout={handleLogout}/>
                <Body>
                    <Sidebar>
                        <h2>{pageMode === 'created' ? '내가 만든 모임' : '참가한 모임'}</h2>
                        {isDataLoading ? <LoadingOrEmpty>목록 로딩 중...</LoadingOrEmpty>
                            : error ? <LoadingOrEmpty>{error}</LoadingOrEmpty>
                                : pageMode === 'created' ? (
                                    eventList.length > 0 ? eventList.map(renderEventItem) : <LoadingOrEmpty>만든 모임이 없습니다.</LoadingOrEmpty>
                                ) : (
                                    <>
                                        <SidebarSectionTitle>참가한 모임 ({approvedEvents.length})</SidebarSectionTitle>
                                        {approvedEvents.length > 0 ? approvedEvents.map(renderEventItem) : <p>참가한 모임이 없습니다.</p>}

                                        <SidebarSectionTitle>참가 신청 대기 중 ({pendingEvents.length})</SidebarSectionTitle>
                                        {pendingEvents.length > 0 ? pendingEvents.map(renderEventItem) : <p>신청 대기 중인 모임이 없습니다.</p>}
                                    </>
                                )
                        }
                    </Sidebar>
                    <Content>
                        {selectedEvent ? (
                            <EventDetailPanel
                                key={selectedEvent.eventId}
                                event={selectedEvent}
                                mode={pageMode}
                                currentUserId={user.id}
                                participationStatus={selectedEvent.myParticipationStatus}
                                onEditClick={handleOpenEditModal}
                            />
                        ) : <LoadingOrEmpty>{eventList.length > 0 ? '표시할 이벤트를 선택해주세요.' : '참여하거나 만든 모임이 없습니다.'}</LoadingOrEmpty> }
                    </Content>
                </Body>
            </Container>
            {isModalOpen && <CreateEventModal onClose={handleCloseModal} onSuccess={handleSuccess} userId={user.id} eventToEdit={eventToEdit} />}
        </>
    );
}
export default MyEventsPage;