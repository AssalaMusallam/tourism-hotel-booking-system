import useAuthStore from '../store/authStore';

const useAuth = () => {
  const store = useAuthStore();
  const isAdmin = store.user?.role === 'ADMIN';
  const isManager = store.user?.role === 'MANAGER' || isAdmin;

  return {
    user: store.user,
    token: store.token,
    isAuthenticated: store.isAuthenticated,
    isAdmin,
    isManager,
    login: store.login,
    logout: store.logout,
    // aliases for backward compat
    setAuth: (user, token) => store.login(token, user),
    clearAuth: store.logout,
  };
};

export default useAuth;
