import { useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import useAuth from '../hooks/useAuth';
import RegisterForm from '../components/auth/RegisterForm';
import styles from './AuthPage.module.css';

const RegisterPage = () => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => { document.title = 'Create Account – PinkFlow'; }, []);

  // Redirect away if already logged in
  useEffect(() => {
    if (isAuthenticated) navigate('/', { replace: true });
  }, [isAuthenticated, navigate]);

  if (isAuthenticated) return null;

  return (
    <div className={styles.page}>
      <div className={styles.bg} />
      <motion.div
        className={`${styles.card} ${styles.cardTall}`}
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.35 }}
      >
        <Link to="/" className={styles.logo}>PinkFlow</Link>
        <RegisterForm />
      </motion.div>
    </div>
  );
};

export default RegisterPage;
