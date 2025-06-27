import React from 'react';
import styled from 'styled-components';

// 카테고리 아이콘 객체를 완전히 제거했습니다.

const FilterContainer = styled.div`
    display: flex;
    padding: 16px 0;
    gap: 12px; /* 버튼 간 간격을 조정합니다. */
    background-color: #fff;
    flex-shrink: 0;
`;

const FilterButton = styled.button`
    display: flex;
    align-items: center;
    justify-content: center;
    background: transparent;
    border: 1px solid transparent; /* 기본 테두리는 보이지 않게 처리합니다. */
    cursor: pointer;
    padding: 8px 16px; /* 텍스트에 어울리는 패딩으로 변경합니다. */
    border-radius: 20px; /* 둥근 알약 형태의 버튼으로 디자인합니다. */
    transition: all 0.2s ease-in-out;

    /* 선택 상태에 따라 다른 스타일을 적용합니다. */
    color: ${props => (props.$isSelected ? '#fff' : '#222')};
    background-color: ${props => (props.$isSelected ? '#222' : '#f7f7f7')};
    font-weight: 600;

    &:hover {
        /* 선택 여부와 관계없이 호버 시에는 좀 더 어두운 배경색을 적용합니다. */
        background-color: ${props => (props.$isSelected ? '#000' : '#e7e7e7')};
    }
`;

const CategoryName = styled.span`
    font-size: 14px; /* 가독성을 위해 폰트 크기를 키웁니다. */
    white-space: nowrap; /* 텍스트가 줄바꿈되지 않도록 합니다. */
`;

function CategoryFilter({ availableCategories, selectedCategories, onCategoryChange }) {
    const handleCategoryClick = (category) => {
        const newSelected = new Set(selectedCategories);
        if (newSelected.has(category)) {
            newSelected.delete(category);
        } else {
            newSelected.add(category);
        }
        // MainPage.jsx 와의 데이터 형식 호환을 위해 객체 형태로 전달합니다.
        onCategoryChange({ categories: newSelected });
    };

    return (
        <FilterContainer>
            {availableCategories.map(category => {
                const isSelected = selectedCategories.has(category);
                return (
                    <FilterButton
                        key={category}
                        $isSelected={isSelected}
                        onClick={() => handleCategoryClick(category)}
                    >
                        {/* 아이콘 관련 로직을 모두 제거하고 CategoryName만 표시합니다. */}
                        <CategoryName>{category}</CategoryName>
                    </FilterButton>
                );
            })}
        </FilterContainer>
    );
}

export default CategoryFilter;
