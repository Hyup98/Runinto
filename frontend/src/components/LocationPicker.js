// src/components/LocationPicker.js
import React, { useState, useEffect, useRef } from 'react';
import styled from 'styled-components';

const Container = styled.div`
    position: relative;
    width: 100%;
    height: 300px;
    margin-bottom: 20px;
`;

const MapContainer = styled.div`
    width: 100%;
    height: 100%;
    border-radius: 12px;
`;

const SearchContainer = styled.div`
    position: absolute;
    top: 10px;
    left: 10px;
    z-index: 10;
    display: flex;
    background-color: white;
    padding: 5px;
    border-radius: 8px;
    box-shadow: 0 2px 6px rgba(0,0,0,0.15);
`;

const SearchInput = styled.input`
    border: none;
    padding: 8px;
    font-size: 14px;
    &:focus {
        outline: none;
    }
`;

const SearchButton = styled.button`
    border: none;
    background-color: #ff385c;
    color: white;
    padding: 0 12px;
    border-radius: 6px;
    cursor: pointer;
`;

function LocationPicker({ onLocationChange, initialCenter }) {
    const mapRef = useRef(null);
    const [searchQuery, setSearchQuery] = useState('');

    // 💡 BUG FIX: useEffect의 의존성 배열을 '[]'로 변경하여
    // 컴포넌트가 처음 마운트될 때 단 한 번만 실행되도록 수정합니다.
    useEffect(() => {
        if (!mapRef.current || !window.naver) {
            return;
        }

        const startCenter = initialCenter && initialCenter.lat ?
            new window.naver.maps.LatLng(initialCenter.lat, initialCenter.lng) :
            new window.naver.maps.LatLng(37.5665, 126.9780); // 기본 위치

        const mapOptions = {
            center: startCenter,
            zoom: 16,
        };
        const map = new window.naver.maps.Map(mapRef.current, mapOptions);

        const marker = new window.naver.maps.Marker({
            position: startCenter,
            map: map,
            draggable: true,
            icon: {
                content: `<div style="font-size:24px;">🚩</div>`,
                anchor: new window.naver.maps.Point(12, 24),
            }
        });

        const listener = window.naver.maps.Event.addListener(marker, 'dragend', () => {
            const newPos = marker.getPosition();
            if (onLocationChange) {
                onLocationChange({ lat: newPos.lat(), lng: newPos.lng() });
            }
        });

        // 컴포넌트가 언마운트될 때 리스너를 제거하여 메모리 누수 방지
        return () => {
            window.naver.maps.Event.removeListener(listener);
        };
        // 💡 의존성 배열을 비워 최초 1회만 실행되도록 수정
    }, []);

    const handleSearch = () => {
        alert(`'${searchQuery}' 검색 기능은 API 연동이 필요합니다.`);
    };

    return (
        <Container>
            <SearchContainer>
                <SearchInput
                    type="text"
                    placeholder="장소, 주소 검색..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
                />
                <SearchButton onClick={handleSearch}>검색</SearchButton>
            </SearchContainer>
            <MapContainer ref={mapRef} />
        </Container>
    );
}

export default LocationPicker;