import { AlertCircle } from 'lucide-react';
import Button from './Button';
import styles from './SectionError.module.css';

const SectionError = ({ message, onRetry }) => {
  if (!message) return null;

  return (
    <div className={styles.section} role="alert">
      <AlertCircle className={styles.icon} size={20} aria-hidden="true" />
      <div className={styles.content}>
        <p className={styles.title}>Could not load data</p>
        <p className={styles.message}>{message}</p>
        {onRetry && (
          <Button className={styles.action} variant="secondary" size="sm" onClick={onRetry}>
            Try Again
          </Button>
        )}
      </div>
    </div>
  );
};

export default SectionError;
