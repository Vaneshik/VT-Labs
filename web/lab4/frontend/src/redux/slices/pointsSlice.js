import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import axios from 'axios';

// Асинхронный экшен: получить список точек (GET /api/points)
export const fetchPoints = createAsyncThunk(
    'points/fetchPoints',
    async (_, { getState, rejectWithValue }) => {
        try {
            // Достаём token из auth-стейта
            const token = getState().auth.token;

            const response = await axios.get('http://vnshk.ru:7777/api/points', {
                headers: {
                    Authorization: token ? `Bearer ${token}` : '',
                },
            });

            return response.data;
            // Предполагаем, что сервер возвращает массив объектов { x, y, r, hit, timestamp }
        } catch (err) {
            return rejectWithValue(err.response?.data || 'Failed to fetch points');
        }
    }
);

// Асинхронный экшен: создать новую точку (POST /api/points)
export const createPoint = createAsyncThunk(
    'points/createPoint',
    async ({ x, y, r }, { getState, rejectWithValue }) => {
        try {
            // Достаём token из auth-стейта
            const token = getState().auth.token;

            const requestBody = { x, y, r };
            const response = await axios.post('http://vnshk.ru:7777/api/points', requestBody, {
                headers: {
                    Authorization: token ? `Bearer ${token}` : '',
                },
            });

            return response.data;
            // Предполагаем, что сервер возвращает сохранённую точку: { x, y, r, hit, timestamp, ... }
        } catch (err) {
            return rejectWithValue(err.response?.data || 'Failed to create point');
        }
    }
);

const initialState = {
    points: [],
    loading: false,
    error: null,

    form: {
        x: '',
        y: '',
        r: '',
        error: null,
    }
};

const pointsSlice = createSlice({
    name: 'points',
    initialState,
    reducers: {
        clearPoints(state) {
            state.points = [];
        },
        setFormField(state, action) {
            const { field, value } = action.payload;
            state.form[field] = value;
        },
        // Установим ошибку формы
        setFormError(state, action) {
            state.form.error = action.payload;
        },
        // Очистим форму
        resetForm(state) {
            state.form.x = '';
            state.form.y = '';
            state.form.r = '';
            state.form.error = '';
        },
    },
    extraReducers: (builder) => {
        builder
            // fetchPoints
            .addCase(fetchPoints.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(fetchPoints.fulfilled, (state, action) => {
                state.loading = false;
                state.points = action.payload;
            })
            .addCase(fetchPoints.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload || 'Error fetching points';
            })

            // createPoint
            .addCase(createPoint.pending, (state) => {
                state.loading = true;
                state.error = null;
            })
            .addCase(createPoint.fulfilled, (state, action) => {
                state.loading = false;
                // Добавляем точку в массив
                state.points.push(action.payload);
            })
            .addCase(createPoint.rejected, (state, action) => {
                state.loading = false;
                state.error = action.payload || 'Error creating point';
            });
    },
});

export const {
    setFormField,
    setFormError,
    resetForm
} = pointsSlice.actions;
export default pointsSlice.reducer;