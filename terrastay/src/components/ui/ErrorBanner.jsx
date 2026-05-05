import { AlertCircle, X } from 'lucide-react';
import styles from './ErrorBanner.module.css';

const ErrorBanner = ({ message, onDismiss, children }) => {
  if (!message) return null;

  return (
    <div className={styles.banner} role="alert">
      <AlertCircle className={styles.icon} size={18} aria-hidden="true" />
      <div className={styles.content}>
        <p className={styles.message}>{message}</p>
        {children}
      </div>
      {onDismiss && (
        <button
          type="button"
          className={styles.dismiss}
          onClick={onDismiss}
          aria-label="Dismiss error"
        >
          <X size={16} aria-hidden="true" />
        </button>
      )}
    </div>
  );
};

export default ErrorBanner;
