import styles from './Badge.module.css';
import { cn } from '../../utils/cn';

const statusVariant = {
  CONFIRMED: 'confirmed',
  PENDING: 'pending',
  CANCELLED: 'cancelled',
};

const Badge = ({ children, variant = 'default', status, className }) => {
  const v = status ? (statusVariant[status] || 'default') : variant;
  return (
    <span className={cn(styles.badge, styles[v], className)}>
      {status || children}
    </span>
  );
};

export default Badge;
