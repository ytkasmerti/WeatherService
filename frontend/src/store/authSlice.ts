import { createSlice, type PayloadAction } from '@reduxjs/toolkit';

interface AuthState {
    isAuth: boolean | null;
}

const initialState: AuthState = {
    isAuth: null,
};

export const authSlice = createSlice({
    name: 'auth',
    initialState,
    reducers: {
        setAuth: (state, action: PayloadAction<boolean>) => {
            state.isAuth = action.payload;
        },
    },
});

export const { setAuth } = authSlice.actions;
export default authSlice.reducer;