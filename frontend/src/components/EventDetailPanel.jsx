// src/components/EventDetailPanel.jsx
import React, { useState, useEffect, useCallback } from 'react';
import styled from 'styled-components';

// JSDoc 타입 정의
/**
 * @typedef {object} UserProfile
 * @property {number} userId - 실제 사용자의 고유 ID
 * @property {number} [participantId] - 이벤트 참여 기록의 고유 ID (강퇴 시 사용)
 * @property {string} name
 * @property {string} [imgUrl]
 * @property {string} [description]
 */

// --- Styled-components 정의 ---
const PanelContainer = styled.div`padding: 24px 32px; background-color: #fff; height: 100%; overflow-y: auto;`;
const HeaderContainer = styled.div`display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 24px;`;
const Title = styled.h2`font-size: 28px; font-weight: 700; color: #222; margin: 0;`;
const EditButton = styled.button`background-color: #f7f7f7; border: 1px solid #ddd; color: #333; padding: 8px 16px; border-radius: 8px; font-size: 14px; font-weight: 600; cursor: pointer; transition: all 0.2s ease; &:hover { background-color: #e9e9e9; }`;
const TabContainer = styled.div`display: inline-flex; background-color: #f7f7f7; border-radius: 10px; padding: 4px; margin-bottom: 24px;`;
const TabButton = styled.button`background: transparent; border: none; padding: 10px 20px; cursor: pointer; font-size: 15px; font-weight: 500; color: #555; outline: none; border-radius: 8px; transition: all 0.3s ease; &.is-active { background-color: #fff; color: #ff385c; font-weight: 600; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1); }`;
const ContentContainer = styled.div`padding-top: 16px;`;
const Section = styled.section`margin-bottom: 32px;`;
const SectionTitle = styled.h3`font-size: 20px; font-weight: 600; color: #333; margin: 0 0 20px 0;`;
const ParticipantList = styled.ul`list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column;`;
const ParticipantItem = styled.li`display: flex; justify-content: space-between; align-items: center; padding: 12px 0; border-bottom: 1px solid #f1f1f1; &:last-child { border-bottom: none; }`;
const UserInfo = styled.div`display: flex; align-items: center; gap: 12px; overflow: hidden;`;
const ProfileImg = styled.img`width: 48px; height: 48px; border-radius: 50%; object-fit: cover; background-color: #eee; flex-shrink: 0;`;
const InfoWrapper = styled.div`display: flex; flex-direction: column; overflow: hidden;`;
const ParticipantName = styled.span`font-weight: 600; font-size: 16px; color: #212529;`;
const ParticipantDescription = styled.span`font-size: 14px; color: #868e96; margin-top: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;`;
const ActionButtons = styled.div`display: flex; gap: 8px; flex-shrink: 0;`;
const ActionButton = styled.button`padding: 6px 12px; border-radius: 6px; border: 1px solid #ddd; background-color: #fff; cursor: pointer; font-size: 13px; font-weight: 500; transition: all 0.2s ease; &.approve { background-color: #ff385c; color: white; border-color: #ff385c; &:hover { opacity: 0.9; } } &.reject { &:hover { background-color: #f1f1f1; } } &.kick { color: #e61e4d; border-color: #fce8ec; &:hover { background-color: #fce8ec; } }`;


// --- 각 탭의 실제 콘텐츠 컴포넌트 ---

/**
 * 정보 탭: 주최자, 설명, 카테고리, 승인된 참여자 목록 표시
 * @param {{event: object}} props
 */
const InfoTab = ({ event }) => {
    /** @type {[UserProfile[], Function]} */
    const [participants, setParticipants] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        if (!event?.eventId) return;
        const fetchApprovedParticipants = async () => {
            setIsLoading(true);
            try {
                const res = await fetch(`/events/${event.eventId}/participants/APPROVED`);
                const data = res.ok ? await res.json() : [];
                setParticipants(Array.isArray(data) ? data : []);
            } catch (err) {
                console.error("승인된 참여자 목록 조회 실패:", err);
                setParticipants([]);
            } finally { setIsLoading(false); }
        };
        fetchApprovedParticipants();
    }, [event.eventId]);

    return (
        <div>
            <Section>
                <SectionTitle>주최자</SectionTitle>
                <UserInfo>
                    <ProfileImg src={event.host?.imgUrl || '/img/default.png'} alt={event.host?.name} />
                    <InfoWrapper>
                        <ParticipantName>{event.host?.name || '정보 없음'}</ParticipantName>
                    </InfoWrapper>
                </UserInfo>
            </Section>
            <Section>
                <SectionTitle>모임 설명</SectionTitle>
                <p>{event.description || '설명이 없습니다.'}</p>
            </Section>
            <Section>
                <SectionTitle>참여 멤버 ({participants.length} / {event.maxParticipants})</SectionTitle>
                {isLoading ? <p>멤버 목록을 불러오는 중...</p> :
                    <ParticipantList>
                        {participants.length > 0 ? (
                            participants.map(p => (
                                <ParticipantItem key={p.userId}>
                                    <UserInfo>
                                        <ProfileImg src={p.imgUrl || '/img/default.png'} alt={p.name} />
                                        <InfoWrapper>
                                            <ParticipantName>{p.name} {p.userId === event.host?.userId && '(주최자)'}</ParticipantName>
                                        </InfoWrapper>
                                    </UserInfo>
                                </ParticipantItem>
                            ))
                        ) : <p>아직 참여한 멤버가 없습니다.</p>}
                    </ParticipantList>
                }
            </Section>
        </div>
    );
};

/**
 * 멤버 관리 탭: 신청자 목록(수락/거절)과 참여자 목록(강퇴) 표시
 * @param {{event: object}} props
 */
const ManagementTab = ({ event }) => {
    const [applicants, setApplicants] = useState([]);
    const [participants, setParticipants] = useState([]);
    const [isLoading, setIsLoading] = useState(true);

    const fetchData = useCallback(async () => {
        setIsLoading(true);
        try {
            const [reqRes, aprRes] = await Promise.all([
                fetch(`/events/${event.eventId}/participants/REQUESTED`),
                fetch(`/events/${event.eventId}/participants/APPROVED`)
            ]);
            const reqData = reqRes.ok ? await reqRes.json() : [];
            const aprData = aprRes.ok ? await aprRes.json() : [];
            setApplicants(Array.isArray(reqData) ? reqData : []);
            setParticipants(Array.isArray(aprData) ? aprData : []);
        } catch (err) {
            console.error("멤버 관리 목록 조회 실패:", err);
        } finally { setIsLoading(false); }
    }, [event.eventId]);

    useEffect(() => { fetchData(); }, [fetchData]);

    const handleAction = async (action, targetId) => {
        let url = '';
        let method = 'POST';
        switch (action) {
            case 'approve': url = `/events/${event.eventId}/participants/${targetId}/approve`; break;
            case 'reject': url = `/events/${event.eventId}/participants/${targetId}/reject`; break;
            case 'kick':
                if (!window.confirm("정말로 이 멤버를 강퇴하시겠습니까?")) return;
                url = `/events/${event.eventId}/participants/${targetId}`;
                method = 'DELETE';
                break;
            default: return;
        }
        try {
            const response = await fetch(url, { method });
            if (response.ok) {
                alert('요청이 처리되었습니다.');
                fetchData();
            } else { alert(`처리 실패: ${await response.text()}`); }
        } catch (err) { console.error(`${action} 처리 중 오류:`, err); }
    };

    if (isLoading) return <p>멤버 목록을 불러오는 중...</p>;

    return (
        <>
            <Section>
                <SectionTitle>참여 신청 목록 ({applicants.length})</SectionTitle>
                <ParticipantList>
                    {applicants.length > 0 ? applicants.map(p => (
                        <ParticipantItem key={p.userId}>
                            <UserInfo>
                                <ProfileImg src={p.imgUrl || '/img/default.png'} alt={p.name} />
                                <ParticipantName>{p.name}</ParticipantName>
                            </UserInfo>
                            <ActionButtons>
                                <ActionButton className="approve" onClick={() => handleAction('approve', p.userId)}>승인</ActionButton>
                                <ActionButton className="reject" onClick={() => handleAction('reject', p.userId)}>거절</ActionButton>
                            </ActionButtons>
                        </ParticipantItem>
                    )) : <p>참여 신청자가 없습니다.</p>}
                </ParticipantList>
            </Section>
            <Section>
                <SectionTitle>참여한 인원 ({participants.length})</SectionTitle>
                <ParticipantList>
                    {participants.length > 0 ? participants.map(p => (
                        <ParticipantItem key={p.userId}>
                            <UserInfo>
                                <ProfileImg src={p.imgUrl || '/img/default.png'} alt={p.name} />
                                <ParticipantName>{p.name} {p.userId === event.host?.userId && '(주최자)'}</ParticipantName>
                            </UserInfo>
                            {p.userId !== event.host?.userId && (
                                <ActionButtons>
                                    <ActionButton className="kick" onClick={() => handleAction('kick', p.userId)}>강퇴</ActionButton>
                                </ActionButtons>
                            )}
                        </ParticipantItem>
                    )) : <p>참여한 인원이 없습니다.</p>}
                </ParticipantList>
            </Section>
        </>
    );
};

const ChatTab = () => <div><h3>채팅</h3><p>기능 준비 중입니다.</p></div>;
const PhotosTab = () => <div><h3>사진첩</h3><p>기능 준비 중입니다.</p></div>;

function EventDetailPanel({ event, mode, currentUserId, onEditClick, participationStatus }) {
    const [activeTab, setActiveTab] = useState('info');
    const isHost = event.host?.userId === currentUserId;
    useEffect(() => { setActiveTab('info'); }, [event]);
    const canShowExtraTabs = mode === 'created' || participationStatus === 'APPROVED';

    return (
        <PanelContainer>
            <HeaderContainer>
                <Title>{event.title}</Title>
                {isHost && <EditButton onClick={() => onEditClick(event)}>수정</EditButton>}
            </HeaderContainer>
            <TabContainer>
                <TabButton className={activeTab === 'info' ? 'is-active' : ''} onClick={() => setActiveTab('info')}>정보</TabButton>
                {canShowExtraTabs && (
                    <>
                        <TabButton className={activeTab === 'chat' ? 'is-active' : ''} disabled>채팅</TabButton>
                        <TabButton className={activeTab === 'photos' ? 'is-active' : ''} disabled>사진첩</TabButton>
                    </>
                )}
                {mode === 'created' && isHost && (
                    <TabButton className={activeTab === 'management' ? 'is-active' : ''} onClick={() => setActiveTab('management')}>멤버 관리</TabButton>
                )}
            </TabContainer>
            <ContentContainer>
                {activeTab === 'info' && <InfoTab event={event} />}
                {activeTab === 'chat' && canShowExtraTabs && <ChatTab />}
                {activeTab === 'photos' && canShowExtraTabs && <PhotosTab />}
                {activeTab === 'management' && isHost && <ManagementTab event={event} />}
            </ContentContainer>
        </PanelContainer>
    );
}
export default EventDetailPanel;