import { createContext, useContext, useReducer, useEffect } from 'react';
import { authApi } from '../services/api';

const AuthContext = createContext();

const initialState = {
  user: null,
  token: localStorage.getItem('accessToken'),
  refreshToken: localStorage.getItem('refreshToken'),
  userId: localStorage.getItem('userId'),
  isAuthenticated: !!localStorage.getItem('accessToken'),
  loading: false,
};

function authReducer(state, action) {
  switch (action.type) {
    case 'LOGIN_SUCCESS':
      return {
        ...state,
        user: action.payload.user,
        token: action.payload.accessToken,
        refreshToken: action.payload.refreshToken,
        userId: action.payload.userId,
        isAuthenticated: true,
        loading: false,
      };
    case 'LOGOUT':
      return {
        ...state,
        user: null,
        token: null,
        refreshToken: null,
        userId: null,
        isAuthenticated: false,
        loading: false,
      };
    case 'SET_LOADING':
      return { ...state, loading: action.payload };
    default:
      return state;
  }
}

export function AuthProvider({ children }) {
  const [state, dispatch] = useReducer(authReducer, initialState);

  useEffect(() => {
    // Check if we have a valid token on mount
    if (state.token) {
      dispatch({ type: 'SET_LOADING', payload: false });
    } else {
      dispatch({ type: 'SET_LOADING', payload: false });
    }
  }, []);

  const login = async (credentials) => {
    const res = await authApi.login(credentials);
    const data = res.data.data;
    
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    localStorage.setItem('userId', data.userId);
    localStorage.setItem('username', data.username);

    dispatch({
      type: 'LOGIN_SUCCESS',
      payload: {
        user: { username: data.username, roles: data.roles },
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
        userId: data.userId,
      },
    });

    return data;
  };

  const register = async (userData) => {
    const res = await authApi.register(userData);
    const data = res.data.data;
    
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    localStorage.setItem('userId', data.userId);
    localStorage.setItem('username', data.username);

    dispatch({
      type: 'LOGIN_SUCCESS',
      payload: {
        user: { username: data.username, roles: data.roles },
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
        userId: data.userId,
      },
    });

    return data;
  };

  const logout = async () => {
    try {
      if (state.refreshToken) {
        await authApi.logout(state.refreshToken);
      }
    } catch (e) {
      // ignore
    }
    localStorage.clear();
    dispatch({ type: 'LOGOUT' });
  };

  return (
    <AuthContext.Provider value={{ ...state, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
