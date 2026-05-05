import styles from './Card.module.css';
import { cn } from '../../utils/cn';

const Card = ({ children, className, onClick, hover = false }) => {
  return (
    <div
      className={cn(styles.card, hover && styles.hover, onClick && styles.clickable, className)}
      onClick={onClick}
    >
      {children}
    </div>
  );
};

export default Card;
