import { Link } from 'react-router-dom';
import { MapPin, Star } from 'lucide-react';
import { motion } from 'framer-motion';
import { formatPrice } from '../../utils/formatPrice';
import Button from '../ui/Button';
import styles from './HotelCard.module.css';

const StarRating = ({ rating, count }) => (
  <div className={styles.rating}>
    <Star size={14} fill="currentColor" />
    <span className={styles.ratingNum}>{rating}</span>
    {count && <span className={styles.ratingCount}>({count})</span>}
  </div>
);

const HotelCard = ({ hotel, index = 0 }) => {
  const imgSrc = hotel.images?.[0] || null;

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.05, duration: 0.3 }}
      whileHover={{ scale: 1.015 }}
      className={styles.card}
    >
      <Link to={`/hotels/${hotel.id}`} className={styles.imageLink}>
        <div className={styles.imageWrap}>
          {imgSrc ? (
            <img src={imgSrc} alt={hotel.name} className={styles.image} loading="lazy" />
          ) : (
            <div className={styles.imagePlaceholder}>
              <span>{hotel.city[0]}</span>
            </div>
          )}
          <div className={styles.starsOverlay}>
            {'★'.repeat(hotel.stars)}
          </div>
        </div>
      </Link>

      <div className={styles.body}>
        <div className={styles.location}>
          <MapPin size={12} />
          <span>{hotel.city}</span>
        </div>
        <Link to={`/hotels/${hotel.id}`} className={styles.name}>
          {hotel.name}
        </Link>
        <div className={styles.meta}>
          <StarRating rating={hotel.rating} count={hotel.reviewCount} />
        </div>

        {hotel.amenities?.length > 0 && (
          <div className={styles.amenities}>
            {hotel.amenities.slice(0, 3).map((a) => (
              <span key={a} className={styles.amenityTag}>{a}</span>
            ))}
            {hotel.amenities.length > 3 && (
              <span className={styles.amenityMore}>+{hotel.amenities.length - 3}</span>
            )}
          </div>
        )}

        <div className={styles.footer}>
          <div className={styles.price}>
            <span className={styles.priceAmount}>{formatPrice(hotel.pricePerNight)}</span>
            <span className={styles.priceLabel}>/night</span>
          </div>
          <Button variant="primary" size="sm" as={Link} to={`/hotels/${hotel.id}`}>
            View Hotel
          </Button>
        </div>
      </div>
    </motion.div>
  );
};

export const HotelCardSkeleton = () => (
  <div className={styles.card}>
    <div className={`${styles.imageWrap} skeleton`} style={{ height: 200 }} />
    <div className={styles.body}>
      <div className="skeleton" style={{ height: 14, width: '40%', marginBottom: 8 }} />
      <div className="skeleton" style={{ height: 20, width: '80%', marginBottom: 8 }} />
      <div className="skeleton" style={{ height: 14, width: '60%', marginBottom: 12 }} />
      <div className={styles.footer}>
        <div className="skeleton" style={{ height: 24, width: 80 }} />
        <div className="skeleton" style={{ height: 36, width: 90, borderRadius: 8 }} />
      </div>
    </div>
  </div>
);

export default HotelCard;
