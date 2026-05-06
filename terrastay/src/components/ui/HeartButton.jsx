import { Heart } from 'lucide-react';
import { motion } from 'framer-motion';
import useFavorites from '../../hooks/useFavorites';
import styles from './HeartButton.module.css';

const HeartButton = ({ hotelId, className = '' }) => {
  const { isFavorite, toggleFavorite } = useFavorites();
  const active = isFavorite(hotelId);

  return (
    <motion.button
      type="button"
      className={`${styles.button} ${active ? styles.active : ''} ${className}`}
      onClick={(event) => {
        event.preventDefault();
        event.stopPropagation();
        toggleFavorite(hotelId);
      }}
      whileTap={{ scale: 0.82 }}
      aria-label={active ? 'Remove from favorites' : 'Add to favorites'}
    >
      <Heart size={19} fill={active ? 'currentColor' : 'none'} />
    </motion.button>
  );
};

export default HeartButton;
