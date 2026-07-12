// src/components/LoginPage.js
import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { loginUser } from '../redux/slices/authSlice';
import { Button, TextInput } from 'belle';
import { useNavigate } from 'react-router-dom';
import Header from './Header';

const LoginPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const authState = useSelector((state) => state.auth);
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    // Dispatch async login action
    const resultAction = await dispatch(loginUser({ username, password }));
    if (loginUser.fulfilled.match(resultAction)) {
      navigate('/main');
    }
    // On failure, the error is stored in authState.error
  };

  return (
    <div className="login-page">
      <Header />
      <div className='login-page-content'>
      <main>
        <h2>Login</h2>
        <form onSubmit={handleLogin}>
          <div>
            <label>
              Username:
              <TextInput
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Enter your username"
              />
            </label>
          </div>
          <div>
            <label>
              Password:
              <TextInput
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter your password"
              />
            </label>
          </div>
          {authState.error && <div className="error">{authState.error}</div>}
          <Button type="submit" disabled={authState.loading}>
            {authState.loading ? 'Logging in...' : 'Login'}
          </Button>
        </form>
      </main>
      </div>
    </div>
  );
};

export default LoginPage;