import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { useSelector } from 'react-redux';

import LandingPage from './components/LandingPage';
import MainPage from './components/MainPage';
import LoginPage from './components/LoginPage';
import RegisterPage from './components/RegisterPage';

function App() {
  const isAuthenticated = useSelector((state) => state.auth.isAuthenticated);

  return (
    <div className="App">
      <Routes>
        <Route path="/" element={<LandingPage />} />
        <Route
          path="/main"
          element={isAuthenticated ? <MainPage /> : <Navigate to="/login" replace />}
        />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={isAuthenticated ? <MainPage/> : <RegisterPage />} />
      </Routes>
    </div>
  );
}

export default App;