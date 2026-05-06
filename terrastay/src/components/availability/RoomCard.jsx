import { CheckCircle2, Snowflake, Users, Wifi, Building2, BadgeCheck } from 'lucide-react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import Button from '../ui/Button';
import PriceBreakdown from './PriceBreakdown';
import PriceBreakdownCard from '../pricing/PriceBreakdownCard';
import JoinWaitingListButton from '../waitingList/JoinWaitingListButton';
import PriceDisplay from '../PriceDisplay';
import { useRoomPricePreview } from '../../hooks/usePricingRules';
import useLanguage from '../../hooks/useLanguage';
import { useLocalizedField } from '../../hooks/useLocalizedField';
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
const roomImageFallback = (room) => {
  const name = `${room?.name || room?.roomTypeName || ''} ${room?.bedType || ''}`.toLowerCase();
  if (name.includes('family') || name.includes('عائ')) return 'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=600&q=80';
  if (name.includes('suite') || name.includes('جناح')) return 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600&q=80';
  if (name.includes('king') || room?.bedType === 'KING') return 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600&q=80';
  return 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=600&q=80';
};

const getRoomImage = (room) => room?.images?.[0]?.imageUrl || room?.imageUrl || roomImageFallback(room);

const RoomCard = ({ room, checkIn, checkOut, onBook }) => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { language } = useLanguage();
  const lf = useLocalizedField();
  const roomTypeId = getRoomId(room);
  const { data: priceBreakdown, isLoading: priceLoading, isError: priceError } = useRoomPricePreview(
    roomTypeId, checkIn, checkOut
  );
  const amenities = room.amenities?.length
    ? room.amenities.map((amenity) => ({ label: typeof amenity === 'string' ? lf({ name: amenity }, 'name') : lf(amenity, 'name'), icon: BadgeCheck }))
    : DEFAULT_AMENITIES;
  const localizedRoomName = language === 'en'
    ? (room.roomTypeNameEn || room.nameEn || lf({ ...room, name: getRoomName(room) }, 'name'))
    : getRoomName(room);
  const localizedDescription = language === 'en'
    ? (room.descriptionEn || lf({ ...room, description: room.description }, 'description'))
    : room.description;

  return (
    <article className={`${styles.card} ${!room.available ? styles.full : ''}`}>
      <div className={styles.media}>
        <img src={getRoomImage(room)} alt={localizedRoomName} loading="lazy" />
      </div>
      <div className={styles.main}>
        <div className={styles.top}>
          <div>
            <h2 className={styles.name}>{localizedRoomName}</h2>
            <div className={styles.meta}>
              <span className={styles.metaItem}>
                <Users size={16} />
                Capacity: {room.capacity} guest{room.capacity === 1 ? '' : 's'}
              </span>
              <span className={styles.metaItem}>
                <PriceDisplay usdAmount={room.basePrice} size="sm" suffix="/night" />
              </span>
            </div>
          </div>
          {room.available ? (
            <span className={styles.available}>
              <CheckCircle2 size={15} />
              {room.remainingUnits} available
            </span>
          ) : (
            <span className={styles.fullBadge}>Fully Booked</span>
          )}
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
          {localizedDescription || (room.available
            ? `Sleeps up to ${room.capacity} guests with ${room.remainingUnits} unit${room.remainingUnits === 1 ? '' : 's'} available for your selected dates.`
            : `Sleeps up to ${room.capacity} guests. Join the waiting list and we will notify you if a spot opens.`)}
        </p>
      </div>

      <div className={styles.side}>
        {priceLoading ? (
          <div className="skeleton" style={{ height: 140, width: '100%', borderRadius: 8 }} />
        ) : priceError ? (
          <PriceBreakdown roomType={room} checkIn={checkIn} checkOut={checkOut} />
        ) : priceBreakdown ? (
          <PriceBreakdownCard breakdown={priceBreakdown} />
        ) : (
          <PriceBreakdown roomType={room} checkIn={checkIn} checkOut={checkOut} />
        )}
        {room.available ? (
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
        ) : (
          <JoinWaitingListButton
            roomTypeId={getRoomId(room)}
            roomTypeName={getRoomName(room)}
            hotelName={room.hotelName}
            checkIn={checkIn}
            checkOut={checkOut}
            onBookAvailable={() => onBook?.({ ...room, available: true })}
          />
        )}
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
