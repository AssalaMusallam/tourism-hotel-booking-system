import { Link } from 'react-router-dom';
import { MapPin, Star } from 'lucide-react';
import { motion } from 'framer-motion';
import styles from './HotelCard.module.css';

// HotelResponseDto shape:
// { id, name, city, country, rating, amenityNames: Set<String>,
//   images: [{id, url}], status }

const HotelCard = ({ hotel, index = 0 }) => {
  // Backend returns images[].url
  const imgSrc = hotel.images?.[0]?.url || null;
  const amenityNames = hotel.amenityNames
    ? (Array.isArray(hotel.amenityNames) ? hotel.amenityNames : [...hotel.amenityNames])
    : [];

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ delay: index * 0.05, duration: 0.3 }}
      whileHover={{ scale: 1.015, y: -2 }}
      className={styles.card}
    >
      <Link to={`/hotels/${hotel.id}`} className={styles.imageLink}>
        <div className={styles.imageWrap}>
          {imgSrc ? (
            <img src={imgSrc} alt={hotel.name} className={styles.image} loading="lazy" />
          ) : (
            <div className={styles.imagePlaceholder}>
              <span>{hotel.city?.[0] || hotel.name?.[0]}</span>
            </div>
          )}
        </div>
      </Link>

      <div className={styles.body}>
        <div className={styles.location}>
          <MapPin size={12} />
          <span>{hotel.city}{hotel.country ? `, ${hotel.country}` : ''}</span>
        </div>
        <Link to={`/hotels/${hotel.id}`} className={styles.name}>
          {hotel.name}
        </Link>
        {hotel.rating != null && (
          <div className={styles.meta}>
            <div className={styles.rating}>
              <Star size={13} fill="var(--color-terracotta)" color="var(--color-terracotta)" />
              <span className={styles.ratingNum}>{Number(hotel.rating).toFixed(1)}</span>
            </div>
          </div>
        )}

        {amenityNames.length > 0 && (
          <div className={styles.amenities}>
            {amenityNames.slice(0, 3).map((a) => (
              <span key={a} className={styles.amenityTag}>{a}</span>
            ))}
            {amenityNames.length > 3 && (
              <span className={styles.amenityMore}>+{amenityNames.length - 3}</span>
            )}
          </div>
        )}

        <div className={styles.footer}>
          <Link to={`/hotels/${hotel.id}`} className={styles.viewBtn}>
            View Hotel
          </Link>
        </div>
      </div>
    </motion.div>
  );
};

export const HotelCardSkeleton = () => (
  <div className={styles.card}>
    <div className={`${styles.imageWrap} skeleton`} style={{ height: 200 }} />
    <div className={styles.body}>
      <div className="skeleton" style={{ height: 12, width: '40%', marginBottom: 8 }} />
      <div className="skeleton" style={{ height: 20, width: '80%', marginBottom: 8 }} />
      <div className="skeleton" style={{ height: 12, width: '50%', marginBottom: 12 }} />
      <div className={styles.footer}>
        <div className="skeleton" style={{ height: 32, width: 90, borderRadius: 8 }} />
      </div>
    </div>
  </div>
);

export default HotelCard;
