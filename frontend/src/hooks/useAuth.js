// src/pages/AuthPage.js
import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';
import LoginForm from '../components/LoginForm';
import SignupForm from '../components/SignupForm';

const AuthContainer = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #f7f7f7; // 부드러운 배경색
`;

const FormWrapper = styled.div`
  background-color: #fff;
  padding: 40px 48px;
  border-radius: 24px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 440px;
  transition: all 0.3s ease-in-out;
`;

function useAuth() {
    const [user, setUser] = useState(null);
    const [isLoading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const checkAuth = async () => {
            try {
                // 백엔드에 현재 세션의 프로필 정보를 요청
                const response = await fetch('/auth/profile', { credentials: 'include' });

                if (response.ok) {
                    // 응답 성공 시 (로그인 상태): 사용자 정보를 state에 저장
                    const userData = await response.json();
                    setUser(userData);
                } else {
                    // 응답 실패 시 (비로그인 상태): 로그인 페이지로 리디렉션
                    console.log("로그인 필요, /auth 페이지로 이동합니다.");
                    navigate('/auth');
                }
            } catch (error) {
                console.error("인증 확인 중 오류 발생:", error);
                navigate('/auth'); // 네트워크 오류 등이 발생해도 로그인 페이지로 이동
            } finally {
                setLoading(false); // 로딩 상태 종료
            }
        };

        checkAuth();
    }, [navigate]);

    return { user, isLoading };
}

export default useAuth;