import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import axios from 'axios';

axios.defaults.withCredentials = false;

export const loginUser = createAsyncThunk(
    'auth/loginUser',
    async ({ username, password }, { rejectWithValue }) => {
        try {
            const response = await axios.post('http://vnshk.ru:7777/api/auth/login', {
                username,
                password,
            });
            return response.data;
        } catch (err) {
            return rejectWithValue(err.response?.data || 'Login failed');
        }
    }
);

export const registerUser = createAsyncThunk(
    'auth/registerUser',
    async ({ username, email, password }, { rejectWithValue }) => {
        try {
            const response = await axios.post('http://vnshk.ru:7777/api/auth/register', {
                username,
                email,
                password,
            });
            return response.data;
        } catch (err) {
            return rejectWithValue(err.response?.data || 'Registration failed');
        }
    }
);

const initialState = {
    isAuthenticated: false,
    user: null,
    token: null,
    loading: false,
    error: null,
};

const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        logout(state) {
            state.isAuthenticated = false;
            state.user = null;
            state.token = null;
            localStorage.removeItem('token');
            localStorage.removeItem('username');
        },
    },
    extraReducers: (builder) => {
        builder
            // ================== LOGIN ==================
            .addCase(loginUser.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(loginUser.fulfilled, (state, action) => {
                state.loading = false;
                state.isAuthenticated = true;

                const { token, username, role, user } = action.payload;

                state.token = token || null;
                state.user = user || { username, role };

                localStorage.setItem('token', action.payload.token);
                localStorage.setItem('username', action.payload.username);
            })
            .addCase(loginUser.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload || 'Login failed';
            })

            // ================== REGISTER ==================
            .addCase(registerUser.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(registerUser.fulfilled, (state, action) => {
                state.loading = false;
                state.isAuthenticated = true;
                const { token, username, role, user } = action.payload || {};

                state.token = token || null;
                state.user = user || { username, role };
            })
            .addCase(registerUser.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload || 'Registration failed';
            });
    },
});

export const { logout } = authSlice.actions;
export default authSlice.reducer;