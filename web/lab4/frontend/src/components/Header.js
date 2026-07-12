// src/components/Header.js
import React from 'react';
import { Link } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { logout } from '../redux/slices/authSlice';
import { Button } from 'belle';

const Header = () => {
  const isAuthenticated = useSelector((state) => state.auth.isAuthenticated);
  const dispatch = useDispatch();

  const handleLogout = () => {
    dispatch(logout());
  };

  return (
    <header className="header">
        <Link to="/">🏠</Link>
        <p>P3207</p>
        <p>Chusovlianov Maksim ✅ </p>
        <p>409858</p>
      <div className="header-links">
        {isAuthenticated ? (
          <Button onClick={handleLogout}>Logout</Button>
        ) : (
          <>
            <Link to="/login">
              <Button>Login</Button>
            </Link>
            <Link to="/register">
              <Button>Register</Button>
            </Link>
          </>
        )}
      </div>
    </header>
  );
};

export default Header;