import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  headers: { 'Content-Type': 'application/json' },
  timeout: 15000,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token') || localStorage.getItem('terrastay_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (res) => res,
  (error) => {
    const status = error.response?.status;
    const path = window.location.pathname;

    if (status === 401) {
      localStorage.removeItem('auth_token');
      localStorage.removeItem('auth_user');
      localStorage.removeItem('terrastay_token');
      localStorage.removeItem('terrastay_user');
      window.dispatchEvent(new CustomEvent('auth:logout'));

      if (path !== '/login') {
        window.location.assign('/login');
      }
    } else if (status === 403) {
      if (path !== '/unauthorized') {
        window.location.assign('/unauthorized');
      }
    }

    return Promise.reject(error);
  }
);

export default api;
