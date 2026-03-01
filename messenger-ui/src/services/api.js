import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || '',
  headers: { 'Content-Type': 'application/json' },
});

// JWT Interceptor
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  // Add X-User-Id header for messaging/user service
  const userId = localStorage.getItem('userId');
  if (userId) {
    config.headers['X-User-Id'] = userId;
  }
  return config;
});

// Auto-refresh or redirect on 401
api.interceptors.response.use(
  (res) => res,
  async (error) => {
    if (error.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ===== Auth API =====
export const authApi = {
  register: (data) => api.post('/api/auth/register', data),
  login: (data) => api.post('/api/auth/login', data),
  refresh: (refreshToken) => api.post('/api/auth/refresh', { refreshToken }),
  logout: (refreshToken) => api.post('/api/auth/logout', { refreshToken }),
};

// ===== User API =====
export const userApi = {
  getProfile: (userId) => {
    if (userId) {
      return api.get(`/api/users/profile/${userId}`);
    }
    return api.get('/api/users/profile');
  },
  updateProfile: (data) => api.post('/api/users/profile', data),
  getContacts: () => api.get('/api/users/contacts'),
  addContact: (data) => api.post('/api/users/contacts', data),
  searchUsers: (query) => api.get(`/api/users/search?q=${encodeURIComponent(query)}`),
};

// ===== Messaging API =====
export const messagingApi = {
  getConversations: (page = 0, size = 20) =>
    api.get(`/api/conversations?page=${page}&size=${size}`),
  createConversation: (data) => api.post('/api/conversations', data),
  getConversation: (id) => api.get(`/api/conversations/${id}`),
  getMessages: (conversationId, page = 0, size = 50) =>
    api.get(`/api/messages/${conversationId}?page=${page}&size=${size}`),
  sendMessage: (data) => api.post('/api/messages', data),
  markAsRead: (conversationId) =>
    api.put(`/api/messages/${conversationId}/read`),
  markAsDelivered: (conversationId) =>
    api.put(`/api/messages/${conversationId}/delivered`),
};

export default api;
