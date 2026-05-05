import { Link, useNavigate } from 'react-router-dom';
import { Lock } from 'lucide-react';
import useAuth from '../hooks/useAuth';
import Button from '../components/ui/Button';
import styles from './UnauthorizedPage.module.css';

/**
 * 403 Unauthorized page — shown when user lacks the required role.
 */
const UnauthorizedPage = () => {
  const { isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  const handleSignOut = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <main className={styles.page}>
      <div className={styles.card}>
        <div className={styles.iconWrap}>
          <Lock size={36} />
        </div>
        <h1 className={styles.title}>Access Denied</h1>
        <p className={styles.desc}>You don&rsquo;t have permission to view this page.</p>

        <div className={styles.actions}>
          <Button variant="secondary" onClick={() => navigate('/', { replace: true })}>
            ← Go Home
          </Button>
          {isAuthenticated ? (
            <Button variant="primary" onClick={handleSignOut}>
              Sign in with different account
            </Button>
          ) : (
            <Link to="/login">
              <Button variant="primary">Sign In</Button>
            </Link>
          )}
        </div>
      </div>
    </main>
  );
};

export default UnauthorizedPage;
