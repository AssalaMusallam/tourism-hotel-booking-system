import useAuth from '../../hooks/useAuth';
import styles from './InactiveAccountBanner.module.css';

const InactiveAccountBanner = () => {
  const { isAuthenticated, user } = useAuth();

  if (!isAuthenticated || user?.active !== false) return null;

  return (
    <div className={styles.banner} role="alert">
      Your account is inactive. Contact support.
    </div>
  );
};

export default InactiveAccountBanner;
