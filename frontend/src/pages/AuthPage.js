// src/pages/AuthPage.js (파일 경로를 pages로 변경하는 것을 추천합니다)
import React, { useState } from 'react';
import styled from 'styled-components';
import LoginForm from '../components/LoginForm';
import SignupForm from '../components/SignupForm';

const AuthContainer = styled.div`
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    background-color: #f5f5f7;
`;

const FormWrapper = styled.div`
    background-color: #fff;
    padding: 40px;
    border-radius: 18px;
    box-shadow: 0 4px 25px rgba(0, 0, 0, 0.1);
    width: 100%;
    max-width: 420px;
`;

function AuthPage() {
    const [isLoginView, setIsLoginView] = useState(true);

    const toggleView = () => setIsLoginView(!isLoginView);

    return (
        <AuthContainer>
            <FormWrapper>
                {isLoginView ? (
                    <LoginForm onToggleView={toggleView} />
                ) : (
                    <SignupForm onToggleView={toggleView} />
                )}
            </FormWrapper>
        </AuthContainer>
    );
}

export default AuthPage;