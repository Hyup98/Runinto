// src/components/SignupForm.js
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styled from 'styled-components';

// --- Styled-components 정의 ---
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

    /* 라디오 버튼용 스타일 추가 */
    &[type="radio"] {
        width: auto;
        margin-right: 8px;
        vertical-align: middle;
    }

    /* Textarea 스타일 상속 */
    &[as="textarea"] {
        resize: vertical;
        min-height: 80px;
    }

    &:focus {
        border-color: #FF385C;
        outline: none;
        box-shadow: 0 0 0 3px rgba(255, 56, 92, 0.15);
    }
`;

const RadioLabel = styled.label`
    display: inline-flex;
    align-items: center;
    margin-right: 24px;
    font-size: 16px;
    cursor: pointer;
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
    margin-top: 10px;

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

function SignupForm({ onToggleView }) {
    const [error, setError] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        const form = e.target;

        // 1. RegisterRequest DTO에 맞춰 전송할 데이터 객체 수정
        const registerRequest = {
            name: form.name.value,
            email: form.email.value,
            password: form.password.value,
            description: form.description.value,
            gender: form.gender.value,
            age: form.age.value ? parseInt(form.age.value, 10) : null,
        };

        const image = form.image.files[0];
        const formData = new FormData();

        formData.append('profile', new Blob([JSON.stringify(registerRequest)], { type: 'application/json' }));
        if (image) {
            formData.append('image', image);
        }

        try {
            const response = await fetch('/auth/signup', {
                method: 'POST',
                body: formData,
                credentials: 'include',
            });

            if (response.status === 201) {
                // 1. 회원가입 후 자동 로그인 대신, 로그인 페이지로 이동하도록 수정
                alert("회원가입이 성공적으로 완료되었습니다. 로그인 페이지로 이동합니다.");
                onToggleView(); // 부모 컴포넌트(AuthPage)의 화면 전환 함수 호출
            } else {
                const errorText = await response.text();
                setError(errorText || "회원가입에 실패했습니다.");
            }
        } catch (err) {
            setError('네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        }
    };

    return (
        <Form onSubmit={handleSubmit}>
            <Title>회원가입</Title>
            <InputGroup>
                <Label>이름</Label>
                <Input type="text" name="name" required />
            </InputGroup>
            <InputGroup>
                <Label>이메일</Label>
                <Input type="email" name="email" required />
            </InputGroup>
            <InputGroup>
                <Label>비밀번호</Label>
                <Input type="password" name="password" required />
            </InputGroup>

            {/* --- DTO에 맞춰 추가된 필드들 --- */}
            <InputGroup>
                <Label>자기소개 (선택)</Label>
                <Input as="textarea" name="description" placeholder="간단한 자기소개를 입력해주세요." />
            </InputGroup>
            <InputGroup>
                <Label>성별</Label>
                <div>
                    <RadioLabel>
                        <Input type="radio" name="gender" value="MALE" required /> 남성
                    </RadioLabel>
                    <RadioLabel>
                        <Input type="radio" name="gender" value="FEMALE" /> 여성
                    </RadioLabel>
                </div>
            </InputGroup>
            <InputGroup>
                <Label>나이</Label>
                <Input type="number" name="age" min="1" required />
            </InputGroup>
            {/* --- 추가된 필드 끝 --- */}

            <InputGroup>
                <Label>프로필 이미지 (선택)</Label>
                <Input type="file" name="image" accept="image/*" />
            </InputGroup>

            <ErrorMessage>{error}</ErrorMessage>

            <Button type="submit">계정 만들기</Button>

            <ToggleLink>
                이미 계정이 있으신가요? <span onClick={onToggleView}>로그인</span>
            </ToggleLink>
        </Form>
    );
}

export default SignupForm;