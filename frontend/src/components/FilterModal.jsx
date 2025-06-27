import React, { useState, useEffect } from 'react';
import styled, { keyframes } from 'styled-components';

const fadeIn = keyframes`
    from { opacity: 0; }
    to { opacity: 1; }
`;

const slideIn = keyframes`
    from { transform: translateY(30px); opacity: 0; }
    to { transform: translateY(0); opacity: 1; }
`;

const ModalOverlay = styled.div`
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.6);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 3000;
    animation: ${fadeIn} 0.3s ease-out;
`;

const ModalContent = styled.div`
    background-color: white;
    border-radius: 16px;
    width: 90%;
    max-width: 780px;
    max-height: 90vh;
    display: flex;
    flex-direction: column;
    animation: ${slideIn} 0.4s cubic-bezier(0.25, 1, 0.5, 1);
    box-shadow: 0 8px 28px rgba(0,0,0,0.28);
`;

const ModalHeader = styled.div`
    padding: 16px 24px;
    border-bottom: 1px solid #ebebeb;
    font-size: 16px;
    font-weight: 700;
    text-align: center;
    position: relative;
`;

const CloseButton = styled.button`
    position: absolute;
    left: 16px;
    top: 50%;
    transform: translateY(-50%);
    background: #f1f1f1;
    border: none;
    border-radius: 50%;
    width: 32px;
    height: 32px;
    font-size: 20px;
    line-height: 1;
    cursor: pointer;
    transition: background-color 0.2s;
    &:hover { background-color: #e0e0e0; }
`;

const ModalBody = styled.div`
    padding: 24px 32px;
    overflow-y: auto;
`;

const FilterSection = styled.div`
    padding-bottom: 32px;
    margin-bottom: 32px;
    border-bottom: 1px solid #ebebeb;
    &:last-of-type {
        border-bottom: none;
        margin-bottom: 0;
    }
`;

const SectionTitle = styled.h3`
    font-size: 22px;
    font-weight: 600;
    margin: 0 0 24px 0;
`;

const OptionsGrid = styled.div`
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 16px;
`;

const OptionButton = styled.button`
    padding: 24px;
    border: 1px solid ${props => props.$isSelected ? '#222' : '#ddd'};
    border-radius: 12px;
    cursor: pointer;
    font-weight: 600;
    font-size: 16px;
    text-align: center;
    transition: all 0.2s;
    background-color: ${props => props.$isSelected ? '#f7f7f7' : '#fff'};

    &:hover {
        border-color: #222;
    }
`;

const SliderContainer = styled.div`
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
`;

const SliderValue = styled.span`
    font-size: 16px;
    font-weight: 600;
    color: #222;
    background-color: #f7f7f7;
    padding: 8px 16px;
    border-radius: 8px;
`;

const RangeInput = styled.input`
    width: 100%;
    -webkit-appearance: none;
    appearance: none;
    height: 8px;
    background: #e0e0e0;
    border-radius: 4px;
    outline: none;

    &::-webkit-slider-thumb {
        -webkit-appearance: none;
        appearance: none;
        width: 24px;
        height: 24px;
        background: #FF385C;
        border-radius: 50%;
        cursor: pointer;
        border: 2px solid white;
        box-shadow: 0 0 4px rgba(0,0,0,0.2);
    }

    &::-moz-range-thumb {
        width: 24px;
        height: 24px;
        background: #FF385C;
        border-radius: 50%;
        cursor: pointer;
        border: 2px solid white;
        box-shadow: 0 0 4px rgba(0,0,0,0.2);
    }
`;


const ModalFooter = styled.div`
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 24px;
    border-top: 1px solid #ebebeb;
`;

const ClearButton = styled.button`
    font-size: 16px;
    font-weight: 600;
    text-decoration: underline;
    background: none;
    border: none;
    cursor: pointer;
    padding: 8px;
`;

const ApplyButton = styled.button`
    background-color: #222;
    color: white;
    padding: 14px 24px;
    border: none;
    border-radius: 8px;
    font-size: 16px;
    font-weight: 600;
    cursor: pointer;
    transition: opacity 0.2s;
    &:hover { opacity: 0.9; }
`;

function FilterModal({ isOpen, onClose, onApply, availableCategories, initialFilters }) {
    const [localFilters, setLocalFilters] = useState({
        categories: new Set(),
        isPublic: null,
        maxParticipants: 50,
    });

    useEffect(() => {
        if (isOpen) {
            setLocalFilters({
                categories: new Set(initialFilters.categories),
                isPublic: initialFilters.isPublic,
                maxParticipants: initialFilters.maxParticipants || 50,
            });
        }
    }, [isOpen, initialFilters]);

    const handleCategoryClick = (category) => {
        setLocalFilters(prev => {
            const newCategories = new Set(prev.categories);
            if (newCategories.has(category)) {
                newCategories.delete(category);
            } else {
                newCategories.add(category);
            }
            return { ...prev, categories: newCategories };
        });
    };

    const handleIsPublicChange = (value) => {
        setLocalFilters(prev => ({ ...prev, isPublic: value }));
    };

    const handleParticipantsChange = (e) => {
        setLocalFilters(prev => ({...prev, maxParticipants: Number(e.target.value)}));
    };

    const handleApply = () => {
        onApply(localFilters);
        onClose();
    };

    const handleClear = () => {
        setLocalFilters({
            categories: new Set(),
            isPublic: null,
            maxParticipants: 50,
        });
    }

    if (!isOpen) return null;

    return (
        <ModalOverlay onClick={onClose}>
            <ModalContent onClick={e => e.stopPropagation()}>
                <ModalHeader>
                    <CloseButton onClick={onClose}>×</CloseButton>
                    필터
                </ModalHeader>
                <ModalBody>
                    <FilterSection>
                        <SectionTitle>모임 종류</SectionTitle>
                        <OptionsGrid>
                            <OptionButton $isSelected={localFilters.isPublic === null} onClick={() => handleIsPublicChange(null)}>전체</OptionButton>
                            <OptionButton $isSelected={localFilters.isPublic === true} onClick={() => handleIsPublicChange(true)}>공개 모임</OptionButton>
                            <OptionButton $isSelected={localFilters.isPublic === false} onClick={() => handleIsPublicChange(false)}>비공개 모임</OptionButton>
                        </OptionsGrid>
                    </FilterSection>

                    <FilterSection>
                        <SectionTitle>최대 인원</SectionTitle>
                        <SliderContainer>
                            <SliderValue>{localFilters.maxParticipants}명 이하</SliderValue>
                            <RangeInput
                                type="range"
                                min="2"
                                max="50"
                                step="1"
                                value={localFilters.maxParticipants}
                                onChange={handleParticipantsChange}
                            />
                        </SliderContainer>
                    </FilterSection>

                    <FilterSection>
                        <SectionTitle>카테고리</SectionTitle>
                        <OptionsGrid>
                            {availableCategories.map(cat => (
                                <OptionButton key={cat} $isSelected={localFilters.categories.has(cat)} onClick={() => handleCategoryClick(cat)}>
                                    {cat}
                                </OptionButton>
                            ))}
                        </OptionsGrid>
                    </FilterSection>
                </ModalBody>
                <ModalFooter>
                    <ClearButton onClick={handleClear}>전체 해제</ClearButton>
                    <ApplyButton onClick={handleApply}>
                        결과 보기
                    </ApplyButton>
                </ModalFooter>
            </ModalContent>
        </ModalOverlay>
    );
}

export default FilterModal;
