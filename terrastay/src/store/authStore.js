import { create } from 'zustand';

const TOKEN_KEY = 'terrastay_token';
const USER_KEY  = 'terrastay_user';

const useAuthStore = create((set) => ({
  token: null,
  user: null,
  isAuthenticated: false,

  initialize: () => {
    try {
      const token = localStorage.getItem(TOKEN_KEY);
      const user  = JSON.parse(localStorage.getItem(USER_KEY) || 'null');
      if (token && user) {
        set({ token, user, isAuthenticated: true });
      }
    } catch {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    }
  },

  login: (token, user) => {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    set({ token, user, isAuthenticated: true });
  },

  logout: () => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    set({ token: null, user: null, isAuthenticated: false });
  },
}));

// Initialize immediately on store creation
useAuthStore.getState().initialize();

// Listen for 401 events from axios interceptor
window.addEventListener('auth:logout', () => {
  useAuthStore.getState().logout();
});

export default useAuthStore;

// Selector hooks
export const useAuth = () => useAuthStore();
export const useIsAdmin   = () => useAuthStore((s) => s.user?.role === 'ADMIN');
export const useIsManager = () => useAuthStore((s) => s.user?.role === 'MANAGER' || s.user?.role === 'ADMIN');
