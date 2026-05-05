import useAuthStore from '../store/authStore';

const useAuth = () => {
  const store = useAuthStore();
  const isAdmin = store.user?.role === 'ADMIN';
  const isManager = store.user?.role === 'MANAGER' || isAdmin;
  const isGuest = store.user?.role === 'GUEST';
  const isActive = store.user?.active !== false;

  return {
    user: store.user,
    token: store.token,
    isAuthenticated: store.isAuthenticated,
    isAdmin,
    isManager,
    isGuest,
    isActive,
    login: store.login,
    logout: store.logout,
    setUser: store.setUser,
    // aliases for backward compat
    setAuth: (user, token) => store.login(token, user),
    clearAuth: store.logout,
  };
};

export default useAuth;
