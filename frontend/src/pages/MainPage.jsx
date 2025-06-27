import React, { useState, useEffect, useCallback } from 'react';
import styled, { createGlobalStyle } from 'styled-components';
import Header from '../components/Header';
import EventList from '../components/EventList';
import NaverMapView from '../components/NaverMapView';
import useUserLocation from '../hooks/useUserLocation';
import CreateEventModal from '../components/CreateEventModal';
import useAuth from '../hooks/useAuth';
import EventDetailModal from '../components/EventDetailModal';
import CategoryFilter from '../components/CategoryFilter';
import FilterModal from '../components/FilterModal';
import Pagination from '../components/Pagination'; // 💡 1. Pagination 컴포넌트 import

const GlobalStyle = createGlobalStyle`
    body.modal-open {
        overflow: hidden;
    }
`;

const Container = styled.div`
    display: flex;
    flex-direction: column;
    height: 100vh;
    background-color: #fff;
`;

const MainContent = styled.div`
    display: flex;
    flex-direction: column;
    flex: 1;
    overflow: hidden;
`;

const FilterBarContainer = styled.div`
    display: flex;
    align-items: center;
    padding: 0 24px;
    border-bottom: 1px solid #ebebeb;
    background-color: #fff;
    gap: 24px;
`;

const CategoryFilterWrapper = styled.div`
    flex-grow: 1;
    overflow-x: auto;
    -ms-overflow-style: none;
    scrollbar-width: none;
    &::-webkit-scrollbar {
        display: none;
    }
`;

const FilterButton = styled.button`
    display: flex;
    align-items: center;
    gap: 8px;
    background-color: #fff;
    border: 1px solid #ddd;
    border-radius: 8px;
    padding: 12px 16px;
    cursor: pointer;
    font-size: 14px;
    font-weight: 600;
    flex-shrink: 0;
    transition: all 0.2s;
    position: relative;

    &:hover {
        border-color: #000;
        box-shadow: 0 1px 4px rgba(0,0,0,0.05);
    }
`;

const Body = styled.div`
    display: flex;
    flex: 1;
    overflow: hidden;
`;

const EventSection = styled.div`
    width: 30%;
    min-width: 360px;
    border-right: 1px solid #ddd;
    display: flex;
    flex-direction: column;
`;

const EventListWrapper = styled.div`
    flex-grow: 1;
    overflow-y: auto;
`;

const MapSection = styled.div` width: 70%; position: relative; `;
const CreateEventButton = styled.button` position: absolute; top: 24px; left: 24px; z-index: 1000; padding: 10px 18px; background: linear-gradient(90deg, #FF385C 0%, #E61E4D 100%); color: white; font-weight: bold; border: none; border-radius: 8px; font-size: 14px; cursor: pointer; box-shadow: 0 2px 6px rgba(0,0,0,0.1); `;
const RefetchButton = styled.button` position: absolute; bottom: 24px; left: 50%; transform: translateX(-50%); z-index: 1000; padding: 12px 24px; background-color: #ff385c; color: white; font-weight: bold; border: none; border-radius: 24px; font-size: 15px; box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1); cursor: pointer; `;

function MainPage() {
    const { user, isLoading: isAuthLoading } = useAuth();
    const { location, loading: isLocationLoading } = useUserLocation();

    // 💡 2. 상태 관리 수정
    const [pageData, setPageData] = useState(null);
    const [currentPage, setCurrentPage] = useState(0);

    const [initialCenter, setInitialCenter] = useState(null);
    const [currentMapBounds, setCurrentMapBounds] = useState(null);
    const [isCreateModalOpen, setCreateModalOpen] = useState(false);
    const [selectedEvent, setSelectedEvent] = useState(null);
    const [isFilterModalOpen, setFilterModalOpen] = useState(false);

    const [filters, setFilters] = useState({
        categories: new Set(),
        isPublic: null,
        maxParticipants: 50
    });

    const availableCategories = ['EAT', 'ACTIVITY', 'TALKING', 'MOVIE', 'MUSIC', 'GAME'];

    // 💡 3. API 호출 함수 수정
    const fetchEvents = useCallback(async (bounds, currentFilters, page) => {
        if (!bounds) return;

        let url = new URL(window.location.origin + "/events");
        url.searchParams.append('page', page);
        url.searchParams.append('size', 10);
        url.searchParams.append('sort', 'creationTime,desc');

        url.searchParams.append('swLat', bounds.swLat);
        url.searchParams.append('neLat', bounds.neLat);
        url.searchParams.append('swLng', bounds.swLng);
        url.searchParams.append('neLng', bounds.neLng);

        if (currentFilters.categories.size > 0) {
            currentFilters.categories.forEach(cat => url.searchParams.append('category', cat));
        }
        if (currentFilters.isPublic !== null) {
            url.searchParams.append('isPublic', currentFilters.isPublic);
        }

        try {
            const res = await fetch(url.toString());
            if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
            const responseData = await res.json();
            setPageData(responseData);
        } catch (e) {
            console.error('이벤트 불러오기 실패:', e);
            setPageData(null);
        }
    }, []);

    const handleApplyFilters = (newFilters) => {
        setFilters(newFilters);
    };

    const handleCategoryBarChange = (newCategoryState) => {
        setFilters(prevFilters => ({
            ...prevFilters,
            categories: newCategoryState.categories,
        }));
    };

    // 💡 4. 필터나 지도 범위 변경 시, 1페이지로 리셋하는 로직
    useEffect(() => {
        setCurrentPage(0);
    }, [currentMapBounds, filters]);

    // 💡 5. API 호출 트리거 useEffect 수정
    useEffect(() => {
        if (currentMapBounds) {
            fetchEvents(currentMapBounds, filters, currentPage);
        }
    }, [currentMapBounds, filters, currentPage, fetchEvents]);

    const handleEventCreationSuccess = useCallback(() => {
        setCreateModalOpen(false);
        if (currentMapBounds) {
            fetchEvents(currentMapBounds, filters, 0);
        }
    }, [currentMapBounds, filters, fetchEvents]);

    const handleMapIdle = useCallback((newBounds) => { setCurrentMapBounds(newBounds); }, []);
    const handleLogout = async () => { window.location.href = '/auth'; };
    const handleCardClick = (event) => { setSelectedEvent(event); };
    const handleCloseDetailModal = () => { setSelectedEvent(null); };
    const handleJoinEvent = async (eventId) => { console.log("Joining event", eventId); };

    // 💡 6. 페이지 변경 핸들러 함수 추가
    const handlePageChange = (pageNumber) => {
        setCurrentPage(pageNumber);
    };

    useEffect(() => {
        if (location && !initialCenter) {
            setInitialCenter({ lat: location.latitude, lng: location.longitude });
        } else if (!isLocationLoading && !initialCenter) {
            setInitialCenter({ lat: 37.5665, lng: 126.9780 });
        }
    }, [location, isLocationLoading, initialCenter]);

    useEffect(() => {
        if (initialCenter && !currentMapBounds) {
            const padding = 0.01;
            setCurrentMapBounds({
                swLat: initialCenter.lat - padding, swLng: initialCenter.lng - padding,
                neLat: initialCenter.lat + padding, neLng: initialCenter.lng + padding,
            });
        }
    }, [initialCenter, currentMapBounds]);

    useEffect(() => {
        document.body.classList.toggle('modal-open', isCreateModalOpen || !!selectedEvent || isFilterModalOpen);
    }, [isCreateModalOpen, selectedEvent, isFilterModalOpen]);

    if (isAuthLoading || !initialCenter) {
        return <div>페이지를 불러오는 중입니다...</div>;
    }

    // 💡 7. 렌더링 로직 수정
    const eventsOnCurrentPage = pageData ? pageData.content : [];

    return (
        <>
            <GlobalStyle />
            <Container>
                <Header user={user} onLogout={handleLogout} />
                <MainContent>
                    <FilterBarContainer>
                        <CategoryFilterWrapper>
                            <CategoryFilter
                                availableCategories={availableCategories}
                                selectedCategories={filters.categories}
                                onCategoryChange={handleCategoryBarChange}
                            />
                        </CategoryFilterWrapper>
                        <FilterButton onClick={() => setFilterModalOpen(true)}>
                            <svg viewBox="0 0 16 16" xmlns="http://www.w3.org/2000/svg" style={{display:'block',height:'16px',width:'16px',fill:'currentColor'}}><path d="M5 8c1.306 0 2.418.835 2.83 2H14v2H7.829A3.001 3.001 0 1 1 5 8zm0 2a1 1 0 1 0 0 2 1 1 0 0 0 0-2zm6-8a3 3 0 1 1-2.829 4H2V4h6.17A3.001 3.001 0 0 1 11 2zm0 2a1 1 0 1 0 0 2 1 1 0 0 0 0-2z"></path></svg>
                            <span>필터</span>
                        </FilterButton>
                    </FilterBarContainer>
                    <Body>
                        <EventSection>
                            <EventListWrapper>
                                <EventList events={eventsOnCurrentPage} onCardClick={handleCardClick} />
                            </EventListWrapper>
                            {pageData && pageData.totalPages > 1 && (
                                <Pagination
                                    currentPage={pageData.number}
                                    totalPages={pageData.totalPages}
                                    onPageChange={handlePageChange}
                                />
                            )}
                        </EventSection>
                        <MapSection>
                            <CreateEventButton onClick={() => setCreateModalOpen(true)} disabled={!user}>
                                이벤트 생성
                            </CreateEventButton>
                            <NaverMapView
                                initialCenter={initialCenter}
                                onMapIdle={handleMapIdle}
                                events={eventsOnCurrentPage}
                            />
                            <RefetchButton onClick={() => fetchEvents(currentMapBounds, filters, 0)}>
                                현재 지도에서 재검색
                            </RefetchButton>
                        </MapSection>
                    </Body>
                </MainContent>

                {isCreateModalOpen && <CreateEventModal onClose={() => setCreateModalOpen(false)} initialCenter={initialCenter} userId={user?.id} onSuccess={handleEventCreationSuccess} />}
                {selectedEvent && <EventDetailModal event={selectedEvent} onClose={handleCloseDetailModal} onJoin={handleJoinEvent} />}
                <FilterModal
                    isOpen={isFilterModalOpen}
                    onClose={() => setFilterModalOpen(false)}
                    onApply={handleApplyFilters}
                    availableCategories={availableCategories}
                    initialFilters={filters}
                />
            </Container>
        </>
    );
}

export default MainPage;