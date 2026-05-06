import { Navigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';

const ManagerRoute = ({ children }) => {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (user?.role === 'ADMIN') return <Navigate to="/admin" replace />;
  if (user?.role !== 'MANAGER') return <Navigate to="/" replace />;

  return children;
};

export default ManagerRoute;
