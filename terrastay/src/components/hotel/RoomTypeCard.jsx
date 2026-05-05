import { Users, BedDouble } from 'lucide-react';
import { formatPrice } from '../../utils/formatPrice';
import Button from '../ui/Button';
import styles from './RoomTypeCard.module.css';

const RoomTypeCard = ({ room, onSelect, selected = false }) => {
  return (
    <div className={`${styles.card} ${selected ? styles.selected : ''} ${!room.available ? styles.unavailable : ''}`}>
      <div className={styles.info}>
        <h4 className={styles.type}>{room.type}</h4>
        <div className={styles.details}>
          <span className={styles.detail}><BedDouble size={14} /> {room.bedType}</span>
          <span className={styles.detail}><Users size={14} /> Up to {room.capacity} guests</span>
        </div>
      </div>
      <div className={styles.priceCol}>
        <div className={styles.price}>
          <span className={styles.priceAmount}>{formatPrice(room.pricePerNight)}</span>
          <span className={styles.priceLabel}>/night</span>
        </div>
        {room.available ? (
          <Button
            variant={selected ? 'secondary' : 'primary'}
            size="sm"
            onClick={() => onSelect(room)}
          >
            {selected ? 'Selected ✓' : 'Select Room'}
          </Button>
        ) : (
          <span className={styles.soldOut}>Sold Out</span>
        )}
      </div>
    </div>
  );
};

export default RoomTypeCard;
