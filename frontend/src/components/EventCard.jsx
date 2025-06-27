import React from 'react';
import styled from 'styled-components';

const CardContainer = styled.div`
    background: #fff;
    border: 1px solid #e9e9e9;
    border-radius: 12px;
    padding: 16px;
    margin-bottom: 16px;
    cursor: pointer;
    transition: transform 0.2s ease, box-shadow 0.2s ease;

    &:hover {
        transform: translateY(-4px);
        box-shadow: 0 6px 12px rgba(0,0,0,0.08);
    }
`;

const HostInfo = styled.div`
    display: flex;
    align-items: center;
    margin-bottom: 12px;
`;

const ProfileImage = styled.img`
    width: 36px;
    height: 36px;
    border-radius: 50%;
    object-fit: cover;
    margin-right: 12px;
    background-color: #f0f0f0;
`;

const HostNickname = styled.span`
    font-size: 14px;
    font-weight: 600;
    color: #222;
`;

const CardTitle = styled.h3`
    font-size: 18px;
    font-weight: 600;
    margin: 0 0 8px 0;
    color: #222;
`;

const CardInfo = styled.p`
    font-size: 14px;
    color: #717171;
    margin: 4px 0;
`;

const CategoryTags = styled.div`
    margin-top: 12px;
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
`;

const Tag = styled.span`
    background-color: #f0f0f0;
    color: #555;
    padding: 4px 10px;
    border-radius: 12px;
    font-size: 12px;
`;

function EventCard({ event, onCardClick }) {
    // 💡 백엔드에서 받은 host 객체를 안전하게 사용합니다.
    const host = event.host;

    return (
        <CardContainer onClick={() => onCardClick(event)}>
            <HostInfo>
                {/* host 객체의 imgUrl을 사용하고, 없을 경우 기본 이미지 표시 */}
                <ProfileImage src={host?.imgUrl || '/img/default.png'} alt={`${host?.name || '주최자'} 프로필`} />
                <HostNickname>{host?.name || '주최자'}</HostNickname>
            </HostInfo>
            <CardTitle>{event.title}</CardTitle>
            <CardInfo>👥 {event.participants} / {event.maxParticipants} 명 참여중</CardInfo>
            <CategoryTags>
                {event.eventCategories?.map(catInfo => (
                    <Tag key={catInfo.category}>{catInfo.category}</Tag>
                ))}
            </CategoryTags>
        </CardContainer>
    );
}


export default EventCard;