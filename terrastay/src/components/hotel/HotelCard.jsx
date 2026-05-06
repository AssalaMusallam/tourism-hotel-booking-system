import { MapPin, Star } from 'lucide-react';
import { motion } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import Badge from '../ui/Badge';
import PriceDisplay from '../PriceDisplay';
import HeartButton from '../ui/HeartButton';
import { getImageUrl } from '../../lib/imageUrl';
import styles from './HotelCard.module.css';

// HotelResponseDto: { id, name, city, country, rating, amenityNames: Set<String>,
//   images: [{id, imageUrl, fileName}], status }
const HotelCard = ({ hotel, onClick, showStatus = false, index = 0 }) => {
  const navigate = useNavigate();
  const img = getImageUrl(hotel.images?.[0]?.imageUrl || hotel.images?.[0]?.url || hotel.images?.[0]?.fileName);
  const amenityNames = hotel.amenityNames
    ? Array.from(hotel.amenityNames)
    : [];

  return (
    <motion.div
      className={styles.card}
      onClick={onClick || (() => navigate(`/hotels/${hotel.id}`))}
      initial={{ opacity: 0, y: 18 }}
      animate={{ opacity: 1, y: 0 }}
      whileHover={{ scale: 1.018, y: -4 }}
      transition={{ delay: index * 0.05, duration: 0.22 }}
    >
      <div className={styles.imgWrap}>
        <img
          src={img}
          alt={hotel.name}
          className={styles.img}
          loading="lazy"
          onError={(e) => { e.currentTarget.src = '/placeholder-hotel.jpg'; }}
        />
        {showStatus && (
          <div className={styles.statusBadge}>
            <Badge variant={hotel.status === 'ACTIVE' ? 'active' : 'inactive'}>
              {hotel.status}
            </Badge>
          </div>
        )}
        <HeartButton hotelId={hotel.id} className={styles.heart} />
      </div>
      <div className={styles.body}>
        <div className={styles.location}>
          <MapPin size={12} />
          <span>{hotel.city}{hotel.country ? `, ${hotel.country}` : ''}</span>
        </div>
        <h3 className={styles.name}>{hotel.name}</h3>
        <div className={styles.meta}>
          {hotel.rating != null && (
            <span className={styles.rating}>
              <Star size={13} fill="var(--color-terracotta)" color="var(--color-terracotta)" />
              {Number(hotel.rating).toFixed(1)}
            </span>
          )}
        </div>
        {amenityNames.length > 0 ? (
          <div className={styles.amenities}>
            {amenityNames.slice(0, 3).map((a) => (
              <span key={a} className={styles.pill}>{a}</span>
            ))}
            {amenityNames.length > 3 && (
              <span className={styles.pill}>+{amenityNames.length - 3}</span>
            )}
          </div>
        ) : null}
        <div className={styles.footer}>
          <div className={styles.price}>
            {(hotel.minPricePerNight || hotel.pricePerNight || hotel.basePrice) ? (
              <PriceDisplay usdAmount={hotel.minPricePerNight || hotel.pricePerNight || hotel.basePrice} size="sm" suffix="/night" />
            ) : (
              <span>حسب التوفر</span>
            )}
          </div>
          <span className={styles.bookNow}>احجز الآن</span>
        </div>
      </div>
    </motion.div>
  );
};

export const HotelCardSkeleton = () => (
  <div className={styles.card} style={{ cursor: 'default' }}>
    <div className={`${styles.imgWrap} skeleton`} />
    <div className={styles.body} style={{ gap: 10 }}>
      <div className="skeleton" style={{ height: 12, width: '40%' }} />
      <div className="skeleton" style={{ height: 18, width: '75%' }} />
      <div className="skeleton" style={{ height: 12, width: '30%' }} />
    </div>
  </div>
);

export default HotelCard;
