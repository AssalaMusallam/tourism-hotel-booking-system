import { create } from 'zustand';

const loadFromStorage = () => {
  try {
    const token = localStorage.getItem('terrastay_token');
    const user = JSON.parse(localStorage.getItem('terrastay_user') || 'null');
    return { token, user };
  } catch {
    return { token: null, user: null };
  }
};

const { token: initialToken, user: initialUser } = loadFromStorage();

const useAuthStore = create((set) => ({
  user: initialUser,
  token: initialToken,
  isAuthenticated: !!initialToken,

  setAuth: (user, token) => {
    localStorage.setItem('terrastay_token', token);
    localStorage.setItem('terrastay_user', JSON.stringify(user));
    set({ user, token, isAuthenticated: true });
  },

  clearAuth: () => {
    localStorage.removeItem('terrastay_token');
    localStorage.removeItem('terrastay_user');
    set({ user: null, token: null, isAuthenticated: false });
  },
}));

export default useAuthStore;
