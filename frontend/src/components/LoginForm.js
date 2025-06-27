// src/components/LoginForm.js
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';

const Form = styled.form`
    display: flex;
    flex-direction: column;
`;

const Title = styled.h2`
    font-size: 28px;
    font-weight: 700;
    color: #222;
    text-align: center;
    margin-bottom: 32px;
`;

const InputGroup = styled.div`
    margin-bottom: 20px;
`;

const Label = styled.label`
    display: block;
    font-size: 14px;
    font-weight: 500;
    color: #555;
    margin-bottom: 8px;
`;

const Input = styled.input`
    width: 100%;
    padding: 14px 16px;
    font-size: 16px;
    border: 1px solid #ddd;
    border-radius: 12px;
    transition: all 0.2s ease;

    &:focus {
        border-color: #FF385C;
        outline: none;
        box-shadow: 0 0 0 3px rgba(255, 56, 92, 0.15);
    }
`;

const Button = styled.button`
    width: 100%;
    padding: 15px;
    font-size: 16px;
    font-weight: 600;
    color: #fff;
    background: linear-gradient(90deg, #FF385C 0%, #E61E4D 100%);
    border: none;
    border-radius: 12px;
    cursor: pointer;
    transition: opacity 0.2s;

    &:hover {
        opacity: 0.9;
    }
`;

const ToggleLink = styled.p`
    text-align: center;
    margin-top: 24px;
    font-size: 14px;
    color: #555;

    span {
        color: #FF385C;
        font-weight: 600;
        cursor: pointer;
        &:hover {
            text-decoration: underline;
        }
    }
`;

const ErrorMessage = styled.p`
    color: #d91b42;
    font-size: 14px;
    text-align: center;
    margin-top: -10px;
    margin-bottom: 15px;
    height: 16px;
`;

function LoginForm({ onToggleView }) {
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        const { email, password } = e.target.elements;

        try {
            const response = await fetch('/auth/signin', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify({ email: email.value, password: password.value }),
            });

            if (response.ok) {
                const loginData = await response.json();

                // 2. 로그인 후 받은 유저 아이디를 로그로 찍어줍니다.
                console.log("로그인 성공! 서버로부터 받은 userId:", loginData.userId);

                navigate('/');
            } else {
                const errorText = await response.text();
                setError(errorText || '로그인에 실패했습니다. 아이디 또는 비밀번호를 확인해주세요.');
            }
        } catch (err) {
            setError('네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        }
    };

    return (
        <Form onSubmit={handleSubmit}>
            <Title>로그인</Title>
            <InputGroup>
                <Label>이메일</Label>
                <Input type="email" name="email" required />
            </InputGroup>
            <InputGroup>
                <Label>비밀번호</Label>
                <Input type="password" name="password" required />
            </InputGroup>
            <ErrorMessage>{error}</ErrorMessage>
            <Button type="submit">로그인</Button>
            <ToggleLink>
                계정이 없으신가요? <span onClick={onToggleView}>회원가입</span>
            </ToggleLink>
        </Form>
    );
}
export default LoginForm;