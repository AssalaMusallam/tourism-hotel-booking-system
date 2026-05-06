import { lazy, Suspense, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { MapPin, Phone, Mail, Globe, Clock, Users, BedDouble, DollarSign } from 'lucide-react';
import { motion } from 'framer-motion';
import toast from 'react-hot-toast';
import { useHotel, useRoomsByHotel } from '../hooks/useCatalogQueries';
import useAuth from '../hooks/useAuth';
import ImageGallery from '../components/hotel/ImageGallery';
import StarRating from '../components/ui/StarRating';
import Badge, { StatusBadge } from '../components/ui/Badge';
import Spinner from '../components/ui/Spinner';
import Button from '../components/ui/Button';
import HotelReviewsList from '../components/review/HotelReviewsList';
import RatingSummaryWidget from '../components/review/RatingSummaryWidget';
import PriceDisplay from '../components/PriceDisplay';
import useLanguage from '../hooks/useLanguage';
import { useLocalizedField } from '../hooks/useLocalizedField';
import styles from './HotelDetailPage.module.css';

const HotelMap = lazy(() => import('../components/map/HotelMap'));

// Format "HH:mm:ss" → "HH:mm" for display
const displayTime = (t) => t ? t.substring(0, 5) : '';

const BED_LABELS = {
  SINGLE: 'Single Bed', DOUBLE: 'Double Bed', TWIN: 'Twin Beds',
  QUEEN: 'Queen Bed', KING: 'King Bed', BUNK: 'Bunk Bed',
  SOFA_BED: 'Sofa Bed', FUTON: 'Futon',
};

const roomImageFallback = (room) => {
  const name = `${room?.name || ''} ${room?.bedType || ''}`.toLowerCase();
  if (name.includes('family') || name.includes('عائ')) return 'https://images.unsplash.com/photo-1596394516093-501ba68a0ba6?w=600&q=80';
  if (name.includes('suite') || name.includes('جناح')) return 'https://images.unsplash.com/photo-1582719478250-c89cae4dc85b?w=600&q=80';
  if (name.includes('king') || room?.bedType === 'KING') return 'https://images.unsplash.com/photo-1590490360182-c33d57733427?w=600&q=80';
  return 'https://images.unsplash.com/photo-1631049307264-da0ec9d70304?w=600&q=80';
};

const getRoomImage = (room) => room?.images?.[0]?.imageUrl || room?.imageUrl || roomImageFallback(room);

const HotelDetailPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();
  const { language, t } = useLanguage();
  const lf = useLocalizedField();

  const { data: hotel, isLoading, error } = useHotel(id);
  const { data: roomsData } = useRoomsByHotel(id, { page: 0, size: 50 });
  const rooms = roomsData?.content || [];

  useEffect(() => {
    if (hotel?.name) document.title = `${lf(hotel, 'name')} - TerraStay`;
    else document.title = 'PinkFlow – Hotel Booking';
    return () => { document.title = 'PinkFlow – Hotel Booking'; };
  }, [hotel?.name, language]);

  if (isLoading) return <Spinner centered />;
  if (error || !hotel) return (
    <div className="container" style={{ padding: '80px 0', textAlign: 'center' }}>
      <h2>Hotel not found</h2>
      <Link to="/search">← Back to Search</Link>
    </div>
  );

  const handleBookRoom = (room) => {
    if (!isAuthenticated) {
      sessionStorage.setItem('pendingBooking', JSON.stringify({
        hotelId: id,
        roomTypeId: room.id,
        roomName: room.name,
      }));
      toast('Please log in to complete your booking', { icon: '🔒' });
      navigate('/login', { state: { from: `/hotels/${id}` } });
      return;
    }
    const query = new URLSearchParams({
      checkIn: new Date().toISOString().slice(0, 10),
      checkOut: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString().slice(0, 10),
      guests: String(Math.max(1, room.maxAdults || 1)),
    });
    navigate(`/hotels/${id}/availability?${query.toString()}`, { state: { room, hotel } });
  };

  const amenityNames = hotel.amenities?.length
    ? hotel.amenities.map((amenity) => lf(amenity, 'name'))
    : (language === 'en' && hotel.amenityNamesEn
      ? Array.from(hotel.amenityNamesEn)
      : (hotel.amenityNames ? (Array.isArray(hotel.amenityNames) ? hotel.amenityNames : [...hotel.amenityNames]) : []).map((name) => (language === 'en' ? lf({ name }, 'name') : name)));
  const hotelName = lf(hotel, 'name');
  const address = lf(hotel, 'address');
  const cityName = lf(hotel, 'city');
  const countryName = lf(hotel, 'country');
  const description = lf(hotel, 'description');
  const cancellation = lf(hotel, 'cancellationPolicySummary');
  const policies = lf(hotel, 'policies');

  return (
    <div>
      {/* Breadcrumb */}
      <div className={styles.breadcrumbWrap}>
        <div className={`container ${styles.breadcrumb}`}>
          <Link to="/">Home</Link>
          <span>/</span>
          <Link to="/search">Search</Link>
          <span>/</span>
          <span>{hotelName}</span>
        </div>
      </div>

      <div className="container" style={{ paddingBottom: 80 }}>
        {/* Gallery */}
        <div className={styles.galleryWrap}>
          <ImageGallery images={hotel.images} name={hotelName} />
        </div>

        {hotel.latitude != null && hotel.longitude != null && (
          <div className={styles.embeddedMap}>
            <Suspense fallback={<div className={`skeleton ${styles.mapSkeleton}`} />}>
              <HotelMap key={hotel.id} hotels={[hotel]} selectedId={hotel.id} height="320px" />
            </Suspense>
          </div>
        )}

        {/* Hotel Header */}
        <motion.div
          className={styles.hotelHeader}
          initial={{ opacity: 0, y: 16 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.35 }}
        >
          <div className={styles.hotelMeta}>
            <div className={styles.location}>
              <MapPin size={16} />
              <span>{address} - {cityName}, {countryName}</span>
            </div>
            <h1 className={styles.hotelName}>{hotelName}</h1>
            {hotel.rating != null && (
              <div className={styles.metaRow}>
                <StarRating value={hotel.rating} size={16} />
                <span className={styles.ratingText}>{Number(hotel.rating).toFixed(1)}</span>
              </div>
            )}
            <div className={styles.contact}>
              {hotel.phoneNumber && (
                <a href={`tel:${hotel.phoneNumber}`} className={styles.contactLink}>
                  <Phone size={14} /> {hotel.phoneNumber}
                </a>
              )}
              {hotel.email && (
                <a href={`mailto:${hotel.email}`} className={styles.contactLink}>
                  <Mail size={14} /> {hotel.email}
                </a>
              )}
              {hotel.websiteUrl && (
                <a href={hotel.websiteUrl} target="_blank" rel="noreferrer" className={styles.contactLink}>
                  <Globe size={14} /> {language === 'en' ? 'Website' : 'الموقع'}
                </a>
              )}
            </div>
          </div>
        </motion.div>

        {/* Check-in / Check-out times */}
        {(hotel.checkInTime || hotel.checkOutTime) && (
          <div className={styles.timesRow}>
            {hotel.checkInTime && (
              <div className={styles.timeBlock}>
                <Clock size={14} />
                <span>{t('checkInTime')}: <strong>{displayTime(hotel.checkInTime)}</strong></span>
              </div>
            )}
            {hotel.checkOutTime && (
              <div className={styles.timeBlock}>
                <Clock size={14} />
                <span>{t('checkOutTime')}: <strong>{displayTime(hotel.checkOutTime)}</strong></span>
              </div>
            )}
          </div>
        )}

        {/* Description */}
        {description && (
          <div className={styles.descSection}>
            <h2>{language === 'en' ? 'About This Hotel' : 'نبذة عن الفندق'}</h2>
            <p className={styles.description}>{description}</p>
          </div>
        )}

        {/* Amenities */}
        {amenityNames.length > 0 && (
          <div className={styles.amenitiesSection}>
            <h2>{t('amenities')}</h2>
            <div className={styles.amenityPills}>
              {amenityNames.map((a) => (
                <Badge key={a} variant="category">{a}</Badge>
              ))}
            </div>
          </div>
        )}

        {/* Policies */}
        {(hotel.policies || hotel.cancellationPolicySummary) && (
          <div className={styles.policiesSection}>
            <h2>{language === 'en' ? 'Policies' : 'السياسات'}</h2>
            {policies && <p>{policies}</p>}
            {cancellation && (
              <div className={styles.cancelPolicy}>
                <strong>{t('cancellationPolicy')}:</strong> {cancellation}
              </div>
            )}
          </div>
        )}

        {/* Room Types */}
        <div className={styles.roomsSection}>
          <h2>{language === 'en' ? 'Available Rooms' : 'الغرف المتاحة'}</h2>
          {rooms.length === 0 ? (
            <p className={styles.noRooms}>No rooms available at this time.</p>
          ) : (
            <div className={styles.roomsList}>
              {rooms.filter(r => r.status === 'ACTIVE').map((room) => (
                <motion.div
                  key={room.id}
                  className={styles.roomCard}
                  initial={{ opacity: 0, y: 8 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ duration: 0.3 }}
                >
                  <img className={styles.roomImage} src={getRoomImage(room)} alt={lf(room, 'name')} loading="lazy" />
                  <div className={styles.roomInfo}>
                    <h3 className={styles.roomName}>{lf(room, 'name')}</h3>
                    <div className={styles.roomDetails}>
                      <span className={styles.roomDetail}>
                        <BedDouble size={14} />
                        {room.bedCount}x {BED_LABELS[room.bedType] || room.bedType}
                      </span>
                      <span className={styles.roomDetail}>
                        <Users size={14} />
                        {room.maxAdults} adult{room.maxAdults !== 1 ? 's' : ''}
                        {room.maxChildren > 0 && `, ${room.maxChildren} child${room.maxChildren !== 1 ? 'ren' : ''}`}
                      </span>
                      <span className={styles.roomDetail}>
                        {t('capacity')}: {room.capacity}
                      </span>
                    </div>
                    {(room.description || language === 'en') && <p className={styles.roomDesc}>{lf(room, 'description')}</p>}
                  </div>
                  <div className={styles.roomAction}>
                    <div className={styles.roomPrice}>
                      <span className={styles.priceAmount}><PriceDisplay usdAmount={room.basePrice} size="md" /></span>
                      <span className={styles.priceNight}>/{t('perNight')}</span>
                    </div>
                    <span className={styles.roomUnits}>{room.totalUnits} unit{room.totalUnits !== 1 ? 's' : ''}</span>
                    <Button variant="primary" size="sm" onClick={() => handleBookRoom(room)}>
                      {t('bookNow')}
                    </Button>
                  </div>
                </motion.div>
              ))}
            </div>
          )}
        </div>

        {/* Rating Summary */}
        <div className={styles.reviewsSection}>
          <h2>{t('reviews')}</h2>
          <RatingSummaryWidget hotelId={id} />
          <HotelReviewsList hotelId={id} />
        </div>
      </div>
    </div>
  );
};

export default HotelDetailPage;
