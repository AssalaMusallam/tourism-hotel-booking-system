import { SearchX } from 'lucide-react';
import Button from './Button';
import styles from './EmptyState.module.css';

const EmptyState = ({
  icon: Icon = SearchX,
  title = 'Nothing here yet',
  description,
  action,
}) => (
  <div className={styles.wrap}>
    <div className={styles.iconWrap}><Icon size={36} strokeWidth={1.5} /></div>
    <h3 className={styles.title}>{title}</h3>
    {description && <p className={styles.desc}>{description}</p>}
    {action && (
      <Button variant="secondary" size="sm" onClick={action.onClick}>
        {action.label}
      </Button>
    )}
  </div>
);
export default EmptyState;
