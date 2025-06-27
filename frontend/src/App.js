// src/App.js
import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import MainPage from './pages/MainPage';
import AuthPage from './pages/AuthPage';
import MyEventsPage from './pages/MyEventsPage'; // 새로 만든 페이지 임포트

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<MainPage />} />
                <Route path="/auth" element={<AuthPage />} />

                {/* '참가한 이벤트'와 '내가 만든 이벤트' 모두 MyEventsPage를 사용합니다. */}
                <Route path="/my-events" element={<MyEventsPage />} />
                <Route path="/created-events" element={<MyEventsPage />} />

                {/* TODO: 다른 페이지 라우트들을 여기에 추가할 수 있습니다. */}
            </Routes>
        </BrowserRouter>
    );
}

export default App;