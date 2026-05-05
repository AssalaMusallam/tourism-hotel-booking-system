import { Navigate, useLocation } from 'react-router-dom';
import toast from 'react-hot-toast';
import useAuth from '../../hooks/useAuth';

const ProtectedRoute = ({ children, roles, requireActive = false }) => {
  const { isAuthenticated, user } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: `${location.pathname}${location.search}` }} replace />;
  }

  if (roles?.length && !roles.includes(user?.role)) {
    toast.error("You don't have permission");
    return <Navigate to="/" replace />;
  }

  if (requireActive && user?.active === false) {
    toast.error('Your account is inactive. Contact support.');
    return <Navigate to="/profile" replace />;
  }

  return children;
};

export default ProtectedRoute;
