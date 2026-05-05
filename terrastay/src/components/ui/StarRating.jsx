import { Star } from 'lucide-react';
import { useState } from 'react';
import styles from './StarRating.module.css';

const StarRating = ({ value = 0, interactive = false, onChange, size = 18 }) => {
  const [hovered, setHovered] = useState(null);
  const display = hovered ?? Math.round(value);

  return (
    <div className={[styles.stars, interactive ? styles.interactive : ''].join(' ')}>
      {[1, 2, 3, 4, 5].map((s) => (
        <button
          key={s}
          type="button"
          disabled={!interactive}
          className={styles.star}
          onClick={() => interactive && onChange?.(s)}
          onMouseEnter={() => interactive && setHovered(s)}
          onMouseLeave={() => interactive && setHovered(null)}
          aria-label={`${s} star${s !== 1 ? 's' : ''}`}
        >
          <Star
            size={size}
            fill={s <= display ? 'var(--color-terracotta)' : 'var(--color-beige)'}
            color={s <= display ? 'var(--color-terracotta)' : 'var(--color-beige)'}
          />
        </button>
      ))}
    </div>
  );
};
export default StarRating;
