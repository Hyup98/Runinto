// src/components/Header.jsx
import React, { useState, useEffect, useRef } from 'react';
import styled from 'styled-components';
import { Link, useNavigate } from 'react-router-dom';

const HeaderContainer = styled.header`
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 24px;
    height: 80px;
    border-bottom: 1px solid #ebebeb;
    background-color: white;
    position: sticky;
    top: 0;
    z-index: 1100;
`;

const Logo = styled(Link)`
    font-size: 24px;
    font-weight: bold;
    color: #ff385c;
    text-decoration: none;
`;

const UserMenuContainer = styled.div`
    position: relative;
`;

const ProfileButton = styled.button`
    display: flex;
    align-items: center;
    padding: 5px 5px 5px 12px;
    border: 1px solid #ddd;
    border-radius: 21px;
    background-color: white;
    cursor: pointer;
    transition: box-shadow 0.2s;

    &:hover {
        box-shadow: 0 2px 4px rgba(0,0,0,0.18);
    }
`;

const HamburgerIcon = styled.div`
    margin-right: 12px;
    font-size: 16px;
    color: #222;
`;

const ProfileImage = styled.img`
    width: 30px;
    height: 30px;
    border-radius: 50%;
    object-fit: cover;
`;

const DropdownMenu = styled.div`
    position: absolute;
    top: 52px;
    right: 0;
    width: 240px;
    background-color: white;
    border-radius: 12px;
    box-shadow: 0 4px 16px rgba(0,0,0,0.12);
    padding: 8px 0;
`;

const MenuItem = styled(Link)`
    display: block;
    padding: 12px 16px;
    font-size: 14px;
    color: #222;
    text-decoration: none;
    cursor: pointer;

    &:hover {
        background-color: #f7f7f7;
    }
`;

const MenuButton = styled.div`
    padding: 12px 16px;
    font-size: 14px;
    color: #d91b42;
    font-weight: bold;
    border-top: 1px solid #ebebeb;
    cursor: pointer;
    &:hover { background-color: #f7f7f7; }
`;

const LoginButton = styled.button`
    padding: 10px 18px;
    background-color: #ff385c;
    color: white;
    font-weight: bold;
    border: none;
    border-radius: 8px;
    font-size: 14px;
    cursor: pointer;
    transition: background-color 0.2s;
    &:hover { background-color: #e11d48; }
`;

function Header({ user, onLogout }) {
    const [isMenuOpen, setMenuOpen] = useState(false);
    const menuRef = useRef(null);
    const navigate = useNavigate();

    useEffect(() => {
        const handleClickOutside = (event) => {
            if (menuRef.current && !menuRef.current.contains(event.target)) {
                setMenuOpen(false);
            }
        };
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, [menuRef]);

    const handleLoginClick = () => navigate('/auth');

    return (
        <HeaderContainer>
            <Logo to="/">우리동네모임</Logo>
            <UserMenuContainer ref={menuRef}>
                {user ? (
                    <>
                        <ProfileButton onClick={() => setMenuOpen(prev => !prev)}>
                            <HamburgerIcon>☰</HamburgerIcon>
                            <ProfileImage src={user.imgUrl || '/img/default.png'} alt="My Profile" />
                        </ProfileButton>

                        {isMenuOpen && (
                            <DropdownMenu>
                                <MenuItem to={`/profile/${user.id}`}>마이 프로필</MenuItem>
                                <MenuItem to={`/my-events`}>참가한 이벤트</MenuItem>
                                <MenuItem to={`/created-events`}>이벤트 관리</MenuItem>
                                <MenuButton onClick={onLogout}>로그아웃</MenuButton>
                            </DropdownMenu>
                        )}
                    </>
                ) : (
                    <LoginButton onClick={handleLoginClick}>로그인</LoginButton>
                )}
            </UserMenuContainer>
        </HeaderContainer>
    );
}

export default Header;