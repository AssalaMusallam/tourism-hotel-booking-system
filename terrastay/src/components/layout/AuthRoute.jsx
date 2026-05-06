import { Navigate } from 'react-router-dom';
import useAuth from '../../hooks/useAuth';

const AuthRoute = ({ children }) => {
  const { isAuthenticated } = useAuth();
  if (isAuthenticated) return <Navigate to="/" replace />;
  return children;
};

export default AuthRoute;
