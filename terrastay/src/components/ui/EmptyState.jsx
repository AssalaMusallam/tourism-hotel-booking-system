import { SearchX } from 'lucide-react';
import Button from './Button';
import styles from './EmptyState.module.css';

const EmptyState = ({
  icon: Icon = SearchX,
  title = 'Nothing here yet',
  description,
  actionLabel,
  onAction,
}) => {
  return (
    <div className={styles.container}>
      <div className={styles.iconWrap}>
        <Icon size={40} strokeWidth={1.5} />
      </div>
      <h3 className={styles.title}>{title}</h3>
      {description && <p className={styles.description}>{description}</p>}
      {actionLabel && onAction && (
        <Button variant="primary" onClick={onAction} className={styles.action}>
          {actionLabel}
        </Button>
      )}
    </div>
  );
};

export default EmptyState;
