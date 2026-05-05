import { Navigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';

// Protects admin routes — allows ADMIN and MANAGER roles
const AdminRoute = ({ children }) => {
  const { isAuthenticated, isManager } = useAuth();

  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (!isManager) return <Navigate to="/" replace />;

  return children;
};

export default AdminRoute;
