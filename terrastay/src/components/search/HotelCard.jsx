import { Link } from 'react-router-dom';
import { MapPin, Star } from 'lucide-react';
import { motion } from 'framer-motion';
import PriceDisplay from '../PriceDisplay';
import { getImageUrl } from '../../lib/imageUrl';
import useLanguage from '../../hooks/useLanguage';
import { useLocalizedField } from '../../hooks/useLocalizedField';
import styles from './HotelCard.module.css';

// HotelResponseDto shape:
// { id, name, city, country, rating, amenityNames: Set<String>,
//   images: [{id, url}], status }

const HotelCard = ({ hotel, index = 0 }) => {
  const { language, t } = useLanguage();
  const lf = useLocalizedField();
  const imgSrc = getImageUrl(
    hotel.images?.[0]?.imageUrl || hotel.images?.[0]?.url || hotel.images?.[0]?.fileName || hotel.imageUrl || hotel.image || '/placeholder-hotel.jpg'
  );
  const amenityNames = hotel.amenities?.length
    ? hotel.amenities.map((amenity) => lf(amenity, 'name'))
    : (language === 'en' && hotel.amenityNamesEn
      ? Array.from(hotel.amenityNamesEn)
      : (Array.isArray(hotel.amenityNames) ? hotel.amenityNames : [...(hotel.amenityNames || [])]).map((name) => (language === 'en' ? lf({ name }, 'name') : name)));
  const hotelName = lf(hotel, 'name');
  const city = lf(hotel, 'city');
  const country = lf(hotel, 'country');

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
          <img
            src={imgSrc}
            alt={hotelName}
            className={styles.image}
            loading="lazy"
            onError={(e) => { e.currentTarget.src = '/placeholder-hotel.jpg'; }}
          />
        </div>
      </Link>

      <div className={styles.body}>
        <div className={styles.location}>
          <MapPin size={12} />
          <span>{city}{country ? `, ${country}` : ''}</span>
        </div>
        <Link to={`/hotels/${hotel.id}`} className={styles.name}>
          {hotelName}
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
          {(hotel.minPricePerNight || hotel.pricePerNight || hotel.basePrice) && (
            <div className={styles.fromPrice}>
              {t('basePrice')} <PriceDisplay usdAmount={hotel.minPricePerNight || hotel.pricePerNight || hotel.basePrice} size="sm" suffix={`/${t('perNight')}`} />
            </div>
          )}
          <Link to={`/hotels/${hotel.id}`} className={styles.viewBtn}>
            {t('viewDetails')}
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
