import { useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import useAuth from '../hooks/useAuth';
import LoginForm from '../components/auth/LoginForm';
import styles from './AuthPage.module.css';

const LoginPage = () => {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => { document.title = 'Sign In – PinkFlow'; }, []);

  // Redirect away if already logged in
  useEffect(() => {
    if (isAuthenticated) {
      const home = user?.role === 'ADMIN' || user?.role === 'MANAGER' ? '/dashboard' : '/';
      navigate(home, { replace: true });
    }
  }, [isAuthenticated, user, navigate]);

  if (isAuthenticated) return null;

  return (
    <div className={styles.page}>
      <div className={styles.bg} />
      <motion.div
        className={styles.card}
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.35 }}
      >
        <Link to="/" className={styles.logo}>PinkFlow</Link>
        <LoginForm />
      </motion.div>
    </div>
  );
};

export default LoginPage;
