// src/components/NaverMapView.jsx
import React, { useEffect, useRef } from 'react';

function NaverMapView({ initialCenter, onMapIdle, events }) {
    const mapRef = useRef(null);
    const mapInstanceRef = useRef(null);
    const eventMarkersRef = useRef([]); // 이벤트 마커들을 관리할 배열
    const activeInfoWindowRef = useRef(null); // 활성화된 정보창을 관리

    // 1. 지도 인스턴스 최초 생성 (단 한 번만 실행)
    useEffect(() => {
        if (mapInstanceRef.current || !initialCenter || !window.naver) return;
        const mapOptions = {
            center: new window.naver.maps.LatLng(initialCenter.lat, initialCenter.lng),
            zoom: 16,
            minZoom: 6,
            zoomControl: false,
            mapTypeControl: false,
        };
        mapInstanceRef.current = new window.naver.maps.Map(mapRef.current, mapOptions);
    }, [initialCenter]);

    // 2. 지도 이동 시 'idle' 이벤트 리스너 관리
    useEffect(() => {
        const map = mapInstanceRef.current;
        if (!map) return;
        const listener = window.naver.maps.Event.addListener(map, 'idle', () => {
            if (map.getBounds() && onMapIdle) {
                const bounds = map.getBounds();
                onMapIdle({ swLat: bounds.getSW().lat(), swLng: bounds.getSW().lng(), neLat: bounds.getNE().lat(), neLng: bounds.getNE().lng() });
            }
        });
        return () => window.naver.maps.Event.removeListener(listener);
    }, [onMapIdle]);

    // 3. 이벤트 마커 관리 (events 배열이 바뀔 때마다 실행되어 마커를 동기화)
    useEffect(() => {
        const map = mapInstanceRef.current;
        if (!map || !window.naver) return;

        // 3-1. 기존에 있던 이벤트 마커들을 먼저 모두 삭제
        eventMarkersRef.current.forEach(marker => marker.setMap(null));
        eventMarkersRef.current = [];

        // 3-2. props로 받은 events 배열을 순회하며 새로운 마커 생성
        if (events && events.length > 0) {
            const newMarkers = events.map(event => {
                const position = new window.naver.maps.LatLng(event.latitude, event.longitude);
                const marker = new window.naver.maps.Marker({ position, map });

                const infoWindow = new window.naver.maps.InfoWindow({
                    content: `<div style="padding:10px;font-size:14px;max-width:200px;"><b>${event.title}</b></div>`
                });

                window.naver.maps.Event.addListener(marker, "click", () => {
                    if (activeInfoWindowRef.current) activeInfoWindowRef.current.close();
                    infoWindow.open(map, marker);
                    activeInfoWindowRef.current = infoWindow;
                });

                return marker;
            });
            eventMarkersRef.current = newMarkers;
        }
    }, [events]); // events 배열이 변경될 때마다 이 로직이 다시 실행됩니다.

    return <div ref={mapRef} style={{ width: '100%', height: '100%' }} />;
}

export default NaverMapView;