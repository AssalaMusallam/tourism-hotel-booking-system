import { CheckCircle2, Snowflake, Users, Wifi, Building2, BadgeCheck } from 'lucide-react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import Button from '../ui/Button';
import PriceBreakdown from './PriceBreakdown';
import styles from './RoomCard.module.css';

const money = (value) =>
  Number(value || 0).toLocaleString('en-US', { style: 'currency', currency: 'USD' });

const DEFAULT_AMENITIES = [
  { label: 'Free WiFi', icon: Wifi },
  { label: 'Air conditioning', icon: Snowflake },
  { label: 'City view', icon: Building2 },
];

const getRoomName = (room) => room.roomTypeName || room.name || 'Room type';
const getRoomId = (room) => room.roomTypeId || room.id;

const RoomCard = ({ room, checkIn, checkOut, onBook }) => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const amenities = room.amenities?.length
    ? room.amenities.map((label) => ({ label, icon: BadgeCheck }))
    : DEFAULT_AMENITIES;

  return (
    <article className={styles.card}>
      <div className={styles.main}>
        <div className={styles.top}>
          <div>
            <h2 className={styles.name}>{getRoomName(room)}</h2>
            <div className={styles.meta}>
              <span className={styles.metaItem}>
                <Users size={16} />
                Capacity: {room.capacity} guest{room.capacity === 1 ? '' : 's'}
              </span>
              <span className={styles.metaItem}>
                {money(room.basePrice)}/night
              </span>
            </div>
          </div>
          <span className={styles.available}>
            <CheckCircle2 size={15} />
            {room.remainingUnits} available
          </span>
        </div>

        <div className={styles.amenities}>
          {amenities.map(({ label, icon: Icon }) => (
            <span key={label} className={styles.amenity}>
              <Icon size={15} />
              {label}
            </span>
          ))}
        </div>

        <p className={styles.description}>
          Sleeps up to {room.capacity} guests with {room.remainingUnits} unit{room.remainingUnits === 1 ? '' : 's'} available for your selected dates.
        </p>
      </div>

      <div className={styles.side}>
        <PriceBreakdown roomType={room} checkIn={checkIn} checkOut={checkOut} />
        <Button
          className={styles.cta}
          variant="primary"
          size="lg"
          onClick={() => {
            if (onBook) {
              onBook(room);
              return;
            }
            const params = new URLSearchParams(searchParams);
            if (checkIn) params.set('checkIn', checkIn);
            if (checkOut) params.set('checkOut', checkOut);
            navigate(`/hotels/${room.hotelId}/rooms/${getRoomId(room)}/book?${params.toString()}`);
          }}
        >
          Book Now
        </Button>
      </div>
    </article>
  );
};

export const RoomCardSkeleton = () => (
  <div className={`${styles.card} ${styles.skeleton}`}>
    <div className={styles.main}>
      <div className="skeleton" style={{ height: 28, width: '52%' }} />
      <div className="skeleton" style={{ height: 18, width: '70%' }} />
      <div className="skeleton" style={{ height: 36, width: '85%' }} />
      <div className="skeleton" style={{ height: 48, width: '100%' }} />
    </div>
    <div className={styles.side}>
      <div className="skeleton" style={{ height: 156, width: '100%' }} />
      <div className="skeleton" style={{ height: 48, width: '100%' }} />
    </div>
  </div>
);

export const getAvailabilityRoomId = getRoomId;
export default RoomCard;
