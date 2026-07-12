// src/components/RegisterPage.js
import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { registerUser } from '../redux/slices/authSlice';
import { Button, TextInput } from 'belle';
import { useNavigate } from 'react-router-dom';
import Header from './Header';

const RegisterPage = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const authState = useSelector((state) => state.auth);
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  const handleRegister = async (e) => {
    e.preventDefault();
    const resultAction = await dispatch(registerUser({ username, email, password }));
    if (registerUser.fulfilled.match(resultAction)) {
      navigate('/login');
    }
    // On failure, authState.error is updated
  };

  return (
    <div className="register-page">
      <Header />
      <div className='register-page-content'>
      <main>
        <h2>Register</h2>
        <form onSubmit={handleRegister}>
          <div>
            <label>
              Username:
              <TextInput
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Choose a username"
              />
            </label>
          </div>
          <div>
            <label>
              Email:
              <TextInput
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="Enter your email"
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
                placeholder="Choose a password"
              />
            </label>
          </div>
          {authState.error && <div className="error">{authState.error}</div>}
          <Button type="submit" disabled={authState.loading}>
            {authState.loading ? 'Registering...' : 'Register'}
          </Button>
        </form>
      </main>
        </div>
    </div>
  );
};

export default RegisterPage;