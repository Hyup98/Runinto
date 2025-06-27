// src/components/Pagination.jsx
import React from 'react';
import styled from 'styled-components';

const PaginationContainer = styled.div`
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 24px;
`;

const PageButton = styled.button`
    padding: 8px 12px;
    margin: 0 4px;
    border: 1px solid #ddd;
    border-radius: 4px;
    background-color: ${props => (props.$isActive ? '#ff385c' : 'white')};
    color: ${props => (props.$isActive ? 'white' : 'black')};
    font-weight: ${props => (props.$isActive ? 'bold' : 'normal')};
    cursor: pointer;
    transition: background-color 0.2s;

    &:disabled {
        cursor: not-allowed;
        color: #ccc;
    }

    &:hover:not(:disabled) {
        background-color: #f0f0f0;
    }
`;

function Pagination({ currentPage, totalPages, onPageChange }) {
    if (totalPages <= 1) {
        return null; // 전체 페이지가 1 이하면 페이지네이션을 표시하지 않음
    }

    const handlePageClick = (pageNumber) => {
        onPageChange(pageNumber);
    };

    return (
        <PaginationContainer>
            <PageButton onClick={() => handlePageClick(currentPage - 1)} disabled={currentPage === 0}>
                이전
            </PageButton>
            {/* 페이지 번호들을 여기에 렌더링 할 수 있습니다 (간단한 버전) */}
            {[...Array(totalPages).keys()].map(number => (
                <PageButton
                    key={number}
                    $isActive={number === currentPage}
                    onClick={() => handlePageClick(number)}
                >
                    {number + 1}
                </PageButton>
            ))}
            <PageButton onClick={() => handlePageClick(currentPage + 1)} disabled={currentPage >= totalPages - 1}>
                다음
            </PageButton>
        </PaginationContainer>
    );
}

export default Pagination;