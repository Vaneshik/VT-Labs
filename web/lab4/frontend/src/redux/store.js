import { configureStore } from '@reduxjs/toolkit';
import authReducer from './slices/authSlice';
import pointsReducer from './slices/pointsSlice';

const token = localStorage.getItem('token');
const username = localStorage.getItem('username');

const preloadedState = {
  auth: {
    isAuthenticated: !!token,
    token: token || null,
    user: username ? { username } : null,
    loading: false,
    error: null
  }
};

export const store = configureStore({
  reducer: {
    auth: authReducer,
    points: pointsReducer,
  },
  preloadedState
});

export default store;